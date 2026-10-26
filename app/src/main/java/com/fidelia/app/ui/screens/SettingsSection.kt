package com.fidelia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidelia.app.ui.FideliaViewModel
import com.fidelia.app.ui.theme.AppPalette
import com.fidelia.app.ui.theme.Palettes

/** Configuracion: elegir la combinacion de colores de la app (las mismas que IntCom). */
@Composable
fun SettingsSection(vm: FideliaViewModel) {
    Column(
        Modifier.padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Configuración",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Elige la combinación de colores de la app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // null = la apariencia de siempre (indigo, clara u oscura segun el movil).
        val options: List<AppPalette?> = listOf(null) + Palettes.all
        options.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { palette ->
                    val id = palette?.id ?: Palettes.DEFAULT_ID
                    Swatch(
                        palette = palette,
                        selected = vm.themeId == id,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.setTheme(id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Swatch(palette: AppPalette?, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val name = palette?.name ?: "Por defecto"
    val from = palette?.previewFrom ?: Color(0xFF4338CA)
    val to = palette?.previewTo ?: Color(0xFF7C3AED)
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier
            .clip(shape)
            .background(colors.surface, shape)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.primary else colors.outlineVariant,
                shape = shape,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Brush.linearGradient(listOf(from, to))),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(24.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            name,
            color = colors.onSurface,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
        )
    }
}
