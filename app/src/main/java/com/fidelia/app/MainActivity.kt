package com.fidelia.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.fidelia.app.ui.FideliaApp
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.theme.FideliaTheme
import com.fidelia.app.ui.theme.isFideliaDark

class MainActivity : ComponentActivity() {

    private val viewModel: FideliaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = isFideliaDark(viewModel.themeId)
            // Los iconos de la barra de arriba (hora, bateria) deben verse sobre el fondo elegido.
            LaunchedEffect(dark) {
                val bars = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            FideliaTheme(viewModel.themeId) {
                FideliaApp(viewModel)
            }
        }
    }
}
