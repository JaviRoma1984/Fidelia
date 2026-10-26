package com.fidelia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fidelia.app.data.BusinessCard
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.AppTopBar
import com.fidelia.app.ui.components.BusinessArt
import com.fidelia.app.ui.components.ConfirmDeleteDialog
import com.fidelia.app.ui.components.ReorderTile
import com.fidelia.app.ui.components.ScreenTitle
import com.fidelia.app.ui.components.gridReorderGestures
import com.fidelia.app.ui.components.rememberGridReorderState

/**
 * "Ver tarjetas de visita": igual que la lista de fidelizacion. Al pulsar una se abre su
 * ficha; manteniendola pulsada se arrastra para cambiarla de sitio. En la barra de arriba
 * solo queda "Atras": el titulo y el contenido van debajo.
 */
@Composable
fun BusinessListScreen(vm: FideliaViewModel) {
    var working by remember(vm.businessCards) { mutableStateOf(vm.businessCards) }
    var toDelete by remember { mutableStateOf<BusinessCard?>(null) }
    val gridState = rememberLazyGridState()
    val reorder = rememberGridReorderState(gridState) { from, to ->
        working = working.toMutableList().apply { add(to, removeAt(from)) }
    }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "", onBack = vm::back) },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            ScreenTitle("Tarjetas de visita")
            if (working.isEmpty()) {
                EmptyState(
                    modifier = Modifier.weight(1f),
                    text = "Vuelve atrás y pulsa «Añadir tarjeta de visita» para guardar la primera.",
                )
            } else {
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
                            onTap = { key -> vm.openBusinessDetail(key as String) },
                            onDrop = { vm.reorderBusiness(working.map { it.id }) },
                        ),
                ) {
                    items(working, key = { it.id }) { card ->
                        val dragging = reorder.draggingKey == card.id
                        ReorderTile(
                            id = card.id,
                            title = card.title,
                            // La empresa va de titulo; debajo, el titular de la tarjeta.
                            subtitle = if (card.company.isNotBlank()) card.holder else null,
                            openLabel = "Abrir la tarjeta de visita de ${card.title}",
                            reorder = reorder,
                            dragging = dragging,
                            modifier = if (dragging) Modifier else Modifier.animateItem(),
                            onOpen = { vm.openBusinessDetail(card.id) },
                            onEdit = { vm.startEditBusiness(card.id) },
                            onDelete = { toDelete = card },
                            art = { m -> BusinessArt(card.tradeId, card.imageFile, m, description = card.title) },
                        )
                    }
                }
            }
        }
    }

    toDelete?.let { card ->
        ConfirmDeleteDialog(
            cardName = card.title,
            what = "sus datos",
            onConfirm = {
                vm.deleteBusiness(card.id)
                toDelete = null
            },
            onDismiss = { toDelete = null },
        )
    }
}
