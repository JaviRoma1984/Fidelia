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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fidelia.app.data.LoyaltyCard
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.AppTopBar
import com.fidelia.app.ui.components.CardArt
import com.fidelia.app.ui.components.ConfirmDeleteDialog

/** "Mostrar tarjetas": todas las tarjetas con su imagen y nombre; al pulsar una sale su codigo. */
@Composable
fun CardListScreen(vm: FideliaViewModel) {
    val cards = remember(vm.cards) { vm.cards.sortedBy { it.name.lowercase() } }
    var toDelete by remember { mutableStateOf<LoyaltyCard?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "Mis tarjetas", onBack = vm::back) },
        floatingActionButton = {
            if (cards.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = vm::startNew,
                    icon = { Icon(Icons.Rounded.AddCard, contentDescription = null) },
                    text = { Text("Añadir tarjeta") },
                )
            }
        },
    ) { padding ->
        if (cards.isEmpty()) {
            EmptyState(Modifier.padding(padding), onAdd = vm::startNew)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                items(cards, key = { it.id }) { card ->
                    CardTile(
                        card = card,
                        onOpen = { vm.openDetail(card.id) },
                        onEdit = { vm.startEdit(card.id) },
                        onDelete = { toDelete = card },
                    )
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
private fun CardTile(card: LoyaltyCard, onOpen: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Column {
        CardArt(
            storeId = card.storeId,
            imageFile = card.imageFile,
            modifier = Modifier.fillMaxWidth(),
            description = "Abrir la tarjeta ${card.name}",
            onClick = onOpen,
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
private fun EmptyState(modifier: Modifier, onAdd: () -> Unit) {
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
            "Añade la primera y la tendrás siempre a mano en la tienda.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Rounded.AddCard, contentDescription = null)
            Spacer(Modifier.size(10.dp))
            Text("Añadir tarjeta")
        }
    }
}
