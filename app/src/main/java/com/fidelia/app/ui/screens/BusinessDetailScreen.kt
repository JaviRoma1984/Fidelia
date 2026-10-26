package com.fidelia.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.fidelia.app.data.BusinessCard
import com.fidelia.app.data.Emails
import com.fidelia.app.data.PhoneNumbers
import com.fidelia.app.data.TradeIcons
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.AppTopBar
import com.fidelia.app.ui.components.BusinessArt
import com.fidelia.app.ui.components.ConfirmDeleteDialog

/**
 * La ficha de una tarjeta de visita. Pulsar un telefono pide confirmacion y, si se acepta,
 * llama directamente (la primera vez Android pide el permiso de llamadas; si no se da,
 * se abre el marcador con el numero ya escrito).
 */
@Composable
fun BusinessDetailScreen(vm: FideliaViewModel, id: String) {
    val context = LocalContext.current
    val card = vm.businessCards.find { it.id == id }
    var confirmDelete by remember { mutableStateOf(false) }
    // Telefono por el que se esta pidiendo confirmacion, y el que espera al permiso de llamadas.
    var confirmCall by remember { mutableStateOf<String?>(null) }
    var waitingForPermission by remember { mutableStateOf<String?>(null) }

    val callPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val number = waitingForPermission
        waitingForPermission = null
        if (number != null) {
            if (granted) placeCall(context, number) else openDialer(context, number)
        }
    }

    if (card == null) {
        Box(Modifier.fillMaxSize())
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // En la barra solo queda "Atras"; el nombre de la empresa ya va debajo, bien grande.
        topBar = { AppTopBar(title = "", onBack = vm::back) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BusinessArt(
                tradeId = card.tradeId,
                imageFile = card.imageFile,
                modifier = Modifier.fillMaxWidth(0.75f),
            )
            Spacer(Modifier.height(20.dp))

            if (card.company.isNotBlank()) {
                Text(
                    card.company,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
            }
            if (card.holder.isNotBlank()) {
                Text(
                    card.holder,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            TradeIcons.find(card.tradeId)?.takeIf { card.imageFile == null }?.let {
                Text(
                    it.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(24.dp))
            if (card.phones.isEmpty()) {
                Text(
                    "Esta tarjeta no tiene teléfono guardado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    card.phones.forEach { phone ->
                        Button(
                            onClick = { confirmCall = phone },
                            enabled = PhoneNumbers.isCallable(phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text(PhoneNumbers.display(phone), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Antes de llamar te pediré confirmación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            if (card.email.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                if (Emails.looksValid(card.email)) {
                    // Tocar el correo abre el programa de correo con un mensaje nuevo para esa direccion.
                    OutlinedButton(
                        onClick = {
                            if (!openEmail(context, card.email)) vm.showMessage("No hay ninguna aplicación de correo.")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Icon(Icons.Rounded.Email, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(card.email.trim(), maxLines = 1)
                    }
                } else {
                    Text(
                        card.email.trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { vm.startEditBusiness(card.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar")
                }
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    confirmCall?.let { number ->
        CallConfirmDialog(
            card = card,
            number = number,
            onDismiss = { confirmCall = null },
            onConfirm = {
                confirmCall = null
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED
                if (granted) {
                    placeCall(context, number)
                } else {
                    waitingForPermission = number
                    callPermission.launch(Manifest.permission.CALL_PHONE)
                }
            },
        )
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            cardName = card.title,
            what = "sus datos",
            onConfirm = {
                confirmDelete = false
                vm.deleteBusiness(card.id)
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun CallConfirmDialog(card: BusinessCard, number: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val who = listOf(card.company, card.holder).filter { it.isNotBlank() }.joinToString(" · ")
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Call, contentDescription = null) },
        title = { Text("¿Llamar ahora?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Vas a llamar a:")
                Text(who, style = MaterialTheme.typography.titleMedium)
                Text(PhoneNumbers.display(number), style = MaterialTheme.typography.titleLarge)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Icon(Icons.Rounded.Call, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Llamar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

/** Abre el correo con un mensaje nuevo para esa direccion. Devuelve false si no hay programa de correo. */
private fun openEmail(context: Context, email: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email.trim(), null)))
    true
} catch (e: Exception) {
    false
}

private fun telUri(number: String): Uri = Uri.fromParts("tel", PhoneNumbers.dialable(number), null)

/** Llama directamente (ya confirmado y con permiso). Si el movil no puede, abre el marcador. */
private fun placeCall(context: Context, number: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_CALL, telUri(number)))
    } catch (e: Exception) {
        openDialer(context, number)
    }
}

/** Abre el marcador con el numero escrito; quien llama es el usuario. */
private fun openDialer(context: Context, number: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, telUri(number)))
    } catch (e: Exception) {
        // Sin aplicacion de telefono: no hay nada mas que hacer.
    }
}
