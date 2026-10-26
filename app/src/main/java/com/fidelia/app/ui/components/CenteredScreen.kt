package com.fidelia.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla con el boton "Atras" arriba, igual que las demas, y el resto (el titulo y el
 * contenido) centrado en alto y en ancho respecto a la pantalla entera del movil (no solo
 * respecto al hueco bajo la barra). Si no cabe (letra muy grande, pantalla pequena), la
 * pantalla se desplaza. La usan los menus de tarjetas y la configuracion.
 */
@Composable
fun CenteredScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // En la barra solo queda "Atras"; el titulo baja con el resto del contenido.
        topBar = { AppTopBar(title = "", onBack = onBack) },
    ) { padding ->
        // El contenido de Scaffold ocupa toda la pantalla; el padding de arriba es lo que mide la barra.
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val viewportHeight = maxHeight
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = viewportHeight)
                    .verticalScroll(rememberScrollState())
                    // Mismo margen arriba y abajo: el centro sigue siendo el de la pantalla,
                    // y si hay que desplazar, el contenido nunca queda bajo la barra.
                    .padding(vertical = padding.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                    content()
                }
            }
        }
    }
}
