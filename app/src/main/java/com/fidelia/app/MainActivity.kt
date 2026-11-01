package com.fidelia.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.IntentCompat
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
        // Si la ventana se recrea (cambio de tema, etc.) el intent original sigue ahi: solo se
        // importa cuando es una apertura nueva.
        if (savedInstanceState == null) importFrom(intent)
    }

    /** Ya hay una ventana abierta y otra app le manda un archivo .fidelia. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        importFrom(intent)
    }

    /** Abrir o compartir una copia .fidelia con Fidelia: se importa igual que con el boton Importar. */
    private fun importFrom(intent: Intent?) {
        if (intent == null) return
        // Al volver desde "recientes" Android repite el intent con el que se abrio la app.
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return
        val uri = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            else -> null
        } ?: return
        if (uri.scheme != "content") {
            viewModel.showMessage("No puedo abrir ese archivo desde ahí. Usa Importar en la pantalla principal.")
            return
        }
        viewModel.importFrom(uri)
    }
}
