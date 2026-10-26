package com.fidelia.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as GeoSize
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fidelia.app.barcode.BarcodeDetector
import com.fidelia.app.barcode.BarcodeRenderer
import com.fidelia.app.barcode.DetectedBarcode
import com.fidelia.app.data.decodeBitmap
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.BarcodeImage
import com.fidelia.app.ui.components.CenteredTextField
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/**
 * Lee el codigo de barras con la camara. Alternativas: escribir el numero a mano
 * o leerlo de una imagen (foto o captura de pantalla) de la galeria.
 */
@Composable
fun ScannerScreen(vm: FideliaViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
        permissionDenied = !it
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var showManual by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var handled by remember { mutableStateOf(false) }

    /** Da por bueno un codigo: lo guarda en el borrador y vuelve al formulario. */
    fun accept(found: DetectedBarcode) {
        if (handled) return
        val rendered = BarcodeRenderer.render(found.value, found.format)
        if (rendered == null) {
            vm.showMessage("No he podido usar ese código. Prueba con otro.")
            return
        }
        handled = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        vm.setBarcode(found.value, rendered.format)
        vm.back()
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            busy = true
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    runCatching { decodeBitmap(context, uri, 2400) }.getOrNull()
                }
                val found = bitmap?.let { BarcodeDetector.detect(it) }
                busy = false
                if (found != null) accept(found) else vm.showMessage("No he encontrado ningún código de barras en esa imagen.")
            }
        }
    }

    BackHandler { vm.back() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission) {
            CameraPreview(
                torchOn = torchOn,
                onCameraReady = { camera = it },
                onBarcode = ::accept,
            )
        } else {
            PermissionPrompt(
                denied = permissionDenied,
                onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onOpenSettings = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    )
                },
            )
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val frameWidth = maxWidth * 0.86f
            val frameHeight = frameWidth * 0.55f
            val frameTop = maxHeight * 0.26f

            if (hasPermission) {
                ScanFrame(frameWidth, frameHeight, frameTop)
                Text(
                    "Apunta al código de barras de tu tarjeta",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = frameTop + frameHeight + 20.dp)
                        .padding(horizontal = 32.dp),
                )
            }

            // Barra superior: Atras y linterna
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OverlayPill(onClick = { vm.back() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Atrás", color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.weight(1f))
                if (hasPermission && camera?.cameraInfo?.hasFlashUnit() == true) {
                    OverlayPill(onClick = { torchOn = !torchOn }) {
                        Icon(
                            if (torchOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                            contentDescription = if (torchOn) "Apagar linterna" else "Encender linterna",
                            tint = Color.White,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (torchOn) "Apagar luz" else "Luz",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            // Alternativas a la camara (una debajo de otra: el texto cabe entero)
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { showManual = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                ) {
                    Icon(Icons.Rounded.Keyboard, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Escribir número")
                }
                OutlinedButton(
                    onClick = {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Desde una foto")
                }
            }
        }

        if (busy) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    if (showManual) {
        ManualCodeDialog(
            onDismiss = { showManual = false },
            onConfirm = { value ->
                showManual = false
                accept(DetectedBarcode(value, BarcodeRenderer.guessFormat(value)))
            },
        )
    }
}

@Composable
private fun OverlayPill(onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        Modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .height(44.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

/** Recuadro de guia con una linea que sube y baja. */
@Composable
private fun ScanFrame(width: Dp, height: Dp, top: Dp) {
    val transition = rememberInfiniteTransition(label = "linea")
    val t by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "posicion",
    )
    Canvas(
        Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val frameW = width.toPx()
        val frameH = height.toPx()
        val left = (size.width - frameW) / 2f
        val frameTop = top.toPx()
        val corner = CornerRadius(24.dp.toPx())
        drawRect(Color.Black.copy(alpha = 0.55f))
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, frameTop),
            size = GeoSize(frameW, frameH),
            cornerRadius = corner,
            blendMode = BlendMode.Clear,
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, frameTop),
            size = GeoSize(frameW, frameH),
            cornerRadius = corner,
            style = Stroke(3.dp.toPx()),
        )
        val y = frameTop + frameH * t
        drawLine(
            color = Color(0xFF818CF8),
            start = Offset(left + 18.dp.toPx(), y),
            end = Offset(left + frameW - 18.dp.toPx(), y),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    torchOn: Boolean,
    onCameraReady: (Camera) -> Unit,
    onBarcode: (DetectedBarcode) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val latestOnBarcode by rememberUpdatedState(onBarcode)
    val latestOnReady by rememberUpdatedState(onCameraReady)

    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient(BarcodeDetector.scannerOptions)
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        var lastValue: String? = null
        var closed = false

        providerFuture.addListener({
            if (closed) return@addListener
            val cameraProvider = providerFuture.get()
            provider = cameraProvider

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val analysis = ImageAnalysis.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(Size(1920, 1080), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                        )
                        .build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { proxy ->
                val media = proxy.image
                if (media == null || closed) {
                    proxy.close()
                    return@setAnalyzer
                }
                try {
                    scanner.process(InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees))
                        .addOnSuccessListener { list ->
                            // Se exige la misma lectura dos veces seguidas para no dar por buena una lectura suelta erronea.
                            val found = list.firstNotNullOfOrNull(BarcodeDetector::toDetected)
                            when {
                                found == null -> lastValue = null
                                found.value == lastValue -> latestOnBarcode(found)
                                else -> lastValue = found.value
                            }
                        }
                        .addOnCompleteListener { proxy.close() }
                } catch (e: Exception) {
                    proxy.close()
                }
            }

            try {
                cameraProvider.unbindAll()
                val bound = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
                camera = bound
                latestOnReady(bound)
            } catch (e: Exception) {
                // Sin camara disponible: quedan las alternativas de abajo.
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            closed = true
            provider?.unbindAll()
            scanner.close()
            executor.shutdown()
        }
    }

    LaunchedEffect(camera, torchOn) {
        camera?.cameraControl?.enableTorch(torchOn)
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun PermissionPrompt(denied: Boolean, onRequest: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Necesito la cámara para leer el código de barras",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Solo se usa mientras escaneas. También puedes escribir el número o leerlo de una foto.",
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRequest, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Dar permiso de cámara")
        }
        if (denied) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onOpenSettings) { Text("Abrir los ajustes de la app") }
        }
    }
}

@Composable
private fun ManualCodeDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val value = text.trim()
    val preview = remember(value) {
        if (value.isEmpty()) null else BarcodeRenderer.render(value, BarcodeRenderer.guessFormat(value))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escribir el código") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Escribe el número que aparece debajo del código de barras de tu tarjeta.")
                CenteredTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "Número",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                )
                if (preview != null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BarcodeImage(preview, barHeight = 70.dp, squareSize = 110.dp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(value) }, enabled = preview != null) { Text("Usar este código") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
