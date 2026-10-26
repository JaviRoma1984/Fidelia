package com.fidelia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.fidelia.app.data.LoyaltyCard
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.AppTopBar
import com.fidelia.app.ui.components.CardArt
import com.fidelia.app.ui.components.ConfirmDeleteDialog
import com.fidelia.app.ui.components.GridReorderState
import com.fidelia.app.ui.components.gridReorderGestures
import com.fidelia.app.ui.components.rememberGridReorderState

/**
 * "Mostrar tarjetas": todas las tarjetas con su imagen y nombre; al pulsar una sale su codigo.
 * Manteniendo pulsada una tarjeta se puede arrastrar para cambiarla de sitio.
 */
@Composable
fun CardListScreen(vm: FideliaViewModel) {
    // Copia de trabajo: mientras se arrastra se reordena esta; al soltar se guarda en el ViewModel.
    var working by remember(vm.cards) { mutableStateOf(vm.cards) }
    var toDelete by remember { mutableStateOf<LoyaltyCard?>(null) }
    val gridState = rememberLazyGridState()
    val reorder = rememberGridReorderState(gridState) { from, to ->
        working = working.toMutableList().apply { add(to, removeAt(from)) }
    }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "Mis tarjetas", onBack = vm::back) },
    ) { padding ->
        if (working.isEmpty()) {
            EmptyState(Modifier.padding(padding))
        } else {
            Column(Modifier.padding(padding)) {
                if (working.size > 1) {
                    Text(
                        "Mantén pulsada una tarjeta y arrástrala para cambiarla de sitio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    // Mientras se arrastra, la lista no debe desplazarse por su cuenta.
                    userScrollEnabled = reorder.draggingKey == null,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .gridReorderGestures(
                            state = reorder,
                            haptic = haptic,
                            onTap = { key -> vm.openDetail(key as String) },
                            onDrop = { vm.reorder(working.map { it.id }) },
                        ),
                ) {
                    items(working, key = { it.id }) { card ->
                        val dragging = reorder.draggingKey == card.id
                        CardTile(
                            card = card,
                            reorder = reorder,
                            dragging = dragging,
                            modifier = if (dragging) Modifier else Modifier.animateItem(),
                            onOpen = { vm.openDetail(card.id) },
                            onEdit = { vm.startEdit(card.id) },
                            onDelete = { toDelete = card },
                        )
                    }
                }
            }
        }
    }

    toDelete?.let { card ->
        ConfirmDeleteDialog(
            cardName = card.name,
            onConfirm = {
                vm.delete(card.id)
                toDelete = null
            },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun CardTile(
    card: LoyaltyCard,
    reorder: GridReorderState,
    dragging: Boolean,
    modifier: Modifier,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    // La tarjeta seleccionada se agranda un poco y sigue al dedo, por encima de las demas.
    val lifted = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                val t = reorder.translationOf(card.id)
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
        CardArt(
            storeId = card.storeId,
            imageFile = card.imageFile,
            modifier = Modifier
                .fillMaxWidth()
                // El toque lo recoge la rejilla (para distinguirlo de arrastrar); esto es para TalkBack.
                .semantics {
                    onClick(label = "Abrir la tarjeta ${card.name}") {
                        onOpen()
                        true
                    }
                },
            description = card.name,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                card.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
            Column {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Más opciones de ${card.name}")
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

@Composable
private fun EmptyState(modifier: Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Rounded.Style,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Aún no tienes tarjetas", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Vuelve atrás y pulsa «Añadir tarjeta» para guardar la primera.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
