package mx.ecomanifiesto.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import mx.ecomanifiesto.app.data.Manifiesto
import java.io.File
import java.io.FileOutputStream

// arma el PDF del manifiesto con los 19 campos del formato oficial de SEMARNAT
// (generador 1-7, transportista 8-14 y destinatario 15-19), todo dibujado a mano con Canvas
object PdfExporter {

    // medidas de la hoja (A4 en puntos) y el margen de los lados
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MX = 28f
    // ancho util = 539
    private const val W = PAGE_W - 2 * MX

    // pinceles (Paint) para dibujar bordes, textos y bandas
    private val borde = Paint().apply {
        style = Paint.Style.STROKE; strokeWidth = 0.6f; color = 0xFF000000.toInt(); isAntiAlias = true
    }
    private val gris = Paint().apply { style = Paint.Style.FILL; color = 0xFFE4E4E4.toInt() }
    private val verdeBanda = Paint().apply { style = Paint.Style.FILL; color = 0xFF1B4D2E.toInt() }
    private val etiqueta = Paint().apply { textSize = 5.8f; color = 0xFF333333.toInt(); isAntiAlias = true }
    private val valor = Paint().apply {
        textSize = 7.6f; color = 0xFF000000.toInt(); isFakeBoldText = true; isAntiAlias = true
    }
    private val titulo = Paint().apply {
        textSize = 10f; color = 0xFF000000.toInt(); isFakeBoldText = true; isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val subtitulo = Paint().apply {
        textSize = 6.5f; color = 0xFF1B4D2E.toInt(); isAntiAlias = true; textAlign = Paint.Align.CENTER
    }
    private val texto = Paint().apply { textSize = 5.9f; color = 0xFF000000.toInt(); isAntiAlias = true }
    private val bandaTexto = Paint().apply {
        textSize = 7f; color = 0xFFFFFFFF.toInt(); isFakeBoldText = true; isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val centro = Paint().apply {
        textSize = 7.5f; color = 0xFF000000.toInt(); isFakeBoldText = true; isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val trazoPaint = Paint().apply {
        style = Paint.Style.STROKE; strokeWidth = 1.2f; color = 0xFF111111.toInt()
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; isAntiAlias = true
    }

    // recorta el texto con ... si no cabe en el ancho de la celda
    private fun fit(t: String, p: Paint, maxW: Float): String {
        if (p.measureText(t) <= maxW) return t
        var s = t
        while (s.length > 1 && p.measureText("$s…") > maxW) s = s.dropLast(1)
        return "$s…"
    }

    // parte un texto en varias lineas para que quepa en el ancho maximo
    private fun wrap(t: String, p: Paint, maxW: Float): List<String> {
        val out = mutableListOf<String>()
        var linea = ""
        for (palabra in t.split(" ")) {
            val prueba = if (linea.isEmpty()) palabra else "$linea $palabra"
            if (p.measureText(prueba) <= maxW) linea = prueba
            else { out.add(linea); linea = palabra }
        }
        if (linea.isNotEmpty()) out.add(linea)
        return out
    }

    // dibuja una celda con borde, la etiqueta chiquita arriba y el valor abajo
    private fun Canvas.celda(x: Float, y: Float, w: Float, h: Float, lbl: String, v: String = "") {
        drawRect(x, y, x + w, y + h, borde)
        drawText(fit(lbl, etiqueta, w - 4f), x + 2f, y + 7f, etiqueta)
        if (v.isNotBlank()) drawText(fit(v, valor, w - 6f), x + 3f, y + h - 4f, valor)
    }

    // banda verde con el titulo de cada bloque
    private fun Canvas.banda(y: Float, h: Float, t: String) {
        drawRect(MX, y, MX + W, y + h, verdeBanda)
        drawText(t, MX + W / 2, y + h - 3f, bandaTexto)
    }

    // las 3 filas de domicilio que se repiten en generador, transportista y destinatario
    // (solo se llena la calle, lo demas queda en blanco para llenarlo a mano)
    private fun Canvas.domicilio(y: Float, calle: String): Float {
        val h = 18f
        celda(MX, y, 90f, h, "Domicilio  Código postal:")
        celda(MX + 90f, y, 289f, h, "Calle:", calle)
        celda(MX + 379f, y, 80f, h, "Núm. Ext.:")
        celda(MX + 459f, y, 80f, h, "Núm. Int.:")
        celda(MX, y + h, 180f, h, "Colonia:")
        celda(MX + 180f, y + h, 200f, h, "Municipio o Delegación:")
        celda(MX + 380f, y + h, 159f, h, "Estado:")
        celda(MX, y + 2 * h, 180f, h, "Teléfono:")
        celda(MX + 180f, y + 2 * h, 359f, h, "Correo electrónico:")
        return y + 3 * h
    }

    // recuadro con el texto de "declaro bajo protesta de decir verdad" y su numero de campo
    private fun Canvas.declaracion(y: Float, numero: String, t: String): Float {
        val lineas = wrap("$numero- $t", texto, W - 6f)
        val h = 4f + lineas.size * 7.2f + 2f
        drawRect(MX, y, MX + W, y + h, borde)
        lineas.forEachIndexed { i, l -> drawText(l, MX + 3f, y + 8f + i * 7.2f, texto) }
        return y + h
    }

    // dibuja la firma (los trazos guardados) dentro de un rectangulo
    private fun Canvas.dibujarTrazo(trazo: String, x: Float, y: Float, w: Float, h: Float) {
        parseTrazo(trazo).forEach { s ->
            for (i in 1 until s.size) {
                drawLine(
                    x + s[i - 1].x * w, y + s[i - 1].y * h,
                    x + s[i].x * w, y + s[i].y * h, trazoPaint
                )
            }
        }
    }

    // fila de firma: nombre, firma dibujada, fecha y sello
    private fun Canvas.filaFirma(
        y: Float, nombre: String, trazo: String, fecha: String, sello: String
    ): Float {
        val h = 36f
        celda(MX, y, 300f, h, "Nombre y firma del responsable")
        if (nombre.isNotBlank()) drawText(fit(nombre, valor, 140f), MX + 3f, y + h - 4f, valor)
        if (trazo.isNotBlank()) dibujarTrazo(trazo, MX + 150f, y + 8f, 145f, h - 11f)
        celda(MX + 300f, y, 110f, h, "Fecha:", fecha)
        celda(MX + 410f, y, 129f, h, "Sello:")
        if (sello.isNotBlank()) {
            drawText("SELLO DIGITAL", MX + 410f + 129f / 2, y + 17f, centro)
            drawText(fit(sello, texto, 120f), MX + 410f + 4f, y + 27f, texto)
        }
        return y + h
    }

    // arma el pdf completo, lo guarda en cache/pdf y regresa el archivo
    fun generar(context: Context, m: Manifiesto): File {
        // una sola pagina
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        val c = page.canvas

        // y es la altura donde vamos dibujando, se va sumando conforme bajamos en la hoja
        var y = 24f
        c.drawText("MANIFIESTO DE ENTREGA, TRANSPORTE Y RECEPCIÓN DE RESIDUOS PELIGROSOS (GENERADOR)", PAGE_W / 2f, y, titulo)
        y += 9f
        c.drawText("Folio electrónico #${m.folio}  •  NOM-052-SEMARNAT-2005  •  Generado por EcoManifiesto", PAGE_W / 2f, y, subtitulo)
        y += 7f

        // bloque del generador (campos 1 al 7)
        c.banda(y, 11f, "DATOS DEL GENERADOR"); y += 11f
        val h = 20f
        c.celda(MX, y, 200f, h, "1- Núm. de registro ambiental:", m.registroAmbiental)
        c.celda(MX + 200f, y, 200f, h, "2- Núm. de manifiesto:", m.folio.substringAfterLast('-'))
        c.celda(MX + 400f, y, 139f, h, "3- Página:", "1/1")
        y += h
        c.celda(MX, y, W, h, "4- Nombre o razón social del generador:", m.generador)
        y += h
        y = c.domicilio(y, m.generadorDomicilio.ifBlank { m.instalacion })

        // campo 5: tabla de residuos
        c.drawRect(MX, y, MX + W, y + 11f, gris); c.drawRect(MX, y, MX + W, y + 11f, borde)
        c.drawText("5- Identificación de los residuos", MX + W / 2, y + 8f, centro)
        y += 11f
        // posicion en x de cada columna de la tabla
        val colsX = floatArrayOf(0f, 165f, 284f, 354f, 419f, 489f, 539f)
        val th = 20f
        c.celda(MX + colsX[0], y, 165f, th, "Nombre del residuo")
        // letras de la clasificacion, se marca con X las que tiene el residuo
        val letras = listOf("C", "R", "E", "T", "I", "B", "M")
        c.drawRect(MX + colsX[1], y, MX + colsX[2], y + th, borde)
        c.drawText("Clasificación", MX + colsX[1] + 3f, y + 7f, etiqueta)
        letras.forEachIndexed { i, l ->
            c.drawText(l, MX + colsX[1] + 8.5f + i * 17f, y + 17f, centro)
        }
        c.celda(MX + colsX[2], y, 70f, th, "Envase: Tipo")
        c.celda(MX + colsX[3], y, 65f, th, "Capacidad")
        c.celda(MX + colsX[4], y, 70f, th, "Cantidad (kg o ton)")
        c.celda(MX + colsX[5], y, 50f, th, "Etiqueta  Sí   No")
        y += th
        val rh = 24f
        c.celda(MX + colsX[0], y, 165f, rh, "", m.residuo)
        c.drawRect(MX + colsX[1], y, MX + colsX[2], y + rh, borde)
        letras.forEachIndexed { i, l ->
            val cx = MX + colsX[1] + i * 17f
            c.drawRect(cx, y, cx + 17f, y + rh, borde)
            if (l in m.cretib) c.drawText("X", cx + 8.5f, y + 15f, centro)
        }
        c.celda(MX + colsX[2], y, 70f, rh, "", m.envase)
        c.celda(MX + colsX[3], y, 65f, rh, "", m.capacidadEnvase)
        c.celda(MX + colsX[4], y, 70f, rh, "", cantidadTexto(m))
        c.drawRect(MX + colsX[5], y, MX + colsX[5] + 25f, y + rh, borde)
        c.drawRect(MX + colsX[5] + 25f, y, MX + colsX[6], y + rh, borde)
        // casillas Si / No de la etiqueta
        c.drawText(if (m.etiquetado) "X" else "", MX + colsX[5] + 12.5f, y + 15f, centro)
        c.drawText(if (!m.etiquetado) "X" else "", MX + colsX[5] + 37.5f, y + 15f, centro)
        y += rh

        c.celda(MX, y, W, 28f, "6- Instrucciones especiales e información adicional para el manejo seguro:")
        if (m.instrucciones.isNotBlank()) {
            wrap(m.instrucciones, valor, W - 6f).take(2).forEachIndexed { i, l ->
                c.drawText(l, MX + 3f, y + 15f + i * 8f, valor)
            }
        }
        y += 28f

        y = c.declaracion(
            y, "7",
            "Declaro bajo protesta de decir verdad que el contenido de este lote está total y correctamente descrito mediante el número de manifiesto, nombre del residuo, características CRETIB, debidamente envasado y etiquetado y que se han previsto las condiciones de seguridad para su transporte por vía terrestre de acuerdo con la legislación vigente."
        )
        // el generador no lleva firma dibujada, solo nombre, fecha y sello (primeros 16 caracteres del hash)
        y = c.filaFirma(y, m.generadorFirmante, "", fmtFecha(m.creadoEn), m.hash.take(16).uppercase())

        // bloque del transportista (campos 8 al 14)
        c.banda(y, 11f, "DATOS DEL TRANSPORTISTA"); y += 11f
        c.celda(MX, y, W, h, "8- Nombre o razón social del transportista:", m.transportista)
        y += h
        y = c.domicilio(y, "")
        c.celda(MX, y, 270f, h, "9- Núm. de autorización de la SEMARNAT:", m.autorizacionTransportista)
        c.celda(MX + 270f, y, 269f, h, "10- Núm. de permiso S.C.T.:", m.permisoSct)
        y += h
        c.celda(MX, y, 270f, h, "11- Tipo de vehículo:", m.tipoVehiculo.ifBlank { m.vehiculo })
        c.celda(MX + 270f, y, 269f, h, "12- Núm. de placa:", m.placas)
        y += h
        c.celda(MX, y, W, h, "13- Ruta de la empresa generadora hasta su entrega:", m.ruta)
        y += h
        y = c.declaracion(
            y, "14",
            "Declaro bajo protesta de decir verdad que recibí los residuos peligrosos descritos en el manifiesto para su transporte a la empresa destinataria señalada por el generador."
        )
        y = c.filaFirma(
            y,
            // nombre de quien firmo, si no hay usa el del conductor (solo si ya esta en transito)
            m.transportistaFirmante.ifBlank { if (m.enTransitoEn > 0) m.conductor else "" },
            m.firmaTransportista,
            if (m.enTransitoEn > 0) fmtFecha(m.enTransitoEn) else "",
            ""
        )

        // bloque del destinatario (campos 15 al 19)
        c.banda(y, 11f, "DATOS DE LA EMPRESA DESTINATARIA"); y += 11f
        c.celda(MX, y, W, h, "15- Nombre o razón social del destinatario:", m.destinatario)
        y += h
        y = c.domicilio(y, m.destinoDomicilio)
        c.celda(MX, y, W, h, "16- Núm. autorización de la SEMARNAT:", m.autorizacionDestinatario)
        y += h
        // el campo 17 junta nombre y cargo
        val recibe = listOf(m.receptorNombre, m.receptorCargo).filter { it.isNotBlank() }.joinToString(" – ")
        c.celda(MX, y, W, h, "17- Nombre y cargo de la persona que recibe los residuos:", recibe)
        y += h
        c.celda(MX, y, W, h, "18- Observaciones:", m.observaciones)
        y += h
        y = c.declaracion(
            y, "19",
            "Declaro bajo protesta de decir verdad que recibí los residuos peligrosos descritos en el manifiesto."
        )
        y = c.filaFirma(
            y, m.receptorNombre, m.firmaReceptor,
            if (m.entregadoEn > 0) fmtFecha(m.entregadoEn) else "",
            m.hashEntrega.take(16).uppercase()
        )

        y += 6f
        // al final el QR con el folio y las huellas hash
        val qrSize = 54
        c.drawBitmap(qrBitmap(m.folio, 220), null, android.graphics.RectF(MX + W - qrSize, y, MX + W, y + qrSize), null)
        c.drawText("Estado del manifiesto: ${m.estado.replace('_', ' ')}", MX, y + 8f, texto)
        c.drawText("Huella SHA-256 (creación): ${m.hash.take(64)}", MX, y + 17f, texto)
        if (m.hashEntrega.isNotBlank()) {
            c.drawText("Huella SHA-256 (entrega): ${m.hashEntrega.take(64)}", MX, y + 26f, texto)
        }
        c.drawText(
            "Documento generado electrónicamente por EcoManifiesto con la estructura del formato oficial de SEMARNAT.",
            MX, y + 38f, texto
        )

        // guardamos el archivo en la carpeta cache
        doc.finishPage(page)
        val dir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val file = File(dir, "${m.folio}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // abre el menu de compartir/imprimir de android con el pdf
    // se usa FileProvider porque no se puede pasar el archivo directo a otra app (ver file_paths.xml)
    fun compartir(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir / Imprimir"))
    }
}
