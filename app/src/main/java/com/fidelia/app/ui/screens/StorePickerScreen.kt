package com.fidelia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fidelia.app.data.Stores
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.AppTopBar
import com.fidelia.app.ui.components.CardArt
import com.fidelia.app.ui.components.CenteredTextField
import com.fidelia.app.ui.components.ScreenTitle

/**
 * La lista de tiendas con su logo. Con [forDraft] no se crea una tarjeta nueva: se elige el
 * logo de la tarjeta que ya se esta editando en el formulario.
 */
@Composable
fun StorePickerScreen(vm: FideliaViewModel, forDraft: Boolean = false) {
    var query by rememberSaveable { mutableStateOf("") }
    val stores = remember(query) { Stores.search(query) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // En la barra solo queda "Atras": el titulo va debajo, con el contenido.
        topBar = { AppTopBar(title = "", onBack = vm::back) },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        if (forDraft) "¿Ninguno te sirve?" else "¿No ves tu tienda en la lista?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    FilledTonalButton(
                        onClick = { if (forDraft) vm.chooseDraftStore(null) else vm.pickStore(null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Text(if (forDraft) "Dejar sin logo" else "Mi tienda no está en la lista")
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            ScreenTitle(if (forDraft) "Elige el logo" else "Elige la tienda")
            CenteredTextField(
                value = query,
                onValueChange = { query = it },
                label = "Buscar tienda…",
                leading = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailing = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (stores.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
                    Text(
                        "No encontramos esa tienda. Pulsa «Mi tienda no está en la lista» y añade una foto de tu tarjeta.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(stores, key = { it.id }) { store ->
                        CardArt(
                            storeId = store.id,
                            imageFile = null,
                            modifier = Modifier.fillMaxWidth(),
                            description = store.name,
                            onClick = { if (forDraft) vm.chooseDraftStore(store) else vm.pickStore(store) },
                        )
                    }
                }
            }
        }
    }
}
