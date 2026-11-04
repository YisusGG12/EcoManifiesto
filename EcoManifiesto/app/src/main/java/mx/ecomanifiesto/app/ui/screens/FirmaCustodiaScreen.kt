package mx.ecomanifiesto.app.ui.screens

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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.ecomanifiesto.app.data.Manifiesto
import mx.ecomanifiesto.app.data.Repo
import mx.ecomanifiesto.app.data.Session
import mx.ecomanifiesto.app.ui.BackTopBar
import mx.ecomanifiesto.app.ui.Caps
import mx.ecomanifiesto.app.ui.Chip
import mx.ecomanifiesto.app.ui.EcoCard
import mx.ecomanifiesto.app.ui.FooterNote
import mx.ecomanifiesto.app.ui.GreenChip
import mx.ecomanifiesto.app.ui.PrimaryButton
import mx.ecomanifiesto.app.ui.SignaturePad
import mx.ecomanifiesto.app.ui.SignatureState
import mx.ecomanifiesto.app.ui.cantidadTexto
import mx.ecomanifiesto.app.ui.fmtFecha
import mx.ecomanifiesto.app.ui.fmtHora
import mx.ecomanifiesto.app.ui.friendlyError
import mx.ecomanifiesto.app.ui.theme.Border
import mx.ecomanifiesto.app.ui.theme.CardWhite
import mx.ecomanifiesto.app.ui.theme.Danger
import mx.ecomanifiesto.app.ui.theme.DangerSoft
import mx.ecomanifiesto.app.ui.theme.FieldBg
import mx.ecomanifiesto.app.ui.theme.GreenSoft
import mx.ecomanifiesto.app.ui.theme.GreenText
import mx.ecomanifiesto.app.ui.theme.Primary
import mx.ecomanifiesto.app.ui.theme.TextMain
import mx.ecomanifiesto.app.ui.theme.TextMuted
import mx.ecomanifiesto.app.ui.theme.Warn
import mx.ecomanifiesto.app.ui.theme.WarnSoft

// pantalla del transportista: ve el ultimo manifiesto por firmar y firma para recibir la custodia
@Composable
fun FirmaCustodiaScreen(onSigned: (String) -> Unit, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var manifiesto by remember { mutableStateOf<Manifiesto?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }
    // estado de la firma
    val firma = remember { SignatureState() }
    // al subirle 1 se vuelve a ejecutar el LaunchedEffect (boton Actualizar)
    var recarga by remember { mutableStateOf(0) }

    // busca el manifiesto pendiente al abrir la pantalla
    LaunchedEffect(recarga) {
        cargando = true
        runCatching { Repo.ultimoPorFirmar() }
            .onSuccess { manifiesto = it; error = null }
            .onFailure { error = friendlyError(it) }
        cargando = false
    }

    val m = manifiesto
    Column(Modifier.fillMaxSize()) {
        BackTopBar(
            title = "Firma Custodia",
            subtitle = "EcoManifiesto",
            onBack = onLogout,
            chipText = "FOLIO OFICIAL",
            onLogout = onLogout
        )

        // hay 3 casos: cargando, no hay manifiestos o hay uno para firmar
        when {
            cargando -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }

            m == null -> Column(
                Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.Warning, null, tint = TextMuted, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(8.dp))
                Text("Sin manifiestos por firmar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextMain)
                Text(
                    error ?: "Cuando un generador cree un manifiesto aparecerá aquí.",
                    fontSize = 12.sp, color = if (error != null) Danger else TextMuted, textAlign = TextAlign.Center
                )
                TextButton(onClick = { recarga++ }) { Text("Actualizar", color = GreenText, fontWeight = FontWeight.Bold) }
            }

            else -> {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // barra verde de protocolo activo (decorativa)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GreenSoft)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Shield, null, tint = GreenText, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Caps("Protocolo de cadena RF-04", GreenText, 10, Modifier.weight(1f))
                        Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                        Spacer(Modifier.width(4.dp))
                        Caps("Activo", GreenText, 10)
                    }

                    // datos del manifiesto
                    EcoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Caps("Manifiesto oficial SEMARNAT", TextMuted, 9)
                                Text("#${m.folio}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                            }
                            Chip("POR ASIGNAR", WarnSoft, Warn, Color(0xFFFCD34D))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Warning, null, tint = Danger, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(m.residuo, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Chip("CRETIB: ${m.cretib.joinToString(" / ")}", DangerSoft, Danger, Color(0xFFFCA5A5))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Clase 9 - Peligrosidad Ambiental", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                            Text(cantidadTexto(m), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                        }
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = Border)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PartyBox("GENERADOR", m.generador, m.instalacion, Modifier.weight(1f))
                            PartyBox("TRANSPORTISTA", m.transportista, m.vehiculo + " • " + m.placas, Modifier.weight(1f))
                        }
                    }

                    // fecha y hora actual y nombre del operador (la hora se toma al abrir la pantalla, no se actualiza)
                    val ahora = remember { System.currentTimeMillis() }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        MetaChip(Icons.Outlined.CalendarToday, fmtFecha(ahora), Modifier)
                        MetaChip(Icons.Outlined.Schedule, "${fmtHora(ahora)} hrs", Modifier)
                        MetaChip(Icons.Outlined.Badge, Session.nombre.ifBlank { "Operador" }, Modifier.weight(1f))
                    }

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // indicador de pasos, son fijos y no cambian
                        Paso(1, "Inspección Física", hecho = true, activo = false)
                        Box(Modifier.weight(1f).height(1.dp).background(Border))
                        Paso(2, "Firma Chofer", hecho = false, activo = true)
                        Box(Modifier.weight(1f).height(1.dp).background(Border))
                        Paso(3, "Ruta", hecho = false, activo = false)
                    }

                    // cuadro para firmar con el dedo
                    EcoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Firma Digital del Operador", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                                Text("Firme aquí para asumir la custodia del residuo", fontSize = 11.sp, color = TextMuted)
                            }
                            TextButton(onClick = { firma.clear() }) {
                                Text("Limpiar", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        SignaturePad(
                            state = firma,
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            hint = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Outlined.Draw, null, tint = GreenText, modifier = Modifier.size(22.dp))
                                    Text("Trazo táctil habilitado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text("Dibuje su firma legal dentro de este recuadro", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Fingerprint, null, tint = GreenText, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cifrado biométrico de trazo", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1f))
                            Text(
                                if (firma.hasInk) "Firma capturada" else "Esperando firma",
                                fontSize = 10.sp, color = if (firma.hasInk) GreenText else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // aviso legal
                    EcoCard(bg = FieldBg) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Outlined.Gavel, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Al firmar, el operador asume formalmente la responsabilidad de custodia y transporte conforme a la NOM-002-SCT/2011 y LGPGIR. Esta acción genera un sello de tiempo inmutable.",
                                fontSize = 11.sp, color = TextMuted
                            )
                        }
                    }

                    // solo visual, todavia no se toman fotos de evidencia
                    EcoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.PhotoCamera, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Evidencia Fotográfica de Carga", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain, modifier = Modifier.weight(1f))
                            GreenChip("2/2 CAPTURADAS")
                        }
                    }

                    if (error != null) Text(error!!, color = Danger, fontSize = 12.sp)

                    // el boton solo se activa cuando ya dibujaron la firma, al guardar pasa a la pantalla de confirmacion
                    PrimaryButton(
                        text = "Firmar Custodia (EN TRÁNSITO)",
                        enabled = firma.hasInk,
                        loading = enviando,
                        onClick = {
                            enviando = true
                            scope.launch {
                                runCatching { Repo.firmarCustodia(m.folio, firma.serialize()) }
                                    .onSuccess { onSigned(m.folio) }
                                    .onFailure { error = friendlyError(it) }
                                enviando = false
                            }
                        }
                    )
                    FooterNote("DISPOSITIVO CERTIFICADO ECOMANIFIESTO MOBILE")
                }
            }
        }
    }
}

// cuadrito con titulo, nombre y detalle (generador / transportista)
@Composable
private fun PartyBox(titulo: String, nombre: String, detalle: String, modifier: Modifier) {
    Column(modifier) {
        Caps(titulo, TextMuted, 9)
        Text(nombre, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
        Text(detalle, fontSize = 10.sp, color = TextMuted)
    }
}

// chip chico con icono y texto
@Composable
private fun MetaChip(icon: ImageVector, texto: String, modifier: Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardWhite)
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMain, maxLines = 1)
    }
}

// circulo numerado de los pasos, verde si ya esta hecho o activo
@Composable
private fun Paso(n: Int, texto: String, hecho: Boolean, activo: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (hecho || activo) Primary else FieldBg)
                .border(1.dp, if (hecho || activo) Primary else Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (hecho) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
            else Text("$n", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activo) Color.White else TextMuted)
        }
        Spacer(Modifier.width(4.dp))
        Text(
            texto, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = if (hecho || activo) TextMain else TextMuted, maxLines = 1
        )
    }
}
