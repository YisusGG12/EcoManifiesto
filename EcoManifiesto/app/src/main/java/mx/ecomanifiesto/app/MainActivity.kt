package mx.ecomanifiesto.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.ecomanifiesto.app.ui.theme.EcoTheme

// unica activity de la app, todo lo demas son pantallas hechas con Compose
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // para que la app se dibuje debajo de la barra de estado
        enableEdgeToEdge()
        setContent {
            // tema con los colores verdes del proyecto
            EcoTheme {
                // aqui empieza la navegacion entre pantallas
                AppNav()
            }
        }
    }
}
