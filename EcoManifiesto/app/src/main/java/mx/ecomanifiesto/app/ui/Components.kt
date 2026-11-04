package mx.ecomanifiesto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.ecomanifiesto.app.ui.theme.Border
import mx.ecomanifiesto.app.ui.theme.CardWhite
import mx.ecomanifiesto.app.ui.theme.Danger
import mx.ecomanifiesto.app.ui.theme.DangerSoft
import mx.ecomanifiesto.app.ui.theme.FieldBg
import mx.ecomanifiesto.app.ui.theme.GreenSoft
import mx.ecomanifiesto.app.ui.theme.GreenSoftBorder
import mx.ecomanifiesto.app.ui.theme.GreenText
import mx.ecomanifiesto.app.ui.theme.Primary
import mx.ecomanifiesto.app.ui.theme.TextMain
import mx.ecomanifiesto.app.ui.theme.TextMuted
import mx.ecomanifiesto.app.ui.theme.Warn
import mx.ecomanifiesto.app.ui.theme.WarnSoft

// logo: cuadro verde con una hojita
@Composable
fun EcoLogo(size: Dp = 32.dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(Primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Outlined.Eco, null, tint = Color.White, modifier = Modifier.size(size * 0.6f))
    }
}

// circulo con icono de persona, se usa para abrir el menu de cerrar sesion (AvatarMenu)
@Composable
fun Avatar(onClick: () -> Unit = {}) {
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Outlined.Person, null, tint = Color.White, modifier = Modifier.size(17.dp))
    }
}

// texto chiquito en mayusculas, lo usamos para etiquetas
@Composable
fun Caps(text: String, color: Color = TextMuted, size: Int = 10, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        color = color,
        fontSize = size.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        modifier = modifier
    )
}

// titulo de seccion con un texto opcional a la derecha (ej. CRETIB ACTIVO)
@Composable
fun SectionLabel(text: String, trailing: String? = null, trailingColor: Color = Danger) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(trailing, color = trailingColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

// nota chica centrada que va al final de las pantallas
@Composable
fun FooterNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = TextMuted,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

// renglon de "etiqueta: valor"
@Composable
fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(label, color = TextMuted, fontSize = 12.sp, modifier = Modifier.width(110.dp))
        Text(
            value, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End, modifier = Modifier.weight(1f)
        )
    }
}

// etiqueta redondita de colores (estados, CRETIB, etc.), el borde y el icono son opcionales
@Composable
fun Chip(text: String, bg: Color, fg: Color, border: Color? = null, icon: ImageVector? = null) {
    var m = Modifier.clip(RoundedCornerShape(50)).background(bg)
    if (border != null) m = m.border(1.dp, border, RoundedCornerShape(50))
    Row(
        m.padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(11.dp))
        Text(text, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, maxLines = 1)
    }
}

// chip verde con los colores ya puestos
@Composable
fun GreenChip(text: String, icon: ImageVector? = null) =
    Chip(text, GreenSoft, GreenText, GreenSoftBorder, icon)

// la I (inflamable) va en amarillo y las demas en rojo
@Composable
fun CretibChip(letra: String) {
    val (bg, fg, br) = if (letra == "I") Triple(WarnSoft, Warn, Color(0xFFFCD34D))
    else Triple(DangerSoft, Danger, Color(0xFFFCA5A5))
    Chip(cretibLabel(letra), bg, fg, br)
}

// tarjeta blanca con borde redondeado, la reutilizamos en casi todas las pantallas
@Composable
fun EcoCard(
    modifier: Modifier = Modifier,
    bg: Color = CardWhite,
    borderColor: Color = Border,
    padding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(padding),
        content = content
    )
}

// cuadrito con un icono adentro (va a la izquierda de las tarjetas)
@Composable
fun IconBox(icon: ImageVector, bg: Color = GreenSoft, tint: Color = GreenText, size: Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(10.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

// base de las barras de arriba: una fila con fondo blanco y una linea abajo
@Composable
fun TopBarShell(content: @Composable RowScope.() -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().background(CardWhite).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
        HorizontalDivider(color = Border)
    }
}

// avatar con menu desplegable para cerrar sesion
@Composable
fun AvatarMenu(onLogout: (() -> Unit)?) {
    // controla si el menu esta abierto o cerrado
    var open by remember { mutableStateOf(false) }
    Box {
        Avatar { open = true }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text("Cerrar sesión") },
                onClick = { open = false; onLogout?.invoke() }
            )
        }
    }
}

// barra superior normal (logo, nombre de la app y rol)
@Composable
fun EcoTopBar(subtitle: String, onLogout: (() -> Unit)? = null, tag: String = "SEMARNAT") {
    TopBarShell {
        EcoLogo(30.dp)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text("EcoManifiesto", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TextMain)
            Text(subtitle.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreenText, letterSpacing = 0.6.sp)
        }
        GreenChip(tag)
        Spacer(Modifier.width(8.dp))
        AvatarMenu(onLogout)
    }
}

// barra superior con boton para regresar, se usa en las pantallas del transportista
@Composable
fun BackTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    chipText: String,
    chipBg: Color = GreenSoft,
    chipFg: Color = GreenText,
    chipBorder: Color? = GreenSoftBorder,
    onLogout: (() -> Unit)? = null
) {
    TopBarShell {
        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Atrás", tint = TextMain, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(6.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextMain, maxLines = 1)
            Text(subtitle.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreenText, letterSpacing = 0.6.sp)
        }
        Chip(chipText, chipBg, chipFg, chipBorder)
        Spacer(Modifier.width(8.dp))
        AvatarMenu(onLogout)
    }
}

// boton verde grande principal, si loading es true muestra el circulo de carga y se bloquea
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leading: ImageVector? = null,
    trailing: ImageVector? = Icons.AutoMirrored.Outlined.ArrowForward
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White,
            disabledContainerColor = Primary.copy(alpha = 0.35f),
            disabledContentColor = Color.White
        )
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            if (leading != null) {
                Icon(leading, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                Icon(trailing, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// boton mas chico, con outlined = true es el que solo tiene borde
@Composable
fun SoftButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = false
) {
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = CardWhite, contentColor = TextMain)
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenSoft, contentColor = GreenText)
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// campo de texto con titulo e icono, hidden = true se usa para la contraseña
@Composable
fun EcoField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    leading: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    hidden: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder, color = TextMuted.copy(alpha = 0.7f), fontSize = 14.sp) },
            leadingIcon = { Icon(leading, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = trailing,
            visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldBg,
                unfocusedContainerColor = FieldBg,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border
            )
        )
    }
}
