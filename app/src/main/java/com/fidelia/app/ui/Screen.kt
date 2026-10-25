package com.fidelia.app.ui

sealed interface Screen {
    data object Home : Screen
    data object Stores : Screen
    data object Form : Screen
    data object Scanner : Screen
    data object CardList : Screen
    data class Detail(val id: String) : Screen
}
