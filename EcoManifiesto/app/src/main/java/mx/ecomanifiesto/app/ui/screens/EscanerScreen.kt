package mx.ecomanifiesto.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import mx.ecomanifiesto.app.data.Manifiesto
import mx.ecomanifiesto.app.data.Repo
import mx.ecomanifiesto.app.data.Session
import mx.ecomanifiesto.app.ui.Caps
import mx.ecomanifiesto.app.ui.Chip
import mx.ecomanifiesto.app.ui.EcoTopBar
import mx.ecomanifiesto.app.ui.FooterNote
import mx.ecomanifiesto.app.ui.GreenChip
import mx.ecomanifiesto.app.ui.PdfExporter
import mx.ecomanifiesto.app.ui.PrimaryButton
import mx.ecomanifiesto.app.ui.SignaturePad
import mx.ecomanifiesto.app.ui.SignatureState
import mx.ecomanifiesto.app.ui.SoftButton
import mx.ecomanifiesto.app.ui.TrazoView
import mx.ecomanifiesto.app.ui.cantidadTexto
import mx.ecomanifiesto.app.ui.friendlyError
import mx.ecomanifiesto.app.ui.theme.Border
import mx.ecomanifiesto.app.ui.theme.CamBg
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
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

// pantalla del destino final: escanea el QR, verifica el manifiesto y firma de recibido
@Composable
fun EscanerScreen(onLogout: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // estados: el manifiesto encontrado, mensajes, si esta buscando o enviando, linterna y el dialogo de folio manual
    var manifiesto by remember { mutableStateOf<Manifiesto?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var buscando by remember { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }
    var entregado by remember { mutableStateOf(false) }
    var linterna by remember { mutableStateOf(false) }
    var dialogoFolio by remember { mutableStateOf(false) }
    var folioManual by remember { mutableStateOf("") }
    val firma = remember { SignatureState() }

    // busca el manifiesto en firestore (lo usan el escaner y el folio manual)
    fun buscar(folio: String) {
        // para que no busque dos veces seguidas
        if (buscando) return
        buscando = true
        mensaje = null
        entregado = false
        scope.launch {
            runCatching { Repo.getManifiesto(folio) }
                .onSuccess { m ->
                    // solo se puede recibir si ya esta EN_TRANSITO
                    when {
                        m == null -> { manifiesto = null; mensaje = "No existe el manifiesto \"$folio\"." }
                        m.estado == "ENTREGADO" -> { manifiesto = null; mensaje = "El manifiesto ${m.folio} ya fue entregado." }
                        m.estado == "CREADO" -> { manifiesto = null; mensaje = "El manifiesto ${m.folio} aún no tiene firma de custodia." }
                        else -> { manifiesto = m; firma.clear() }
                    }
                }
                .onFailure { mensaje = friendlyError(it) }
            buscando = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        EcoTopBar("RECEPCIÓN", onLogout)

        // parte de arriba: la camara con los botones encima
        Box(Modifier.weight(1f).fillMaxWidth().background(CamBg)) {
            CameraQrScanner(
                modifier = Modifier.fillMaxSize(),
                torch = linterna,
                // cada vez que lee un QR se llama a buscar con el texto del codigo
                onCode = { buscar(it) }
            )
            // marco de escaneo
            ScanOverlay()

            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.clip(RoundedCornerShape(50)).background(Color(0x99000000)).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                    Spacer(Modifier.width(6.dp))
                    Caps("Cámara activa", Color.White, 9)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (linterna) Primary else Color(0x99000000))
                        .clickable { linterna = !linterna },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.FlashOn, "Linterna", tint = Color.White, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(8.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x99000000))
                        .clickable { dialogoFolio = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Keyboard, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Folio Manual", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // aviso de entrega notificada (solo visual)
            if (entregado) {
                val m = manifiesto
                Row(
                    Modifier
                        .padding(12.dp)
                        .padding(top = 44.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardWhite)
                        .border(1.dp, GreenSoftBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Notifications, null, tint = GreenText, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Entrega Notificada", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                        Text(
                            "Entrega de ${m?.folio.orEmpty()} registrada para ${m?.generador.orEmpty()}",
                            fontSize = 10.sp, color = TextMuted, maxLines = 1
                        )
                    }
                    Icon(
                        Icons.Outlined.Close, "Cerrar", tint = TextMuted,
                        modifier = Modifier.size(16.dp).clickable { entregado = false }
                    )
                }
            }
        }

        // parte de abajo: datos del manifiesto y firma del receptor
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 430.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(CardWhite)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.align(Alignment.CenterHorizontally).width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(50)).background(Border)
            )
            val m = manifiesto
            // si no hay manifiesto muestra las instrucciones o el mensaje de error
            if (m == null) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.QrCodeScanner, null, tint = TextMuted, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (buscando) "Verificando manifiesto..." else "Escanee el código QR del manifiesto para verificarlo",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMain, textAlign = TextAlign.Center
                    )
                    if (mensaje != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(mensaje!!, fontSize = 11.sp, color = Danger, textAlign = TextAlign.Center)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.VerifiedUser, null, tint = GreenText, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Manifiesto Verificado", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                        Caps("RF-05 • Destino Final", TextMuted, 9)
                    }
                    Chip(
                        if (m.estado == "ENTREGADO") "ENTREGADO" else "TRÁNSITO → ENTREGADO",
                        if (m.estado == "ENTREGADO") GreenSoft else WarnSoft,
                        if (m.estado == "ENTREGADO") GreenText else Warn
                    )
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FieldBg)
                        .padding(12.dp)
                ) {
                    DatoFila("Folio Oficial:", "#${m.folio}")
                    DatoFila("Residuo Peligroso:", "${m.residuo} (${cantidadTexto(m)})")
                    DatoFila("Transportista Certificado:", "${m.transportista}\nOperador: ${m.transportistaFirmante.ifBlank { m.conductor }}")
                    DatoFila("Planta Generadora:", m.generador)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Firma de Receptor Autorizado (Destino Final)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain, modifier = Modifier.weight(1f))
                    if (m.estado == "ENTREGADO" || firma.hasInk) GreenChip("VALIDADO")
                    else Chip("PENDIENTE", WarnSoft, Warn)
                }

                // si ya esta entregado muestra la firma guardada, si no deja firmar
                if (m.estado == "ENTREGADO") {
                    TrazoView(
                        m.firmaReceptor,
                        Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(12.dp)).background(FieldBg)
                    )
                } else {
                    SignaturePad(
                        state = firma,
                        modifier = Modifier.fillMaxWidth().height(96.dp),
                        hint = { Text("Firme aquí", fontSize = 12.sp, color = TextMuted) }
                    )
                    TextButton(onClick = { firma.clear() }) {
                        Text("Limpiar firma", color = TextMuted, fontSize = 11.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Fingerprint, null, tint = GreenText, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Huella Criptográfica SHA-256",
                        fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(Session.nombre.ifBlank { "Receptor autorizado" }, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
                        Text(Session.cargo.ifBlank { "Sup. Planta Destino Final" }, fontSize = 10.sp, color = TextMuted)
                    }
                }

                // confirma la recepcion, solo se activa si ya firmaron y todavia no esta entregado
                PrimaryButton(
                    text = if (m.estado == "ENTREGADO") "Recepción Confirmada" else "Confirmar Recepción (ENTREGADO)",
                    enabled = m.estado != "ENTREGADO" && firma.hasInk,
                    loading = enviando,
                    trailing = null,
                    onClick = {
                        enviando = true
                        scope.launch {
                            runCatching { Repo.confirmarRecepcion(m.folio, firma.serialize()) }
                                .onSuccess { nuevo -> if (nuevo != null) { manifiesto = nuevo; entregado = true } }
                                .onFailure { mensaje = friendlyError(it) }
                            enviando = false
                        }
                    }
                )
                // genera el acuse en pdf
                SoftButton(
                    text = "Descargar Acuse Certificado PDF",
                    icon = Icons.Outlined.PictureAsPdf,
                    onClick = { PdfExporter.compartir(ctx, PdfExporter.generar(ctx, m)) },
                    modifier = Modifier.fillMaxWidth(),
                    outlined = true
                )
                if (mensaje != null) Text(mensaje!!, fontSize = 11.sp, color = Danger)
                FooterNote("PROTOCOLO OFICIAL NOM-052-SEMARNAT-2005")
            }
        }
    }

    // ventana para escribir el folio a mano por si no sirve la camara
    if (dialogoFolio) {
        AlertDialog(
            onDismissRequest = { dialogoFolio = false },
            title = { Text("Folio manual") },
            text = {
                OutlinedTextField(
                    value = folioManual,
                    onValueChange = { folioManual = it },
                    singleLine = true,
                    placeholder = { Text("EM-2026-0042") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogoFolio = false
                    if (folioManual.isNotBlank()) buscar(folioManual)
                }) { Text("Verificar", color = GreenText, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { dialogoFolio = false }) { Text("Cancelar", color = TextMuted) }
            }
        )
    }
}

// renglon de datos del manifiesto
@Composable
private fun DatoFila(label: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(120.dp))
        Text(valor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TextMain, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

// marco con esquinas verdes y una linea que sube y baja (animacion)
@Composable
private fun ScanOverlay() {
    val transition = rememberInfiniteTransition(label = "scan")
    // t va de 0.08 a 0.92 y se repite, es la posicion de la linea
    val t by transition.animateFloat(
        initialValue = 0.08f, targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "linea"
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(190.dp)) {
                val lado = size.minDimension
                val corto = 34.dp.toPx()
                val grosor = 4.dp.toPx()
                drawRoundRect(
                    color = Color(0x33FFFFFF),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    size = Size(lado, lado)
                )
                val verde = Color(0xFF22C55E)
                val s = Stroke(width = grosor, cap = StrokeCap.Round)
                // dibuja una esquina en forma de L, dx y dy dicen hacia donde van las lineas
                fun esquina(x: Float, y: Float, dx: Float, dy: Float) {
                    drawLine(verde, Offset(x, y), Offset(x + dx * corto, y), grosor, StrokeCap.Round)
                    drawLine(verde, Offset(x, y), Offset(x, y + dy * corto), grosor, StrokeCap.Round)
                }
                // una por cada esquina del cuadro
                esquina(0f, 0f, 1f, 1f)
                esquina(lado, 0f, -1f, 1f)
                esquina(0f, lado, 1f, -1f)
                esquina(lado, lado, -1f, -1f)
                // la linea que se mueve
                val y = lado * t
                drawLine(verde, Offset(10.dp.toPx(), y), Offset(lado - 10.dp.toPx(), y), 2.dp.toPx())
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Alinee el código QR oficial de SEMARNAT",
                color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x99000000))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }
    }
}

// vista de la camara con CameraX, cada cuadro se analiza con ML Kit para detectar codigos QR
@Composable
private fun CameraQrScanner(modifier: Modifier, torch: Boolean, onCode: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // revisa si ya tiene permiso de camara
    var permitido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permitido = it }
    // si no tiene permiso lo pide al abrir
    LaunchedEffect(Unit) { if (!permitido) pedirPermiso.launch(Manifest.permission.CAMERA) }

    if (!permitido) {
        Box(modifier.background(CamBg), contentAlignment = Alignment.Center) {
            Text(
                "Se necesita permiso de cámara para escanear.\nPuedes usar \"Folio Manual\".",
                color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center
            )
        }
        return
    }

    // para usar siempre la ultima version de onCode dentro del analizador
    val onCodeActual by rememberUpdatedState(onCode)
    // hilo aparte para analizar las imagenes sin trabar la pantalla
    val executor = remember { Executors.newSingleThreadExecutor() }
    // escaner de ML Kit configurado solo para QR
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
    }
    // evita que lea el mismo QR muchas veces seguidas
    val ocupado = remember { AtomicBoolean(false) }
    var camara by remember { mutableStateOf<Camera?>(null) }

    // al salir de la pantalla cerramos el hilo y el escaner
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            scanner.close()
        }
    }
    // prende o apaga la linterna
    LaunchedEffect(torch, camara) { camara?.cameraControl?.enableTorch(torch) }

    // PreviewView es una vista normal de android, por eso se mete con AndroidView
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val vista = PreviewView(ctx)
            val futuro = ProcessCameraProvider.getInstance(ctx)
            futuro.addListener({
                val proveedor = futuro.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(vista.surfaceProvider) }
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                // aqui llega cada imagen de la camara
                analisis.setAnalyzer(executor) { proxy ->
                    val media = proxy.image
                    if (media == null) {
                        proxy.close()
                    } else {
                        val imagen = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
                        scanner.process(imagen)
                            .addOnSuccessListener { codigos ->
                                val valor = codigos.firstOrNull()?.rawValue
                                if (valor != null && ocupado.compareAndSet(false, true)) {
                                    onCodeActual(valor)
                                    // despues de 3 segundos deja escanear otra vez
                                    vista.postDelayed({ ocupado.set(false) }, 3000)
                                }
                            }
                            .addOnCompleteListener { proxy.close() }
                    }
                }
                proveedor.unbindAll()
                camara = proveedor.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis
                )
            }, ContextCompat.getMainExecutor(ctx))
            vista
        }
    )
}
