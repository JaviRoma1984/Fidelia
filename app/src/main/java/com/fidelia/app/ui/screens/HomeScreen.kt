package com.fidelia.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidelia.app.R
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.components.ActionCard
import com.fidelia.app.ui.theme.LocalBrandColors

/**
 * Inter (licencia SIL OFL 1.1, Rasmus Andersson): una tipografia sobria y moderna para el
 * nombre de la app. Los ficheros son los que trae Android Studio.
 */
private val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

/** Hueco entre la cabecera y la barra de la hora; el mismo que hay entre los botones de copia y la base. */
private val ScreenMargin = 12.dp

@Composable
fun HomeScreen(vm: FideliaViewModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        // El contenido ocupa como minimo toda la pantalla: la cabecera se estira con el
        // espacio que sobra, de modo que abajo queda el mismo margen que arriba. Si el
        // contenido no cabe (letra muy grande, pantalla pequena), la pantalla se desplaza.
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(min = viewportHeight)
                .verticalScroll(rememberScrollState())
        ) {
            // La cabecera empieza debajo de la barra de la hora y las notificaciones del movil.
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(Modifier.height(ScreenMargin))
            Header(
                onSettings = vm::openSettings,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 170.dp),
            )
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ActionCard(
                    title = "Tarjetas de fidelización",
                    subtitle = countText(vm.cards.size),
                    icon = Icons.Rounded.Style,
                    onClick = vm::openLoyaltyMenu,
                )
                ActionCard(
                    title = "Tarjetas de visita",
                    subtitle = countText(vm.businessCards.size),
                    icon = Icons.Rounded.Badge,
                    onClick = vm::openBusinessMenu,
                )
            }
            TransferSection(vm)
            // Margen de abajo igual al de arriba, medido desde la barra de botones del movil.
            Spacer(Modifier.height(ScreenMargin))
            Spacer(Modifier.navigationBarsPadding())
        }
        // Fondo fijo bajo la hora y los iconos del movil: al desplazar, el contenido no se mezcla con ellos.
        Box(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

internal fun countText(count: Int): String = when (count) {
    0 -> "Aún no hay tarjetas guardadas"
    1 -> "1 tarjeta guardada"
    else -> "$count tarjetas guardadas"
}

/** Cabecera: logo, nombre y una frase corta, con los colores de la combinacion elegida y la rueda de configuracion. */
@Composable
private fun Header(onSettings: () -> Unit, modifier: Modifier = Modifier) {
    val brand = LocalBrandColors.current
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(brand.headerStart, brand.headerEnd)))
    ) {
        // Todo lo de dentro se dimensiona a partir de la altura de la cabecera, que se estira
        // con la pantalla: asi el contenido nunca se ve pequeno dentro de un recuadro grande.
        val h = maxHeight
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                brand.onHeader.copy(alpha = 0.10f),
                radius = size.height * 1.0f,
                center = Offset(size.width * 0.98f, size.height * 0.02f),
            )
            drawCircle(
                brand.onHeader.copy(alpha = 0.07f),
                radius = size.height * 0.75f,
                center = Offset(size.width * 0.0f, size.height * 1.05f),
            )
        }
        if (h >= 230.dp) {
            // Cabecera alta: logo, nombre y frase apilados y centrados, a escala con la altura.
            val titleSize = (h.value * 0.20f).sp
            Column(
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LogoMark(Modifier.size(h * 0.46f), barColor = brand.headerStart)
                Spacer(Modifier.height(h * 0.04f))
                Text(
                    "Fidelia",
                    style = TextStyle(
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = titleSize,
                        lineHeight = titleSize * 1.08f,
                        letterSpacing = (-1.5).sp,
                    ),
                    color = brand.onHeader,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Tus tarjetas, siempre a mano",
                    style = TextStyle(
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = (h.value * 0.062f).coerceAtLeast(16f).sp,
                        letterSpacing = 0.3.sp,
                    ),
                    color = brand.onHeader.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            // Cabecera baja (pantalla pequena o letra muy grande): en una fila.
            val titleSize = (h.value * 0.24f).sp
            Row(
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LogoMark(Modifier.size(h * 0.5f), barColor = brand.headerStart)
                Spacer(Modifier.width(20.dp))
                Column {
                    Text(
                        "Fidelia",
                        style = TextStyle(
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = titleSize,
                            lineHeight = titleSize * 1.1f,
                            letterSpacing = (-1.2).sp,
                        ),
                        color = brand.onHeader,
                    )
                    Text(
                        "Tus tarjetas,\nsiempre a mano",
                        style = TextStyle(
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            letterSpacing = 0.2.sp,
                        ),
                        color = brand.onHeader.copy(alpha = 0.92f),
                    )
                }
            }
        }
        // Boton redondo con la rueda dentada: lleva a la configuracion.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(brand.onHeader.copy(alpha = 0.18f))
                .clickable(role = Role.Button, onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = "Configuración",
                tint = brand.onHeader,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

/** El mismo logo que el icono de la app: una tarjeta blanca con un codigo de barras. */
@Composable
private fun LogoMark(modifier: Modifier = Modifier, barColor: Color = Color(0xFF4338CA)) {
    Canvas(modifier) {
        val s = size.minDimension
        drawRoundRect(Color.White.copy(alpha = 0.20f), cornerRadius = CornerRadius(s * 0.28f))

        val cardW = s * 0.68f
        val cardH = s * 0.48f
        val left = (s - cardW) / 2f
        val top = (s - cardH) / 2f
        drawRoundRect(Color.White, Offset(left, top), Size(cardW, cardH), CornerRadius(s * 0.10f))

        // Barras: (anchura de la barra, hueco que sigue), en unidades.
        val pattern = listOf(2f to 1f, 1f to 1f, 3f to 1f, 1f to 1f, 2f to 1f, 1f to 1f, 3f to 1f, 1f to 1f, 2f to 0f)
        val units = pattern.sumOf { (bar, gap) -> (bar + gap).toDouble() }.toFloat()
        val innerW = cardW * 0.70f
        val unit = innerW / units
        val barH = cardH * 0.52f
        val barTop = top + (cardH - barH) / 2f
        var x = left + (cardW - innerW) / 2f
        for ((bar, gap) in pattern) {
            drawRect(barColor, Offset(x, barTop), Size(bar * unit, barH))
            x += (bar + gap) * unit
        }
    }
}
