package mx.ecomanifiesto.app.data

// los 4 roles del sistema, key es como se guarda en firestore
enum class Rol(val label: String, val key: String) {
    GENERADOR("Generador", "generador"),
    TRANSPORTISTA("Transportista", "transportista"),
    DESTINO_FINAL("Destino Final", "destino_final"),
    ADMINISTRADOR("Administrador", "administrador")
}

// un manifiesto es un documento de la coleccion "manifiestos" (el id es el folio)
// todos los campos tienen valor por defecto porque firestore necesita un constructor vacio
data class Manifiesto(
    val folio: String = "",
    val estado: String = "CREADO", // CREADO -> EN_TRANSITO -> ENTREGADO
    val residuo: String = "",
    val residuoDetalle: String = "",
    val claveResiduo: String = "",
    val cretib: List<String> = emptyList(),
    val cantidad: Double = 0.0,
    val unidad: String = "L",
    val generador: String = "",
    val instalacion: String = "",
    val transportista: String = "",
    val vehiculo: String = "",
    val placas: String = "",
    val conductor: String = "",
    val generadorUid: String = "",
    val creadoEn: Long = 0L,
    val hash: String = "",
    val firmaTransportista: String = "",
    val transportistaFirmante: String = "",
    val enTransitoEn: Long = 0L,
    val firmaReceptor: String = "",
    val receptorNombre: String = "",
    val entregadoEn: Long = 0L,
    val hashEntrega: String = "",
    // campos del formato oficial del manifiesto, el numero es el campo del formato en papel
    val registroAmbiental: String = "", // campo 1
    val generadorFirmante: String = "", // campo 7
    val generadorDomicilio: String = "", // campo 4
    val envase: String = "", // campo 5: tipo de envase
    val capacidadEnvase: String = "", // campo 5: capacidad
    val etiquetado: Boolean = true, // campo 5: etiqueta Si/No
    val instrucciones: String = "", // campo 6
    val tipoVehiculo: String = "", // campo 11
    val autorizacionTransportista: String = "", // campo 9
    val permisoSct: String = "", // campo 10
    val ruta: String = "", // campo 13
    val destinatario: String = "", // campo 15
    val destinoDomicilio: String = "", // campo 15
    val autorizacionDestinatario: String = "", // campo 16
    val receptorCargo: String = "", // campo 17
    val observaciones: String = "" // campo 18
)

// catalogos: residuos, transportistas y destinos, se leen de firestore
// (los llena la pagina web del admin), activo = false lo oculta sin borrarlo
data class ResiduoCat(
    val nombre: String = "",
    val detalle: String = "",
    val clave: String = "",
    val cretib: List<String> = emptyList(),
    val envase: String = "",
    val capacidad: String = "",
    val instrucciones: String = "",
    val activo: Boolean = true
)

data class TransportistaCat(
    val empresa: String = "",
    val vehiculo: String = "",
    val placas: String = "",
    val conductor: String = "",
    val tipoVehiculo: String = "",
    val autorizacion: String = "",
    val permisoSct: String = "",
    val activo: Boolean = true
)

data class DestinoCat(
    val empresa: String = "",
    val domicilio: String = "",
    val autorizacion: String = "",
    val activo: Boolean = true
)

// junta los tres catalogos en un solo objeto para cargarlos de una vez
data class Catalogos(
    val residuos: List<ResiduoCat>,
    val transportistas: List<TransportistaCat>,
    val destinos: List<DestinoCat>
)

// OJO: son datos de ejemplo, los numeros de autorizacion y permisos no son reales
// se usan cuando firestore no tiene catalogos o no hay internet
val RESIDUOS = listOf(
    ResiduoCat(
        "Aceite Lubricante Usado", "Base Mineral de Maquinaria Pesada", "RP-ALG-01", listOf("T", "I"),
        "Tambor metálico", "200 L", "Mantener alejado de fuentes de calor. No mezclar con otros residuos."
    ),
    ResiduoCat(
        "Lodos con Metales Pesados", "Tratamiento de Aguas Residuales", "RP-LMP-02", listOf("T", "C"),
        "Tote", "1000 L", "Evitar contacto con la piel. Usar equipo de protección personal."
    ),
    ResiduoCat(
        "Solventes Orgánicos Usados", "Limpieza Industrial", "RP-SOL-03", listOf("T", "I"),
        "Tambor metálico", "200 L", "Altamente inflamable. Sin chispas ni flama abierta."
    ),
    ResiduoCat(
        "Ácidos Corrosivos", "Procesos de Galvanizado", "RP-ACI-04", listOf("C", "R"),
        "Bidón plástico", "50 L", "Corrosivo. Mantener separado de bases y materiales reactivos."
    )
)

val TRANSPORTISTAS = listOf(
    TransportistaCat(
        "EcoTransportes del Centro S.A.", "Kenworth T680", "45-AF-9X", "Roberto Morales G.",
        "Tractocamión con tanque", "SEMARNAT-09-EJEMPLO-001", "SCT-EJEMPLO-000000000001"
    ),
    TransportistaCat(
        "Transportes Químicos del Norte S.A. de C.V.", "Kenworth T680", "45-AF-9X", "Martha Morales",
        "Tractocamión con tanque", "SEMARNAT-09-EJEMPLO-002", "SCT-EJEMPLO-000000000002"
    ),
    TransportistaCat(
        "Remolques y Fletes Industriales del Golfo", "Lowboy Kenworth W900", "52-CC-6M", "Carlos Aranza Montes",
        "Plataforma", "SEMARNAT-09-EJEMPLO-003", "SCT-EJEMPLO-000000000003"
    )
)

val DESTINO = DestinoCat(
    "Termodestructora Monterrey",
    "Monterrey, Nuevo León",
    "SEMARNAT-19-EJEMPLO-001"
)

// respaldo para cuando firestore esta vacio o falla la conexion
fun catalogosPorDefecto() = Catalogos(RESIDUOS, TRANSPORTISTAS, listOf(DESTINO))

// limite de litros por manifiesto, se valida al crearlo
const val CAPACIDAD_MAXIMA_L = 1200.0

// datos del usuario que inicio sesion mientras la app esta abierta
// se llenan desde la coleccion "users", los valores por defecto son de ejemplo
object Session {
    var rol: Rol? = null
    var nombre: String = ""
    var cargo: String = ""
    var empresa: String = "Ind. Químicas del Bajío"
    var instalacion: String = "Planta Manufacturera Toluca - Nave 4"
    var direccion: String = "C. Industrial Park 204, Toluca EdoMex"
    var registro: String = "09-028-PS-I-2024"

    // se llama al cerrar sesion
    fun limpiar() {
        rol = null
        nombre = ""
        cargo = ""
    }
}
