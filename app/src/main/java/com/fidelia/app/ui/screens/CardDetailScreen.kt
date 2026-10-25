package com.fidelia.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidelia.app.barcode.BarcodeRenderer
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.AppTopBar
import com.fidelia.app.ui.components.BarcodeImage
import com.fidelia.app.ui.components.CardArt
import com.fidelia.app.ui.components.ConfirmDeleteDialog

/** La tarjeta abierta: su codigo de barras grande para ensenarlo en caja. */
@Composable
fun CardDetailScreen(vm: FideliaViewModel, id: String) {
    BrightnessBoost()

    val card = vm.cards.find { it.id == id }
    var confirmDelete by remember { mutableStateOf(false) }
    if (card == null) {
        Box(Modifier.fillMaxSize())
        return
    }
    val rendered = remember(card.barcodeValue, card.barcodeFormat) {
        BarcodeRenderer.render(card.barcodeValue, card.barcodeFormat)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = card.name, onBack = vm::back) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CardArt(
                storeId = card.storeId,
                imageFile = card.imageFile,
                modifier = Modifier.fillMaxWidth(0.55f),
            )
            Spacer(Modifier.height(20.dp))

            // Siempre blanco sobre negro: es lo que mejor lee el lector de la tienda.
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (rendered != null) {
                        BarcodeImage(rendered, barHeight = 150.dp, squareSize = 240.dp)
                    } else {
                        Text("No se puede mostrar este código.", color = Color.Black)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        card.barcodeValue,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Enséñaselo al lector en caja. Mientras estés aquí, la pantalla se pone con el máximo brillo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { vm.startEdit(card.id) },
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

    if (confirmDelete) {
        ConfirmDeleteDialog(
            cardName = card.name,
            onConfirm = {
                confirmDelete = false
                vm.delete(card.id)
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

/**
 * Sube el brillo de esta ventana al maximo y mantiene la pantalla encendida
 * mientras se ensena el codigo. Solo afecta a la app y se deshace al salir;
 * no toca los ajustes del movil.
 */
@Composable
private fun BrightnessBoost() {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity) {
        val window = activity?.window
        val previous = window?.attributes?.screenBrightness
        if (window != null) {
            window.attributes = window.attributes.also {
                it.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (window != null) {
                window.attributes = window.attributes.also {
                    it.screenBrightness = previous ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
