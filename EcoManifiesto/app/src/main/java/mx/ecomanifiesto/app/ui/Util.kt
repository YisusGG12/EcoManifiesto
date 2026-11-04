package mx.ecomanifiesto.app.ui

import android.graphics.Bitmap
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import mx.ecomanifiesto.app.data.Manifiesto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// formatos de fecha y hora a partir de milisegundos
fun fmtFecha(ms: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))

fun fmtHora(ms: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))

// convierte la letra CRETIB a su nombre completo
fun cretibLabel(c: String): String = when (c) {
    "T" -> "T - Tóxico"
    "I" -> "I - Inflamable"
    "C" -> "C - Corrosivo"
    "R" -> "R - Reactivo"
    "E" -> "E - Explosivo"
    "B" -> "B - Biológico-Infeccioso"
    else -> c
}

fun cantidadTexto(m: Manifiesto): String = cantidadTexto(m.cantidad, m.unidad)

// si la cantidad es entera no muestra el .0 (500.0 pasa a 500)
fun cantidadTexto(c: Double, unidad: String): String {
    val n = if (c % 1.0 == 0.0) c.toLong().toString() else c.toString()
    return "$n $unidad"
}

// cambia las excepciones de firebase por mensajes que si entienda el usuario
fun friendlyError(t: Throwable): String = when (t) {
    is FirebaseAuthInvalidCredentialsException,
    is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos."
    is FirebaseNetworkException -> "Sin conexión. Revisa tu internet."
    is FirebaseFirestoreException ->
        "No se pudo acceder a los datos (${t.code.name.lowercase()}). Revisa las reglas de Firestore."
    else -> t.message ?: "Ocurrió un error inesperado."
}

// genera la imagen del QR con zxing, pinta cada pixel de verde oscuro o de blanco
fun qrBitmap(contenido: String, size: Int = 512, dark: Int = 0xFF14432A.toInt()): Bitmap {
    val matrix = QRCodeWriter().encode(
        contenido, BarcodeFormat.QR_CODE, size, size,
        mapOf(EncodeHintType.MARGIN to 1)
    )
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(x, y, if (matrix[x, y]) dark else 0xFFFFFFFF.toInt())
        }
    }
    return bmp
}
