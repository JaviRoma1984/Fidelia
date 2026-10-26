package com.fidelia.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Confirmacion obligatoria antes de borrar una tarjeta. [what] dice que se borra junto
 * con la tarjeta ("su código de barras", "sus datos"...).
 */
@Composable
fun ConfirmDeleteDialog(
    cardName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    what: String = "su código de barras",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
        title = { Text("¿Eliminar esta tarjeta?") },
        text = {
            Text("Se borrará «$cardName» y $what de este móvil. Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) { Text("Sí, eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
