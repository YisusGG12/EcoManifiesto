package mx.ecomanifiesto.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import mx.ecomanifiesto.app.ui.theme.Border
import mx.ecomanifiesto.app.ui.theme.FieldBg
import mx.ecomanifiesto.app.ui.theme.TextMain
import java.util.Locale

// guarda los trazos de la firma, las coordenadas se normalizan (0 a 1)
// para poder redibujarlas en cualquier tamaño de pantalla
class SignatureState {
    // cada trazo es una lista de puntos (levantar el dedo = trazo nuevo)
    val strokes = mutableStateListOf<List<Offset>>()
    var size by mutableStateOf(IntSize.Zero)

    // true si ya dibujaron algo (minimo un trazo con 2 puntos)
    val hasInk: Boolean get() = strokes.any { it.size > 1 }

    fun clear() = strokes.clear()

    // pasa la firma a texto para guardarla en firestore
    // trazos separados por | y puntos por ;
    fun serialize(): String {
        val w = size.width.coerceAtLeast(1).toFloat()
        val h = size.height.coerceAtLeast(1).toFloat()
        return strokes.filter { it.size > 1 }.joinToString("|") { s ->
            s.joinToString(";") { String.format(Locale.US, "%.3f,%.3f", it.x / w, it.y / h) }
        }
    }
}

// lo contrario de serialize, del texto de firestore otra vez a puntos
fun parseTrazo(t: String): List<List<Offset>> =
    if (t.isBlank()) emptyList()
    else t.split("|").map { stroke ->
        stroke.split(";").mapNotNull { p ->
            val xy = p.split(",")
            if (xy.size == 2) {
                val x = xy[0].toFloatOrNull()
                val y = xy[1].toFloatOrNull()
                if (x != null && y != null) Offset(x, y) else null
            } else null
        }
    }

// dibuja un trazo uniendo sus puntos con lineas
private fun DrawScope.drawStroke(pts: List<Offset>, color: Color, width: Float) {
    if (pts.size < 2) return
    val path = Path()
    pts.forEachIndexed { i, o -> if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }
    drawPath(path, color, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

// el cuadro donde el usuario firma con el dedo
@Composable
fun SignaturePad(
    state: SignatureState,
    modifier: Modifier = Modifier,
    hint: @Composable () -> Unit = {}
) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(FieldBg.copy(alpha = 0.5f))
            // borde punteado
            .drawBehind {
                drawRoundRect(
                    color = Border,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                    )
                )
            }
            // guardamos el tamaño para poder normalizar los puntos
            .onSizeChanged { state.size = it }
            .pointerInput(Unit) {
                // al empezar a arrastrar crea un trazo nuevo y mientras mueve el dedo le agrega puntos
                detectDragGestures(
                    onDragStart = { o -> state.strokes.add(listOf(o)) },
                    onDrag = { change, _ ->
                        change.consume()
                        val i = state.strokes.lastIndex
                        if (i >= 0) state.strokes[i] = state.strokes[i] + change.position
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // si todavia no hay firma muestra el texto de ayuda
        if (state.strokes.isEmpty()) hint()
        Canvas(Modifier.fillMaxSize()) {
            val w = 2.5.dp.toPx()
            state.strokes.forEach { drawStroke(it, TextMain, w) }
        }
    }
}

// dibuja una firma ya guardada (por ejemplo la del receptor) escalada al tamaño del canvas
@Composable
fun TrazoView(trazo: String, modifier: Modifier = Modifier, color: Color = TextMain) {
    val strokes = remember(trazo) { parseTrazo(trazo) }
    Canvas(modifier) {
        val w = 2.5.dp.toPx()
        strokes.forEach { s ->
            drawStroke(s.map { Offset(it.x * size.width, it.y * size.height) }, color, w)
        }
    }
}
