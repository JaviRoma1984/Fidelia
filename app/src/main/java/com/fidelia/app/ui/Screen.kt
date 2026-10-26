package com.fidelia.app.ui

sealed interface Screen {
    data object Home : Screen

    /** Menu de las tarjetas de fidelizacion: ver y anadir. */
    data object Loyalty : Screen

    /** Menu de las tarjetas de visita: ver y anadir. */
    data object Business : Screen

    /** Configuracion: combinaciones de color. */
    data object Settings : Screen

    // Tarjetas de fidelizacion
    data object Stores : Screen

    /** Elegir, para la tarjeta del formulario, uno de los logos de la lista de la app. */
    data object StoreChoice : Screen
    data object Form : Screen
    data object Scanner : Screen
    data object CardList : Screen
    data class Detail(val id: String) : Screen

    // Tarjetas de visita
    data object BusinessList : Screen
    data object BusinessForm : Screen
    data class BusinessDetail(val id: String) : Screen
}
