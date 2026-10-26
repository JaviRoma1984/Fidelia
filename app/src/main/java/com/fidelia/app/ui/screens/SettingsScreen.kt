package com.fidelia.app.ui.screens

import androidx.compose.runtime.Composable
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.CenteredScreen

/**
 * Configuracion: se llega desde la rueda dentada de la cabecera de la pantalla principal.
 * Como los menus, deja el boton "Atras" arriba y centra el resto en la pantalla.
 */
@Composable
fun SettingsScreen(vm: FideliaViewModel) {
    CenteredScreen(title = "Configuración", onBack = vm::back) {
        ColorChoiceSection(vm)
    }
}
