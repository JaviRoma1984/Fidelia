package com.fidelia.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fidelia.app.ui.screens.BusinessDetailScreen
import com.fidelia.app.ui.screens.BusinessFormScreen
import com.fidelia.app.ui.screens.BusinessListScreen
import com.fidelia.app.ui.screens.BusinessMenuScreen
import com.fidelia.app.ui.screens.CardDetailScreen
import com.fidelia.app.ui.screens.CardFormScreen
import com.fidelia.app.ui.screens.CardListScreen
import com.fidelia.app.ui.screens.HomeScreen
import com.fidelia.app.ui.screens.LoyaltyMenuScreen
import com.fidelia.app.ui.screens.ScannerScreen
import com.fidelia.app.ui.screens.SettingsScreen
import com.fidelia.app.ui.screens.StorePickerScreen

@Composable
fun FideliaApp(vm: FideliaViewModel) {
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.message) {
        val text = vm.message ?: return@LaunchedEffect
        snackbar.showSnackbar(text)
        vm.consumeMessage()
    }

    // El formulario y el escaner tienen su propio BackHandler (el mas reciente gana).
    BackHandler(enabled = vm.stack.size > 1) { vm.back() }

    // Surface fija el color del texto sobre el fondo (si no, el texto sale negro en modo oscuro).
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = vm.stack.last(),
            transitionSpec = {
                if (vm.forward) {
                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 8 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 8 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 4 } + fadeOut())
                }
            },
            label = "pantallas",
        ) { screen ->
            when (screen) {
                Screen.Home -> HomeScreen(vm)
                Screen.Loyalty -> LoyaltyMenuScreen(vm)
                Screen.Business -> BusinessMenuScreen(vm)
                Screen.Settings -> SettingsScreen(vm)
                Screen.Stores -> StorePickerScreen(vm)
                Screen.StoreChoice -> StorePickerScreen(vm, forDraft = true)
                Screen.Form -> CardFormScreen(vm)
                Screen.Scanner -> ScannerScreen(vm)
                Screen.CardList -> CardListScreen(vm)
                is Screen.Detail -> CardDetailScreen(vm, screen.id)
                Screen.BusinessList -> BusinessListScreen(vm)
                Screen.BusinessForm -> BusinessFormScreen(vm)
                is Screen.BusinessDetail -> BusinessDetailScreen(vm, screen.id)
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
        )
    }
    }
}
