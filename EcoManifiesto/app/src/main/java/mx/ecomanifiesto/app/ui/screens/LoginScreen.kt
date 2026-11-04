package mx.ecomanifiesto.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.ecomanifiesto.app.data.Repo
import mx.ecomanifiesto.app.data.Rol
import mx.ecomanifiesto.app.ui.Caps
import mx.ecomanifiesto.app.ui.EcoCard
import mx.ecomanifiesto.app.ui.EcoField
import mx.ecomanifiesto.app.ui.EcoLogo
import mx.ecomanifiesto.app.ui.FooterNote
import mx.ecomanifiesto.app.ui.PrimaryButton
import mx.ecomanifiesto.app.ui.friendlyError
import mx.ecomanifiesto.app.ui.theme.Border
import mx.ecomanifiesto.app.ui.theme.CardWhite
import mx.ecomanifiesto.app.ui.theme.Danger
import mx.ecomanifiesto.app.ui.theme.GreenSoft
import mx.ecomanifiesto.app.ui.theme.GreenSoftBorder
import mx.ecomanifiesto.app.ui.theme.GreenText
import mx.ecomanifiesto.app.ui.theme.Primary
import mx.ecomanifiesto.app.ui.theme.TextMain
import mx.ecomanifiesto.app.ui.theme.TextMuted

// icono de cada rol
private fun Rol.icono(): ImageVector = when (this) {
    Rol.GENERADOR -> Icons.Outlined.Factory
    Rol.TRANSPORTISTA -> Icons.Outlined.LocalShipping
    Rol.DESTINO_FINAL -> Icons.Outlined.Business
    Rol.ADMINISTRADOR -> Icons.Outlined.AdminPanelSettings
}

// pantalla de inicio de sesion: se elige el rol y se escribe correo y contraseña
@Composable
fun LoginScreen(onLoggedIn: (Rol) -> Unit) {
    // estados de la pantalla: rol elegido, campos de texto, si esta cargando y el mensaje de error
    var rol by remember { mutableStateOf(Rol.GENERADOR) }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var verPass by remember { mutableStateOf(false) }
    var recordar by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardWhite)
                .border(1.dp, Border, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) { EcoLogo(52.dp) }

        Spacer(Modifier.height(14.dp))
        Text("EcoManifiesto Mobile", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
        Text(
            "Gestión y Trazabilidad de Residuos Peligrosos",
            fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(18.dp))
        // banner verde de cumplimiento normativo (es texto fijo)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GreenSoft)
                .border(1.dp, GreenSoftBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = GreenText, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Caps("Acreditación oficial", GreenText, 9)
                Text("Cumplimiento Normativo SEMARNAT & LGPGIR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenText)
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "Selecciona tu Rol Operativo",
            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        // los roles se muestran en 2 columnas
        val roles = Rol.entries
        for (fila in roles.chunked(2)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { r ->
                    RoleButton(r, r == rol, Modifier.weight(1f)) { rol = r }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))
        EcoCard(padding = 16.dp) {
            EcoField(
                label = "Correo Electrónico Corporativo",
                value = email,
                onChange = { email = it },
                placeholder = "correo@empresa.com",
                leading = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            EcoField(
                label = "Contraseña de Acceso",
                value = pass,
                onChange = { pass = it },
                placeholder = "••••••••••",
                leading = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                hidden = !verPass,
                trailing = {
                    IconButton(onClick = { verPass = !verPass }) {
                        Icon(
                            if (verPass) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            "Mostrar contraseña", tint = TextMuted, modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // el checkbox de recordar sesion todavia no hace nada
                // (firebase ya mantiene la sesion abierta por su cuenta)
                Checkbox(
                    checked = recordar,
                    onCheckedChange = { recordar = it },
                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                )
                Text("Recordar sesión", fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(1f))
                Text(
                    "Recuperar contraseña",
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenText,
                    // todavia no hay recuperacion de contraseña, solo muestra un aviso
                    modifier = Modifier.clickable {
                        error = "Contacta al administrador de tu planta para restablecer la contraseña."
                    }
                )
            }
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(error!!, color = Danger, fontSize = 12.sp)
            }
            Spacer(Modifier.height(14.dp))
            PrimaryButton(
                text = "Iniciar Sesión",
                loading = loading,
                enabled = email.isNotBlank() && pass.isNotEmpty(),
                onClick = {
                    error = null
                    loading = true
                    // el login se hace en una corrutina para no congelar la pantalla
                    scope.launch {
                        Repo.signIn(email, pass, rol)
                            .onSuccess { onLoggedIn(rol) }
                            .onFailure { error = friendlyError(it) }
                        loading = false
                    }
                }
            )
        }

        Spacer(Modifier.height(20.dp))
        FooterNote("🔒 CONEXIÓN SEGURA SSL")
        Text(
            "(256-bit Encriptación Grado Industrial)",
            fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// boton de cada rol, cambia de color cuando esta seleccionado
@Composable
private fun RoleButton(rol: Rol, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = if (selected) Primary else CardWhite
    val fg = if (selected) Color.White else TextMain
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, if (selected) Primary else Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(rol.icono(), null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(rol.label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
