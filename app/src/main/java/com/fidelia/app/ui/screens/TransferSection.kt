package com.fidelia.app.ui.screens

import android.content.ClipData
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fidelia.app.data.CardTransfer
import com.fidelia.app.ui.FideliaViewModel
import kotlinx.coroutines.launch

/** Exportar las tarjetas a un archivo (para compartirlo o guardarlo) e importarlo de otro. */
@Composable
fun TransferSection(vm: FideliaViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExport by remember { mutableStateOf(false) }

    val saveFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(CardTransfer.MIME)
    ) { uri -> if (uri != null) vm.saveExportTo(uri) }

    val openFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) vm.importFrom(uri) }

    Column(
        Modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Copia y compartir",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Pasa tus tarjetas a otro móvil o guárdalas como copia de seguridad.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    if (vm.cards.isEmpty()) vm.showMessage("Aún no tienes tarjetas que exportar.") else showExport = true
                },
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar", maxLines = 1)
            }
            OutlinedButton(
                onClick = { openFile.launch(arrayOf("*/*")) },
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                Icon(Icons.Rounded.FileDownload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Importar", maxLines = 1)
            }
        }
    }

    if (showExport) {
        val count = vm.cards.size
        AlertDialog(
            onDismissRequest = { showExport = false },
            title = { Text("Exportar tarjetas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Se creará un archivo con " + (if (count == 1) "tu tarjeta" else "tus $count tarjetas") +
                            ", con sus imágenes y códigos de barras. Quien lo reciba podrá usarlas: " +
                            "compártelo solo con quien quieras."
                    )
                    Button(
                        onClick = {
                            showExport = false
                            scope.launch {
                                val uri = vm.createShareFile()
                                if (uri == null) {
                                    vm.showMessage("No se ha podido crear el archivo.")
                                } else {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = CardTransfer.MIME
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Mis tarjetas de Fidelia")
                                        clipData = ClipData.newRawUri("", uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(send, "Compartir tarjetas"))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir")
                    }
                    OutlinedButton(
                        onClick = {
                            showExport = false
                            saveFile.launch(CardTransfer.suggestedFileName())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar en el móvil")
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExport = false }) { Text("Cancelar") } },
        )
    }

    vm.importReport?.let { report ->
        AlertDialog(
            onDismissRequest = vm::dismissImportReport,
            title = {
                Text(if (report.added == 0 && report.skipped == 0) "No se ha importado nada" else "Importación terminada")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tarjetas añadidas: ${report.added}")
                    if (report.skipped > 0) {
                        Text("Ya las tenías (omitidas): ${report.skipped}")
                    }
                    if (report.renamed.isNotEmpty()) {
                        Text(
                            "Mismo nombre pero otro código de barras (añadidas con otro nombre): " +
                                report.renamed.joinToString(", ") { "«$it»" }
                        )
                    }
                    if (report.invalid > 0) {
                        Text("No válidas (no se han importado): ${report.invalid}")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.dismissImportReport()
                    if (report.added > 0) vm.openCardList()
                }) { Text(if (report.added > 0) "Ver mis tarjetas" else "Entendido") }
            },
            dismissButton = if (report.added > 0) {
                { TextButton(onClick = vm::dismissImportReport) { Text("Cerrar") } }
            } else {
                null
            },
        )
    }
}
