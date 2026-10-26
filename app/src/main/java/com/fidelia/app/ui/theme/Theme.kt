package com.fidelia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF4338CA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0F766E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    background = Color(0xFFF5F6FB),
    onBackground = Color(0xFF14152B),
    surface = Color.White,
    onSurface = Color(0xFF14152B),
    surfaceVariant = Color(0xFFE9EBF5),
    onSurfaceVariant = Color(0xFF4A4D68),
    outline = Color(0xFF8A8DA8),
    outlineVariant = Color(0xFFD5D8E8),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF0F1020),
    onBackground = Color(0xFFE8E9F5),
    surface = Color(0xFF1A1C32),
    onSurface = Color(0xFFE8E9F5),
    surfaceVariant = Color(0xFF2A2D4A),
    onSurfaceVariant = Color(0xFFB9BCD6),
    outline = Color(0xFF8A8DA8),
    outlineVariant = Color(0xFF3A3D5C),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
)

private val FideliaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val base = Typography()
private val FideliaTypography = Typography(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = base.bodyLarge.copy(fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = base.bodyMedium.copy(fontSize = 15.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp),
)

private val DefaultBrand = BrandColors(
    headerStart = Color(0xFF4338CA),
    headerEnd = Color(0xFF7C3AED),
    onHeader = Color.White,
)

/** Colores de la cabecera de la pantalla principal (cambian con la combinacion elegida). */
val LocalBrandColors = staticCompositionLocalOf { DefaultBrand }

/** Si la apariencia es oscura: la de la combinacion elegida o, en la predeterminada, la del movil. */
@Composable
fun isFideliaDark(paletteId: String): Boolean =
    Palettes.byId(paletteId)?.isDark ?: isSystemInDarkTheme()

/**
 * [paletteId] es una de [Palettes.all] (las combinaciones de IntCom) o [Palettes.DEFAULT_ID]
 * para la apariencia de siempre (indigo, clara u oscura segun el movil).
 */
@Composable
fun FideliaTheme(paletteId: String = Palettes.DEFAULT_ID, content: @Composable () -> Unit) {
    val palette = Palettes.byId(paletteId)
    val systemDark = isSystemInDarkTheme()
    val scheme = palette?.toColorScheme() ?: if (systemDark) DarkColors else LightColors
    val brand = palette?.toBrandColors() ?: DefaultBrand
    CompositionLocalProvider(LocalBrandColors provides brand) {
        MaterialTheme(
            colorScheme = scheme,
            shapes = FideliaShapes,
            typography = FideliaTypography,
            content = content,
        )
    }
}
