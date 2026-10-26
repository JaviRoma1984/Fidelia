package com.fidelia.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Una tarjeta de una lista reordenable (fidelizacion o visita): su "cara" encima, el
 * nombre debajo y el menu de tres puntos (editar / eliminar). Mientras se arrastra se
 * agranda un poco y sigue al dedo, por encima de las demas.
 *
 * El toque lo recoge la rejilla (para distinguirlo de arrastrar); [onOpen] aqui solo
 * sirve para TalkBack.
 */
@Composable
fun ReorderTile(
    id: String,
    title: String,
    subtitle: String?,
    openLabel: String,
    reorder: GridReorderState,
    dragging: Boolean,
    modifier: Modifier,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    art: @Composable (Modifier) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val lifted = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                val t = reorder.translationOf(id)
                translationX = t.x
                translationY = t.y
                scaleX = 1.07f
                scaleY = 1.07f
            }
            .alpha(0.95f)
    } else {
        Modifier
    }
    Column(modifier.then(lifted)) {
        art(
            Modifier
                .fillMaxWidth()
                .semantics {
                    onClick(label = openLabel) {
                        onOpen()
                        true
                    }
                }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Más opciones de $title")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}
