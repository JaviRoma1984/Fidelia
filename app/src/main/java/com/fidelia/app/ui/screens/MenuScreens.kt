package com.fidelia.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Style
import androidx.compose.runtime.Composable
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.ActionCard
import com.fidelia.app.ui.components.CenteredScreen

/** Tarjetas de fidelizacion: dos botones, ver las guardadas y anadir una nueva. */
@Composable
fun LoyaltyMenuScreen(vm: FideliaViewModel) {
    CenteredScreen(title = "Tarjetas de fidelización", onBack = vm::back) {
        ActionCard(
            title = "Ver tarjetas",
            subtitle = countText(vm.cards.size),
            icon = Icons.Rounded.Style,
            onClick = vm::openCardList,
            tall = true,
        )
        ActionCard(
            title = "Añadir tarjeta",
            subtitle = "Guarda una tarjeta nueva en tu móvil",
            icon = Icons.Rounded.AddCard,
            onClick = vm::startNew,
            tall = true,
        )
    }
}

/** Tarjetas de visita: dos botones, ver las guardadas y anadir una nueva. */
@Composable
fun BusinessMenuScreen(vm: FideliaViewModel) {
    CenteredScreen(title = "Tarjetas de visita", onBack = vm::back) {
        ActionCard(
            title = "Ver tarjetas de visita",
            subtitle = countText(vm.businessCards.size),
            icon = Icons.Rounded.Badge,
            onClick = vm::openBusinessList,
            tall = true,
        )
        ActionCard(
            title = "Añadir tarjeta de visita",
            subtitle = "Con un icono de oficio o una foto de la tarjeta",
            icon = Icons.Rounded.PersonAdd,
            onClick = vm::startNewBusiness,
            tall = true,
        )
    }
}
