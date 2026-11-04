package mx.ecomanifiesto.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// tema de la app, solo definimos los colores que usamos
@Composable
fun EcoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            onPrimary = Color.White,
            background = Bg,
            surface = CardWhite,
            onSurface = TextMain,
            outline = Border
        ),
        typography = Typography(),
        content = content
    )
}
