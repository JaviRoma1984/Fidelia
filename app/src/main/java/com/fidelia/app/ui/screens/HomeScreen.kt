package com.fidelia.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.theme.LocalBrandColors

@Composable
fun HomeScreen(vm: FideliaViewModel) {
    val count = vm.cards.size
    val countText = when (count) {
        0 -> "Aún no hay tarjetas guardadas"
        1 -> "1 tarjeta guardada"
        else -> "$count tarjetas guardadas"
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // La cabecera empieza debajo de la barra de la hora y las notificaciones del movil.
        Header(
            Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
        )
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ActionCard(
                title = "Mostrar tarjetas",
                subtitle = countText,
                icon = Icons.Rounded.Style,
                filled = true,
                onClick = vm::openCardList,
            )
            ActionCard(
                title = "Añadir tarjeta",
                subtitle = "Guarda una tarjeta nueva en tu móvil",
                icon = Icons.Rounded.AddCard,
                filled = false,
                onClick = vm::startNew,
            )
        }
        TransferSection(vm)
        Spacer(Modifier.height(28.dp))
        SettingsSection(vm)
        Spacer(Modifier.height(28.dp))
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

/** Cabecera de la pantalla principal: logo, nombre y una frase corta, con los colores de la combinacion elegida. */
@Composable
private fun Header(modifier: Modifier = Modifier) {
    val brand = LocalBrandColors.current
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(brand.headerStart, brand.headerEnd)))
    ) {
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
        Row(
            Modifier.padding(horizontal = 22.dp, vertical = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogoMark(Modifier.size(84.dp), barColor = brand.headerStart)
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    "Fidelia",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = brand.onHeader,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Tus tarjetas,\nsiempre a mano",
                    style = MaterialTheme.typography.bodyLarge,
                    color = brand.onHeader.copy(alpha = 0.92f),
                )
            }
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

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    filled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val container = if (filled) colors.primary else colors.surface
    val content = if (filled) colors.onPrimary else colors.onSurface
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = container,
        contentColor = content,
        shadowElevation = if (filled) 8.dp else 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (filled) Color.White.copy(alpha = 0.18f) else colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (filled) colors.onPrimary else colors.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = content.copy(alpha = 0.8f),
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null)
        }
    }
}
