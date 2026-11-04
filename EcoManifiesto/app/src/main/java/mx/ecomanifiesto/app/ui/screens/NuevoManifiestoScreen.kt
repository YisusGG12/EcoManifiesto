package mx.ecomanifiesto.app.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.ecomanifiesto.app.data.CAPACIDAD_MAXIMA_L
import mx.ecomanifiesto.app.data.catalogosPorDefecto
import mx.ecomanifiesto.app.data.Manifiesto
import mx.ecomanifiesto.app.data.RESIDUOS
import mx.ecomanifiesto.app.data.Repo
import mx.ecomanifiesto.app.data.Session
import mx.ecomanifiesto.app.data.TRANSPORTISTAS
import mx.ecomanifiesto.app.ui.Caps
import mx.ecomanifiesto.app.ui.Chip
import mx.ecomanifiesto.app.ui.CretibChip
import mx.ecomanifiesto.app.ui.EcoCard
import mx.ecomanifiesto.app.ui.EcoTopBar
import mx.ecomanifiesto.app.ui.FooterNote
import mx.ecomanifiesto.app.ui.IconBox
import mx.ecomanifiesto.app.ui.InfoLine
import mx.ecomanifiesto.app.ui.PdfExporter
import mx.ecomanifiesto.app.ui.PrimaryButton
import mx.ecomanifiesto.app.ui.SectionLabel
import mx.ecomanifiesto.app.ui.SoftButton
import mx.ecomanifiesto.app.ui.cantidadTexto
import mx.ecomanifiesto.app.ui.fmtFecha
import mx.ecomanifiesto.app.ui.friendlyError
import mx.ecomanifiesto.app.ui.qrBitmap
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

// pantalla del generador: elige residuo, cantidad y transportista y genera el manifiesto con su QR
@Composable
fun NuevoManifiestoScreen(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // indices de lo que esta seleccionado en los catalogos
    var resIdx by remember { mutableIntStateOf(0) }
    var menuResiduo by remember { mutableStateOf(false) }
    var cantidad by remember { mutableStateOf("500") }
    var unidad by remember { mutableStateOf("L") }
    var trIdx by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var creado by remember { mutableStateOf<Manifiesto?>(null) }

    // empieza con los datos de ejemplo y cuando firestore responde se cambian por los reales
    var cat by remember { mutableStateOf(catalogosPorDefecto()) }
    LaunchedEffect(Unit) { cat = Repo.cargarCatalogos() }

    // coerceIn por si la lista cambia de tamaño al cargar y el indice se sale
    val res = cat.residuos[resIdx.coerceIn(0, cat.residuos.lastIndex)]
    val tr = cat.transportistas[trIdx.coerceIn(0, cat.transportistas.lastIndex)]
    // por ahora siempre se usa el primer destinatario
    val dest = cat.destinos.first()

    Column(Modifier.fillMaxSize()) {
        EcoTopBar("GENERADOR", onLogout)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // barra de progreso (paso 1 de 2, es fija en 50%)
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Caps("Paso 1 de 2: Registro de residuo", Primary, 10, Modifier.weight(1f))
                    Text("50%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0.5f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                    color = Primary,
                    trackColor = Border
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Shield, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Protocolo Oficial SEMARNAT (NOM-052 / NOM-053)", fontSize = 10.sp, color = TextMuted)
                }
            }

            Column {
                Text("Nuevo Manifiesto", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                Text(
                    "Entrega, Transporte y Destino Final de Residuos Peligrosos",
                    fontSize = 13.sp, color = TextMuted
                )
            }

            // tarjeta de verificacion (RF-03), por ahora es texto fijo, todavia no revisa fechas de vigencia
            EcoCard(bg = GreenSoft, borderColor = GreenSoftBorder) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = GreenText, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Caps("Verificación cumplida • RF-03", GreenText, 9)
                        Text("Autorización SEMARNAT Vigente", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                        Text("No. Registro: ${Session.registro}", fontSize = 11.sp, color = GreenText)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // selector de tipo de residuo (dropdown)
                SectionLabel("Tipo de Residuo Peligroso", "CRETIB ACTIVO")
                Box {
                    EcoCard(Modifier.clickable { menuResiduo = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBox(Icons.Outlined.Opacity, DangerSoft, Danger)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(res.nombre, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                                Text(res.detalle, fontSize = 11.sp, color = TextMuted)
                            }
                            Icon(Icons.Outlined.KeyboardArrowDown, null, tint = TextMuted)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                res.cretib.forEach { CretibChip(it) }
                            }
                            Text("Clave: ${res.clave}", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                    DropdownMenu(expanded = menuResiduo, onDismissRequest = { menuResiduo = false }) {
                        cat.residuos.forEachIndexed { i, r ->
                            DropdownMenuItem(
                                text = { Text(r.nombre) },
                                onClick = { resIdx = i; menuResiduo = false }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // cantidad y unidad
                SectionLabel("Cantidad Estimada para Retiro")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardWhite)
                        .border(1.dp, Border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Inventory2, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = cantidad,
                        // solo deja numeros y punto, maximo 8 caracteres
                        onValueChange = { v -> cantidad = v.filter { it.isDigit() || it == '.' }.take(8) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextMain),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FieldBg)
                            .padding(3.dp)
                    ) {
                        // botones para cambiar entre L y Kg
                        listOf("L", "Kg").forEach { u ->
                            val sel = u == unidad
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) Primary else Color.Transparent)
                                    .clickable { unidad = u }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    u, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = if (sel) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }
                Text(
                    "Capacidad máxima autorizada en bitácora de almacén: 1,200 L",
                    fontSize = 10.sp, color = TextMuted
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // transportista, el boton de las flechas pasa al siguiente de la lista
                SectionLabel("Empresa Transportista Certificada")
                EcoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBox(Icons.Outlined.LocalShipping)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(tr.empresa, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.VerifiedUser, null, tint = GreenText, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Permiso SCT / SEMARNAT Activo", fontSize = 11.sp, color = GreenText)
                            }
                        }
                        // el % hace que al llegar al ultimo regrese al primero
                        IconButton(onClick = { trIdx = (trIdx + 1) % cat.transportistas.size }) {
                            Icon(Icons.Outlined.SwapHoriz, "Cambiar transportista", tint = TextMuted)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // vehiculo y conductor del transportista elegido
                SectionLabel("Vehículo Asignado y Placas Federales")
                EcoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBox(Icons.Outlined.DirectionsCar, FieldBg, TextMain)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "${tr.vehiculo} / Placas: ${tr.placas}",
                                fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain
                            )
                            Text("Conductor certificado: ${tr.conductor}", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // datos de la planta del generador (vienen de Session)
                SectionLabel("Instalación Generadora")
                EcoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBox(Icons.Outlined.Business, FieldBg, TextMain)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(Session.instalacion, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                            Text(Session.direccion, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            EcoCard(bg = FieldBg) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Shield, null, tint = GreenText, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        // aviso de protesta de decir verdad
                        "Al generar el manifiesto digital, usted certifica bajo protesta de decir verdad que los residuos corresponden a las características CRETIB declaradas.",
                        fontSize = 11.sp, color = TextMuted
                    )
                }
            }

            if (error != null) Text(error!!, color = Danger, fontSize = 12.sp)

            // validamos la cantidad antes de guardar: que sea numero mayor a 0 y que no pase de 1,200 L
            PrimaryButton(
                text = "Generar Manifiesto y Código QR",
                loading = loading,
                leading = Icons.Outlined.QrCode2,
                trailing = null,
                onClick = {
                    val c = cantidad.toDoubleOrNull()
                    when {
                        c == null || c <= 0.0 -> error = "Captura una cantidad válida."
                        unidad == "L" && c > CAPACIDAD_MAXIMA_L ->
                            error = "La cantidad excede la capacidad máxima autorizada (1,200 L)."
                        else -> {
                            error = null
                            loading = true
                            scope.launch {
                                runCatching {
                                    // los datos de la empresa se copian dentro del manifiesto, asi no cambian aunque despues editen el catalogo
                                    Repo.crearManifiesto(
                                        Manifiesto(
                                            residuo = res.nombre,
                                            residuoDetalle = res.detalle,
                                            claveResiduo = res.clave,
                                            cretib = res.cretib,
                                            cantidad = c,
                                            unidad = unidad,
                                            generador = Session.empresa,
                                            instalacion = Session.instalacion,
                                            transportista = tr.empresa,
                                            vehiculo = tr.vehiculo,
                                            placas = tr.placas,
                                            conductor = tr.conductor,
                                            registroAmbiental = Session.registro,
                                            generadorFirmante = Session.nombre,
                                            generadorDomicilio = Session.direccion,
                                            envase = res.envase,
                                            capacidadEnvase = res.capacidad,
                                            etiquetado = true,
                                            instrucciones = res.instrucciones,
                                            tipoVehiculo = tr.tipoVehiculo,
                                            autorizacionTransportista = tr.autorizacion,
                                            permisoSct = tr.permisoSct,
                                            // la ruta esta fija por ahora (sale de Toluca)
                                            ruta = "Toluca, Edo. Méx. - Autopista federal - ${dest.domicilio}",
                                            destinatario = dest.empresa,
                                            destinoDomicilio = dest.domicilio,
                                            autorizacionDestinatario = dest.autorizacion
                                        )
                                    )
                                }.onSuccess { creado = it }
                                    .onFailure { error = friendlyError(it) }
                                loading = false
                            }
                        }
                    }
                }
            )

            // cuando ya se creo muestra la tarjeta con el folio y el QR
            creado?.let { FolioCard(it) }

            Text(
                "¿Tienes dudas sobre los códigos CRETIB? Consulta el manual técnico",
                fontSize = 11.sp, color = GreenText, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri("https://www.gob.mx/semarnat") }
                    .padding(vertical = 6.dp)
            )
        }
    }
}

// tarjeta que sale despues de crear el manifiesto: folio, QR y botones de compartir e imprimir
@Composable
private fun FolioCard(m: Manifiesto) {
    val ctx = LocalContext.current
    // el QR solo lleva el folio, el destino lo escanea y lo busca en firestore
    val qr = remember(m.folio) { qrBitmap(m.folio).asImageBitmap() }

    EcoCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Caps("Folio oficial electrónico", TextMuted, 9)
                Text("#${m.folio}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
            }
            Chip("CREADO", DangerSoft, Danger, Color(0xFFFCA5A5))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(fmtFecha(m.creadoEn), FieldBg, TextMuted)
            Chip("SELLO • ${m.hash.take(6).uppercase()}", FieldBg, TextMuted)
        }
        Spacer(Modifier.height(14.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Image(qr, "Código QR del manifiesto", Modifier.size(170.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Presente esta clave al transportista para transferir custodia en sitio",
                fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(10.dp))
        InfoLine("Residuo:", "${m.residuo} (${cantidadTexto(m)})")
        InfoLine("Transporte:", m.transportista)
        InfoLine("Vehículo / Placas:", "${m.vehiculo} [${m.placas}]")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // compartir como texto por cualquier app
            SoftButton("Compartir", Icons.Outlined.Share, {
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Manifiesto #${m.folio} – ${m.residuo} (${cantidadTexto(m)})")
                }
                ctx.startActivity(Intent.createChooser(i, "Compartir manifiesto"))
            }, Modifier.weight(1f))
            // genera el pdf y abre el menu de compartir/imprimir
            SoftButton("Imprimir PDF", Icons.Outlined.Print, {
                PdfExporter.compartir(ctx, PdfExporter.generar(ctx, m))
            }, Modifier.weight(1f), outlined = true)
        }
        Spacer(Modifier.height(10.dp))
        FooterNote("CRIPTOGRAFÍA SHA-256 VALIDADA")
    }
}
