package mx.ecomanifiesto.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.ecomanifiesto.app.data.Repo
import mx.ecomanifiesto.app.data.Rol
import mx.ecomanifiesto.app.data.Session
import mx.ecomanifiesto.app.ui.screens.ConfirmacionScreen
import mx.ecomanifiesto.app.ui.screens.DesktopScreen
import mx.ecomanifiesto.app.ui.screens.EscanerScreen
import mx.ecomanifiesto.app.ui.screens.FirmaCustodiaScreen
import mx.ecomanifiesto.app.ui.screens.LoginScreen
import mx.ecomanifiesto.app.ui.screens.NuevoManifiestoScreen
import mx.ecomanifiesto.app.ui.theme.Bg

// nombres de las rutas, los puse aqui para no andar escribiendo strings por todos lados
object Routes {
    const val LOGIN = "login"
    const val NUEVO = "nuevo"
    const val FIRMA = "firma"
    // esta ruta lleva el folio como argumento
    const val CONFIRMACION = "confirmacion/{folio}"
    const val ESCANER = "escaner"
    const val DESKTOP = "desktop"

    fun confirmacion(folio: String) = "confirmacion/$folio"
}

// segun el rol vemos a que pantalla lo mandamos despues del login
private fun inicioDe(rol: Rol): String = when (rol) {
    Rol.GENERADOR -> Routes.NUEVO
    Rol.TRANSPORTISTA -> Routes.FIRMA
    Rol.DESTINO_FINAL -> Routes.ESCANER
    Rol.ADMINISTRADOR -> Routes.DESKTOP
}

// navega a la pantalla del rol y quita el login del historial para que no regrese con el boton de atras
private fun NavHostController.irAlInicio(rol: Rol) {
    navigate(inicioDe(rol)) {
        popUpTo(Routes.LOGIN) { inclusive = true }
        launchSingleTop = true
    }
}

// cierra sesion en firebase y limpia todo el historial de pantallas
private fun NavHostController.cerrarSesion() {
    Repo.signOut()
    navigate(Routes.LOGIN) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun AppNav() {
    // controlador que maneja el cambio de pantallas
    val nav = rememberNavController()

    // si ya habia una sesion de firebase abierta nos saltamos el login
    LaunchedEffect(Unit) {
        Repo.restaurarSesion()?.let { nav.irAlInicio(it) }
    }

    // safeDrawingPadding para que no se encime con la barra de estado ni con el teclado
    Box(Modifier.fillMaxSize().background(Bg).safeDrawingPadding()) {
        NavHost(navController = nav, startDestination = Routes.LOGIN) {
            composable(Routes.LOGIN) {
                LoginScreen(onLoggedIn = { rol -> nav.irAlInicio(rol) })
            }
            composable(Routes.NUEVO) {
                NuevoManifiestoScreen(onLogout = { nav.cerrarSesion() })
            }
            composable(Routes.FIRMA) {
                FirmaCustodiaScreen(
                    onSigned = { folio -> nav.navigate(Routes.confirmacion(folio)) },
                    onLogout = { nav.cerrarSesion() }
                )
            }
            composable(
                Routes.CONFIRMACION,
                arguments = listOf(navArgument("folio") { type = NavType.StringType })
            ) { entry ->
                ConfirmacionScreen(
                    folio = entry.arguments?.getString("folio").orEmpty(),
                    onDone = {
                        nav.navigate(Routes.FIRMA) {
                            popUpTo(Routes.FIRMA) { inclusive = true }
                        }
                    },
                    onLogout = { nav.cerrarSesion() }
                )
            }
            composable(Routes.ESCANER) {
                EscanerScreen(onLogout = { nav.cerrarSesion() })
            }
            composable(Routes.DESKTOP) {
                DesktopScreen(
                    onLogout = { nav.cerrarSesion() },
                    // el admin puede probar el flujo del chofer sin cerrar sesion
                    // (solo se cambia el rol en Session, en firebase no se guarda)
                    onCambiarAChofer = {
                        Session.rol = Rol.TRANSPORTISTA
                        nav.navigate(Routes.FIRMA)
                    },
                    onEscanear = { nav.navigate(Routes.ESCANER) }
                )
            }
        }
    }
}
