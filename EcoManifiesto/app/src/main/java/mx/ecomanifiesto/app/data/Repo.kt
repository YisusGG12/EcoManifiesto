package mx.ecomanifiesto.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Calendar
import java.util.Locale
import java.util.UUID

// aqui estan todas las llamadas a firebase (auth y firestore) para no ponerlas en las pantallas
object Repo {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    // saca el hash sha256 de un texto, lo usamos como sello del manifiesto
    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }

    // login con correo y contraseña de firebase auth
    // tambien revisa que el rol elegido en pantalla sea el mismo que tiene guardado la cuenta
    suspend fun signIn(email: String, password: String, rol: Rol): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = auth.currentUser ?: error("No se pudo iniciar sesión.")
        val ref = db.collection("users").document(user.uid)
        val snap = ref.get().await()
        // rol guardado en la coleccion users (es null si es su primer login)
        val guardado = snap.getString("rol")
        // si eligio un rol distinto al de su cuenta lo sacamos
        if (guardado != null && guardado != rol.key) {
            auth.signOut()
            error("Tu cuenta no tiene acceso con el rol ${rol.label}.")
        }
        // primer login: guardamos el rol que escogio
        if (guardado == null) {
            ref.set(mapOf("rol" to rol.key, "email" to email.trim()), SetOptions.merge()).await()
        }
        // pasamos los datos del perfil a Session
        aplicarPerfil(snap, rol, email.trim())
    }

    // si ya hay un usuario logueado de antes, recupera su rol y perfil sin pedir login otra vez
    suspend fun restaurarSesion(): Rol? {
        val user = auth.currentUser ?: return null
        return runCatching {
            val snap = db.collection("users").document(user.uid).get().await()
            val rol = Rol.entries.firstOrNull { it.key == snap.getString("rol") } ?: return null
            aplicarPerfil(snap, rol, user.email.orEmpty())
            rol
        }.getOrNull()
    }

    fun signOut() {
        auth.signOut()
        Session.limpiar()
    }

    // copia los datos del documento users/{uid} a Session, si no existen usa valores de ejemplo
    private fun aplicarPerfil(snap: DocumentSnapshot, rol: Rol, email: String) {
        Session.rol = rol
        // nombres de ejemplo para cuando el usuario no tiene nombre en firestore
        Session.nombre = snap.getString("nombre") ?: when (rol) {
            Rol.GENERADOR -> "Lic. Martha Morales"
            Rol.TRANSPORTISTA -> "Juan Pérez"
            Rol.DESTINO_FINAL -> "Ing. Roberto Garza"
            Rol.ADMINISTRADOR -> email.substringBefore("@").ifBlank { "Ing. Roberto Vega" }
        }
        Session.cargo = snap.getString("cargo") ?: when (rol) {
            Rol.DESTINO_FINAL -> "Sup. Planta Destino Final"
            else -> rol.label
        }
        snap.getString("empresa")?.let { Session.empresa = it }
        snap.getString("instalacion")?.let { Session.instalacion = it }
        snap.getString("direccion")?.let { Session.direccion = it }
        snap.getString("registro")?.let { Session.registro = it }
    }

    // lee los catalogos activos de firestore
    // si alguno viene vacio o hay error usa los de ejemplo de Models.kt
    suspend fun cargarCatalogos(): Catalogos {
        val base = catalogosPorDefecto()
        return runCatching {
            val residuos = db.collection("residuos").get().await()
                .toObjects(ResiduoCat::class.java)
                // solo los activos y que tengan nombre
                .filter { it.activo && it.nombre.isNotBlank() }
                .sortedBy { it.nombre }
            val transportistas = db.collection("transportistas").get().await()
                .toObjects(TransportistaCat::class.java)
                .filter { it.activo && it.empresa.isNotBlank() }
                .sortedBy { it.empresa }
            val destinos = db.collection("destinatarios").get().await()
                .toObjects(DestinoCat::class.java)
                .filter { it.activo && it.empresa.isNotBlank() }
                .sortedBy { it.empresa }
            Catalogos(
                residuos.ifEmpty { base.residuos },
                transportistas.ifEmpty { base.transportistas },
                destinos.ifEmpty { base.destinos }
            )
        // si algo truena regresamos los de ejemplo
        }.getOrDefault(base)
    }

    // crea el manifiesto: saca el siguiente folio con una transaccion (para que no se repita) y lo guarda
    suspend fun crearManifiesto(base: Manifiesto): Manifiesto {
        val contador = db.collection("counters").document("manifiestos")
        val n = db.runTransaction { tx ->
            // si no existe el contador empieza en 41 para que el primer folio sea el 0042
            val actual = tx.get(contador).getLong("n") ?: 41L
            val siguiente = actual + 1
            tx.set(contador, mapOf("n" to siguiente))
            siguiente
        }.await()
        val ahora = System.currentTimeMillis()
        val anio = Calendar.getInstance().get(Calendar.YEAR)
        // formato EM-AAAA-0042
        val folio = String.format(Locale.US, "EM-%d-%04d", anio, n)
        val m = base.copy(
            folio = folio,
            estado = "CREADO",
            creadoEn = ahora,
            generadorUid = auth.currentUser?.uid.orEmpty(),
            // sello del manifiesto: hash de folio + residuo + cantidad + fecha
            hash = sha256("$folio|${base.residuo}|${base.cantidad}|$ahora")
        )
        db.collection("manifiestos").document(folio).set(m).await()
        return m
    }

    // busca un manifiesto por folio (lo pasa a mayusculas por si lo escriben a mano)
    suspend fun getManifiesto(folio: String): Manifiesto? =
        db.collection("manifiestos").document(folio.trim().uppercase()).get().await()
            .toObject(Manifiesto::class.java)

    // regresa el manifiesto CREADO mas reciente, que es al que le toca firmar el transportista
    suspend fun ultimoPorFirmar(): Manifiesto? =
        db.collection("manifiestos")
            .whereEqualTo("estado", "CREADO")
            // traemos 25 y escogemos el mas reciente aqui mismo
            .limit(25)
            .get().await()
            .toObjects(Manifiesto::class.java)
            .maxByOrNull { it.creadoEn }

    // guarda la firma del transportista y cambia el estado a EN_TRANSITO
    suspend fun firmarCustodia(folio: String, trazo: String): Manifiesto? {
        val ahora = System.currentTimeMillis()
        val ref = db.collection("manifiestos").document(folio)
        ref.update(
            mapOf(
                "estado" to "EN_TRANSITO",
                "firmaTransportista" to trazo,
                "transportistaFirmante" to Session.nombre,
                "enTransitoEn" to ahora
            )
        ).await()
        return ref.get().await().toObject(Manifiesto::class.java)
    }

    // guarda la firma de quien recibe y cambia el estado a ENTREGADO, con su hash de entrega
    suspend fun confirmarRecepcion(folio: String, trazo: String): Manifiesto? {
        val ahora = System.currentTimeMillis()
        val ref = db.collection("manifiestos").document(folio)
        ref.update(
            mapOf(
                "estado" to "ENTREGADO",
                "firmaReceptor" to trazo,
                "receptorNombre" to Session.nombre,
                "receptorCargo" to Session.cargo,
                "entregadoEn" to ahora,
                "hashEntrega" to sha256("$folio|$trazo|$ahora")
            )
        ).await()
        return ref.get().await().toObject(Manifiesto::class.java)
    }

    // crea un token que dura 5 minutos y regresa la url que va dentro del QR
    // (la pagina web todavia no lo valida, falta esa parte)
    suspend fun crearTokenHandoff(): String {
        val token = UUID.randomUUID().toString().replace("-", "").take(24)
        db.collection("handoffs").document(token).set(
            mapOf(
                "uid" to auth.currentUser?.uid.orEmpty(),
                // 5 minutos en milisegundos
                "expiraEn" to System.currentTimeMillis() + 5 * 60 * 1000L,
                "usado" to false
            )
        ).await()
        return "https://app.ecomanifiesto.mx/acceso?token=$token"
    }
}
