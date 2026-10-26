package com.fidelia.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.fidelia.app.barcode.BarcodeRenderer
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.Screen
import com.fidelia.app.ui.components.AppTopBar
import com.fidelia.app.ui.components.BarcodeImage
import com.fidelia.app.ui.components.CardArt
import com.fidelia.app.ui.components.CenteredTextField
import com.fidelia.app.ui.components.ScreenTitle
import java.io.File

/** Crear o editar una tarjeta: imagen, nombre y codigo de barras. */
@Composable
fun CardFormScreen(vm: FideliaViewModel) {
    val d = vm.draft
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val editing = d.editingId != null
    var confirmExit by remember { mutableStateOf(false) }

    fun tryExit() {
        if (vm.isDraftDirty) confirmExit = true else vm.back()
    }
    BackHandler { tryExit() }

    // ---- Foto de la tarjeta (camara del sistema) y galeria ----
    val captureUri = remember {
        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(dir, "card.jpg"))
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) vm.importImage(captureUri)
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.importImage(uri)
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            takePicture.launch(captureUri)
        } else {
            vm.showMessage("Sin permiso de cámara no puedo hacer la foto. Puedes elegir una imagen de la galería.")
        }
    }
    fun takePhoto() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) takePicture.launch(captureUri) else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    val rendered = remember(d.barcodeValue, d.barcodeFormat) {
        BarcodeRenderer.render(d.barcodeValue, d.barcodeFormat)
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        // En la barra solo queda "Atras": el titulo va debajo, con el contenido.
        topBar = { AppTopBar(title = "", onBack = { tryExit() }) },
        bottomBar = {
            val missing = when {
                d.name.isBlank() -> "Ponle un nombre a la tarjeta para poder guardarla."
                d.barcodeValue.isBlank() -> "Escanea el código de barras para poder guardarla."
                else -> null
            }
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (missing != null) {
                        Text(
                            missing,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    Button(
                        onClick = vm::save,
                        enabled = d.canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Text(if (editing) "Guardar cambios" else "Guardar tarjeta")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ScreenTitle(if (editing) "Editar tarjeta" else "Nueva tarjeta")

            // 1. Imagen
            StepHeader(1, "Imagen de la tarjeta", done = d.imageFile != null || d.storeId != null)
            CardArt(
                storeId = d.storeId,
                imageFile = d.imageFile,
                modifier = Modifier.fillMaxWidth(),
            )
            if (d.storeId == null && d.imageFile == null) {
                Text(
                    "Haz una foto a tu tarjeta o elige una imagen de la galería.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { takePhoto() },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Icon(Icons.Rounded.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hacer foto", maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galería", maxLines = 1)
                }
            }
            // Tambien se puede elegir uno de los logos de la app (p. ej. al editar una tarjeta sin logo).
            OutlinedButton(
                onClick = vm::openLogoChoice,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(Icons.Rounded.Storefront, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (d.storeId != null) "Cambiar el logo de la lista" else "Elegir un logo de la lista", maxLines = 1)
            }
            if (d.imageFile != null) {
                TextButton(onClick = vm::clearImage) {
                    Text(if (d.storeId != null) "Usar el logo de la tienda" else "Quitar la imagen")
                }
            }

            Spacer(Modifier.height(8.dp))

            // 2. Nombre
            StepHeader(2, "Nombre de la tarjeta", done = d.name.isNotBlank())
            CenteredTextField(
                value = d.name,
                onValueChange = vm::setName,
                label = "Nombre de la tarjeta",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )

            Spacer(Modifier.height(8.dp))

            // 3. Codigo de barras
            StepHeader(3, "Código de barras", done = rendered != null)
            if (rendered == null) {
                Text(
                    "Se abrirá la cámara. Apunta al código de barras de tu tarjeta y se leerá solo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { vm.push(Screen.Scanner) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Escanear código de barras")
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BarcodeImage(rendered, barHeight = 90.dp, squareSize = 150.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            d.barcodeValue,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                OutlinedButton(
                    onClick = { vm.push(Screen.Scanner) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Volver a escanear")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("¿Salir sin guardar?") },
            text = { Text("Los cambios que has hecho en esta tarjeta se perderán.") },
            confirmButton = {
                Button(onClick = { confirmExit = false }) { Text("Seguir editando") }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmExit = false
                    vm.back()
                }) { Text("Salir sin guardar") }
            },
        )
    }
}

@Composable
internal fun StepHeader(number: Int, title: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(30.dp)
                .background(
                    if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Text(
                    "$number",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}
