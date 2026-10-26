package com.fidelia.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.fidelia.app.data.TradeIcon
import com.fidelia.app.data.TradeIcons
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.AppTopBar
import com.fidelia.app.ui.components.BusinessArt
import com.fidelia.app.ui.components.CenteredTextField
import com.fidelia.app.ui.components.ScreenTitle
import com.fidelia.app.ui.theme.contrast
import java.io.File

/**
 * Crear o editar una tarjeta de visita: un icono de oficio o una foto de la tarjeta, y
 * luego la empresa, el titular y hasta dos telefonos.
 */
@Composable
fun BusinessFormScreen(vm: FideliaViewModel) {
    val d = vm.businessDraft
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val editing = d.editingId != null
    var confirmExit by remember { mutableStateOf(false) }

    fun tryExit() {
        if (vm.isBusinessDraftDirty) confirmExit = true else vm.back()
    }
    BackHandler { tryExit() }

    // ---- Foto de la tarjeta (camara del sistema) y galeria ----
    val captureUri = remember {
        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(dir, "card.jpg"))
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) vm.importBusinessImage(captureUri)
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.importBusinessImage(uri)
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

    // ---- Elegir un telefono de la agenda del movil ----
    var phoneSlot by rememberSaveable { mutableIntStateOf(1) }
    val pickPhone = rememberLauncherForActivityResult(PickPhoneContract) { uri ->
        if (uri != null) vm.setBusinessPhoneFromContact(phoneSlot, uri)
    }
    fun pickPhoneFor(slot: Int) {
        phoneSlot = slot
        try {
            pickPhone.launch(Unit)
        } catch (e: Exception) {
            vm.showMessage("No hay ninguna aplicación de contactos.")
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        // En la barra solo queda "Atras"; el titulo va debajo, con el contenido.
        topBar = { AppTopBar(title = "", onBack = { tryExit() }) },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (!d.canSave) {
                        Text(
                            "Escribe el nombre de la empresa o el del titular para poder guardarla.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    Button(
                        onClick = vm::saveBusiness,
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
            ScreenTitle(if (editing) "Editar tarjeta de visita" else "Nueva tarjeta de visita")

            // 1. Icono o foto
            StepHeader(1, "Icono o foto de la tarjeta", done = d.tradeId != null || d.imageFile != null)
            BusinessArt(
                tradeId = d.tradeId,
                imageFile = d.imageFile,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .align(Alignment.CenterHorizontally),
            )
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
            if (d.imageFile != null) {
                TextButton(onClick = vm::clearBusinessImage) { Text("Quitar la foto") }
            }
            Text(
                "O elige el icono de un oficio:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TradeGrid(selectedId = d.tradeId, onSelect = vm::chooseTrade)

            Spacer(Modifier.height(8.dp))

            // 2. Datos
            StepHeader(2, "Datos de la tarjeta", done = d.canSave)
            CenteredTextField(
                value = d.company,
                onValueChange = vm::setBusinessCompany,
                label = "Nombre de la empresa",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )
            CenteredTextField(
                value = d.holder,
                onValueChange = vm::setBusinessHolder,
                label = "Titular de la tarjeta",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )
            CenteredTextField(
                value = d.phone1,
                onValueChange = vm::setBusinessPhone1,
                label = "Teléfono 1",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                trailing = { ContactsButton { pickPhoneFor(1) } },
            )
            CenteredTextField(
                value = d.phone2,
                onValueChange = vm::setBusinessPhone2,
                label = "Teléfono 2 (opcional)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                trailing = { ContactsButton { pickPhoneFor(2) } },
            )
            Text(
                "Escribe el teléfono o elígelo de tu agenda con el icono de contactos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            CenteredTextField(
                value = d.email,
                onValueChange = vm::setBusinessEmail,
                label = "Correo electrónico (opcional)",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
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

/**
 * El selector de contactos del sistema, filtrado a numeros de telefono: devuelve la fila del
 * numero elegido (con permiso para leerla) o null si se cancela.
 */
private object PickPhoneContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        if (resultCode == Activity.RESULT_OK) intent?.data else null
}

/** Boton con el icono de contactos que va dentro del campo de telefono. */
@Composable
private fun ContactsButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Rounded.Contacts,
            contentDescription = "Elegir un teléfono de tus contactos",
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

/** La lista de oficios: tres por fila, para que los nombres se lean enteros. */
@Composable
private fun TradeGrid(selectedId: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TradeIcons.all.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { trade ->
                    TradeCell(
                        trade = trade,
                        selected = trade.id == selectedId,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(trade.id) },
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TradeCell(trade: TradeIcon, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    // Si el color del oficio se confunde con el fondo (p. ej. un gris oscuro en tema oscuro), se aclara.
    val iconColor = if (contrast(trade.color, colors.surface) >= 3f) {
        trade.color
    } else {
        lerp(trade.color, colors.onSurface, 0.6f)
    }
    // Los oficios "solid" (Emergencias) se ven siempre en blanco sobre su color.
    val filled = selected || trade.solid
    Column(
        modifier
            .clip(shape)
            .background(if (filled) trade.color else colors.surface, shape)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.primary else colors.outlineVariant,
                shape = shape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            trade.icon,
            contentDescription = null,
            tint = if (filled) Color.White else iconColor,
            modifier = Modifier.size(30.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            trade.label,
            color = if (filled) Color.White else colors.onSurface,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
