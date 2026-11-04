package mx.ecomanifiesto.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import mx.ecomanifiesto.app.ui.Caps
import mx.ecomanifiesto.app.ui.Chip
import mx.ecomanifiesto.app.ui.EcoCard
import mx.ecomanifiesto.app.ui.EcoTopBar
import mx.ecomanifiesto.app.ui.FooterNote
import mx.ecomanifiesto.app.ui.GreenChip
import mx.ecomanifiesto.app.ui.PrimaryButton
import mx.ecomanifiesto.app.ui.SoftButton
import mx.ecomanifiesto.app.ui.friendlyError
import mx.ecomanifiesto.app.ui.qrBitmap
import mx.ecomanifiesto.app.data.Repo
import mx.ecomanifiesto.app.ui.theme.Border
import mx.ecomanifiesto.app.ui.theme.CardWhite
import mx.ecomanifiesto.app.ui.theme.Danger
import mx.ecomanifiesto.app.ui.theme.FieldBg
import mx.ecomanifiesto.app.ui.theme.GreenSoft
import mx.ecomanifiesto.app.ui.theme.GreenSoftBorder
import mx.ecomanifiesto.app.ui.theme.GreenText
import mx.ecomanifiesto.app.ui.theme.Primary
import mx.ecomanifiesto.app.ui.theme.TextMain
import mx.ecomanifiesto.app.ui.theme.TextMuted
import mx.ecomanifiesto.app.ui.theme.Warn
import mx.ecomanifiesto.app.ui.theme.WarnSoft

// pantalla del administrador: muestra un QR para entrar a la pagina web desde la computadora
@Composable
fun DesktopScreen(onLogout: () -> Unit, onCambiarAChofer: () -> Unit, onEscanear: () -> Unit) {
    val ctx = LocalContext.current
    var enlace by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    // segundos que le quedan al token (5 minutos = 300 s)
    var segundos by remember { mutableIntStateOf(299) }
    // cuando sube se genera un token nuevo
    var ciclo by remember { mutableIntStateOf(0) }

    // crea el token al abrir la pantalla y cada vez que expira
    LaunchedEffect(ciclo) {
        enlace = null
        error = null
        runCatching { Repo.crearTokenHandoff() }
            .onSuccess { enlace = it }
            .onFailure { error = friendlyError(it) }
        segundos = 299
        // cuenta regresiva, una vuelta por segundo
        while (segundos > 0) {
            delay(1000)
            segundos--
        }
        // ya expiro, el ciclo++ vuelve a lanzar el LaunchedEffect
        ciclo++
    }

    Column(Modifier.fillMaxSize()) {
        EcoTopBar("DESKTOP HANDOFF", onLogout)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Chip("ACCESO REGULADO", GreenSoft, GreenText, GreenSoftBorder)
            }
            Column {
                Text("Portal Web Obligatorio • NOM-052", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                Text("Acceso Administrativo en PC", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                Text(
                    "El panel regulatorio requiere pantalla de escritorio para la validación criptográfica e.firma (SAT), exportación de libros COA y auditoría SEMARNAT.",
                    fontSize = 12.sp, color = TextMuted
                )
            }

            EcoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vinculación Instantánea", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextMain, modifier = Modifier.weight(1f))
                    GreenChip("TLS 1.3 SEGURO")
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val url = enlace
                        if (url != null) {
                            // el QR lleva la url con el token
                            val bmp = remember(url) { qrBitmap(url).asImageBitmap() }
                            Image(bmp, "QR de acceso a PC", Modifier.fillMaxSize())
                        } else if (error != null) {
                            Text(error!!, color = Danger, fontSize = 11.sp, textAlign = TextAlign.Center)
                        } else {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Chip(
                        "Válido por: %02d:%02d min".format(segundos / 60, segundos % 60),
                        FieldBg, TextMain, Border
                    )
                }
                Spacer(Modifier.height(12.dp))
                Paso(1, "Entra a app.ecomanifiesto.mx desde el navegador de tu computadora.")
                Paso(2, "Selecciona \"Acceso Rápido Móvil\" o escanea el monitor con el botón inferior.")
            }

            PrimaryButton(
                text = "Escanear QR de mi Computadora",
                leading = Icons.Outlined.QrCodeScanner,
                trailing = null,
                onClick = onEscanear
            )
            SoftButton(
                text = "Enviar enlace de acceso a mi correo",
                icon = Icons.Outlined.Email,
                modifier = Modifier.fillMaxWidth(),
                outlined = true,
                onClick = {
                    // abre la app de correo con el enlace ya escrito
                    enlace?.let { url ->
                        val i = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_SUBJECT, "Acceso EcoManifiesto en PC")
                            putExtra(Intent.EXTRA_TEXT, "Abre este enlace en tu computadora (válido 5 minutos):\n$url")
                        }
                        ctx.startActivity(Intent.createChooser(i, "Enviar enlace"))
                    }
                }
            )

            // tarjeta para que el admin pueda probar el flujo del chofer
            EcoCard(bg = WarnSoft, borderColor = Color(0xFFFCD34D)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocalShipping, null, tint = Warn, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("¿Necesitas operar en campo?", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                        Text(
                            "Si eres chofer, custodio o encargado de almacén de acopio, cambia tu rol para generar firmas digitales en ruta y cotejar folios CRETIB.",
                            fontSize = 10.sp, color = TextMuted
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                SoftButton("Cambiar a Chofer", Icons.Outlined.LocalShipping, onCambiarAChofer, Modifier.fillMaxWidth())
            }

            FooterNote(
                "Por lineamientos de la Cédula de Operación Anual (COA), la asignación masiva de permisos y firma de manifiestos matriz permanece inhabilitada en clientes móviles."
            )
        }
    }
}

// paso numerado con circulo verde
@Composable
private fun Paso(n: Int, texto: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(20.dp).clip(CircleShape).background(Primary),
            contentAlignment = Alignment.Center
        ) { Text("$n", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(8.dp))
        Text(texto, fontSize = 11.sp, color = TextMain, modifier = Modifier.weight(1f))
    }
}
