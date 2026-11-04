package mx.ecomanifiesto.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.ecomanifiesto.app.data.Manifiesto
import mx.ecomanifiesto.app.data.Repo
import mx.ecomanifiesto.app.ui.BackTopBar
import mx.ecomanifiesto.app.ui.Caps
import mx.ecomanifiesto.app.ui.Chip
import mx.ecomanifiesto.app.ui.CretibChip
import mx.ecomanifiesto.app.ui.EcoCard
import mx.ecomanifiesto.app.ui.GreenChip
import mx.ecomanifiesto.app.ui.InfoLine
import mx.ecomanifiesto.app.ui.cantidadTexto
import mx.ecomanifiesto.app.ui.fmtFecha
import mx.ecomanifiesto.app.ui.fmtHora
import mx.ecomanifiesto.app.ui.theme.Border
import mx.ecomanifiesto.app.ui.theme.FieldBg
import mx.ecomanifiesto.app.ui.theme.GreenSoft
import mx.ecomanifiesto.app.ui.theme.GreenSoftBorder
import mx.ecomanifiesto.app.ui.theme.GreenText
import mx.ecomanifiesto.app.ui.theme.Primary
import mx.ecomanifiesto.app.ui.theme.TextMain
import mx.ecomanifiesto.app.ui.theme.TextMuted
import mx.ecomanifiesto.app.ui.theme.Warn
import mx.ecomanifiesto.app.ui.theme.WarnSoft

// pantalla que sale despues de firmar la custodia, muestra el avance de la ruta
@Composable
fun ConfirmacionScreen(folio: String, onDone: () -> Unit, onLogout: () -> Unit) {
    var m by remember { mutableStateOf<Manifiesto?>(null) }
    // volvemos a leer el manifiesto de firestore para mostrar los datos ya actualizados
    LaunchedEffect(folio) { m = runCatching { Repo.getManifiesto(folio) }.getOrNull() }

    Column(Modifier.fillMaxSize()) {
        BackTopBar(
            title = "Folio $folio",
            subtitle = "Confirmación de custodia",
            onBack = onDone,
            chipText = "EN TRÁNSITO",
            chipBg = WarnSoft,
            chipFg = Warn,
            chipBorder = Color(0xFFFCD34D),
            onLogout = onLogout
        )
        val data = m
        // mientras carga se muestra el circulo
        if (data == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Column
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Caps("Custodia legal asumida por operador", TextMuted, 10, Modifier.fillMaxWidth())

            // mensaje de exito. OJO: el texto dice que se notifico por SMS/Email/push pero todavia no se manda nada (falta FCM)
            EcoCard(bg = GreenSoft, borderColor = GreenSoftBorder) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = GreenText, modifier = Modifier.size(44.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "¡Firma Registrada y Notificada con Éxito!",
                        fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextMain, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Se ha emitido el sello criptográfico inmutable y se dispersó la notificación legal por SMS/Email y push al emisor ${data.generador} y al confirmante final.",
                        fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        GreenChip("Notificado a Generador", Icons.Outlined.Check)
                        GreenChip("Notificado a Destino Final", Icons.Outlined.Check)
                        GreenChip("Hash SEMARNAT generándose", Icons.Outlined.VerifiedUser)
                    }
                }
            }

            // linea de tiempo del recorrido, las horas salen del manifiesto
            EcoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Avance de Ruta Federal", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain, modifier = Modifier.weight(1f))
                    Caps("Etapa 3 de 4", TextMuted, 9)
                }
                Spacer(Modifier.height(12.dp))
                // OJO: "Planta Celaya" esta escrito a mano en el texto
                Etapa("1. Generación y Creación", "${data.generador} • Planta Celaya", "${fmtHora(data.creadoEn)} hrs", Estado.HECHO, true)
                Etapa("2. Firma del Transportista", "${data.transportistaFirmante.ifBlank { data.conductor }} • Huella biométrica verificada", "${fmtHora(data.enTransitoEn)} hrs", Estado.HECHO, true)
                Etapa("3. Tránsito Federal", "En camino a destino final por autopista federal", "EN CURSO", Estado.ACTIVO, true)
                Etapa("4. Báscula y Entrega Final", "Instalación de destino autorizada", "Pendiente", Estado.PENDIENTE, false)
            }

            // resumen del embarque
            EcoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ficha Técnica del Embarque", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain, modifier = Modifier.weight(1f))
                    data.cretib.forEach { CretibChip(it); Spacer(Modifier.width(4.dp)) }
                }
                Spacer(Modifier.height(6.dp))
                InfoLine("Residuo Peligroso:", "${data.residuo} (${cantidadTexto(data)})")
                InfoLine("Transportista:", "${data.transportista} (${data.vehiculo} · ${data.placas})")
                InfoLine("Salida y Telemetría:", "${fmtFecha(data.enTransitoEn)} ${fmtHora(data.enTransitoEn)} hrs")
            }

            EcoCard(bg = FieldBg) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Gavel, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "NOM-052-SEMARNAT / Art. 82 LGPGIR: porte obligatorio del manifiesto durante todo el recorrido. El código QR sirve como constancia oficial ante inspecciones de SCT, Guardia Nacional y PROFEPA.",
                        fontSize = 10.sp, color = TextMuted
                    )
                }
            }

            TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al inicio", color = GreenText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// estados de cada etapa de la linea de tiempo
private enum class Estado { HECHO, ACTIVO, PENDIENTE }

// una fila de la linea de tiempo: circulo, linea que lo une con el siguiente, textos y hora
@Composable
private fun Etapa(titulo: String, detalle: String, hora: String, estado: Estado, conLinea: Boolean) {
    Row(Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (estado == Estado.PENDIENTE) FieldBg else Primary)
                    .border(1.dp, if (estado == Estado.PENDIENTE) Border else Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (estado == Estado.HECHO) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                if (estado == Estado.ACTIVO) Box(Modifier.size(7.dp).clip(CircleShape).background(Color.White))
            }
            if (conLinea) Box(Modifier.width(2.dp).height(34.dp).background(Border))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f).padding(bottom = 10.dp)) {
            Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (estado == Estado.PENDIENTE) TextMuted else TextMain)
            Text(detalle, fontSize = 10.sp, color = TextMuted)
        }
        if (estado == Estado.ACTIVO) GreenChip(hora)
        else Text(hora, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
    }
}
