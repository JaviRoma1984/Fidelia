package com.fidelia.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/**
 * Una combinacion de colores de la app. Son las mismas que usa IntCom en su
 * configuracion (assets/web/assets/css/style.css, bloques `body[data-theme=...]`),
 * con los mismos nombres de cada color: bg, line, surface, surface-2, text,
 * text-muted, border, primary, primary-text, accent y danger.
 */
data class AppPalette(
    val id: String,
    val name: String,
    /** Los temas oscuros de IntCom: oscuro, rojo y negro, lima y negro. */
    val isDark: Boolean,
    val bg: Color,
    val line: Color,
    val surface: Color,
    val surface2: Color,
    val text: Color,
    val textMuted: Color,
    val border: Color,
    val primary: Color,
    val primaryText: Color,
    val accent: Color,
    val danger: Color,
    /** `--primary-ink` de IntCom: tono del color principal que se lee sobre el fondo (solo "rojo"). */
    val primaryInk: Color = primary,
    /** Degradado de la muestra en la configuracion (el `.preview` de IntCom). */
    val previewFrom: Color,
    val previewTo: Color,
)

/** Colores de la cabecera de la pantalla principal. */
data class BrandColors(val headerStart: Color, val headerEnd: Color, val onHeader: Color)

object Palettes {
    /** La apariencia que tenia Fidelia antes: indigo, clara u oscura segun el movil. */
    const val DEFAULT_ID = "default"

    private fun c(hex: Long) = Color(0xFF000000 or hex)

    val all: List<AppPalette> = listOf(
        AppPalette(
            id = "claro", name = "Claro", isDark = false,
            bg = c(0xF2F4F8), line = c(0x3B62D9), surface = c(0xFFFFFF), surface2 = c(0xEEF1F6),
            text = c(0x1F2430), textMuted = c(0x6B7280), border = c(0xE2E6EE),
            primary = c(0x4F7CFF), primaryText = c(0xFFFFFF), accent = c(0x22C55E), danger = c(0xEF4444),
            previewFrom = c(0xF2F4F8), previewTo = c(0x4F7CFF),
        ),
        AppPalette(
            id = "oscuro", name = "Oscuro", isDark = true,
            bg = c(0x12131A), line = c(0x5470E0), surface = c(0x1B1D27), surface2 = c(0x23252F),
            text = c(0xEDEEF3), textMuted = c(0x9297A6), border = c(0x2D2F3B),
            primary = c(0x6F8DFF), primaryText = c(0xFFFFFF), accent = c(0x34D399), danger = c(0xF87171),
            previewFrom = c(0x12131A), previewTo = c(0x6F8DFF),
        ),
        AppPalette(
            id = "rosa", name = "Rosa", isDark = false,
            bg = c(0xFFF1F6), line = c(0xE03F70), surface = c(0xFFFFFF), surface2 = c(0xFFE4EE),
            text = c(0x3A2233), textMuted = c(0x9A6B83), border = c(0xFFD3E4),
            primary = c(0xFF5C8A), primaryText = c(0xFFFFFF), accent = c(0xFF8FAB), danger = c(0xE0457A),
            previewFrom = c(0xFFF1F6), previewTo = c(0xFF5C8A),
        ),
        AppPalette(
            id = "manzana", name = "Blanco y manzana", isDark = false,
            bg = c(0xF3FAEE), line = c(0x3F8A1C), surface = c(0xFFFFFF), surface2 = c(0xE9F5E0),
            text = c(0x1F2E18), textMuted = c(0x667D5A), border = c(0xD4E8C4),
            primary = c(0x58A82C), primaryText = c(0xFFFFFF), accent = c(0x8FD14F), danger = c(0xE5484D),
            previewFrom = c(0xFFFFFF), previewTo = c(0x7CC242),
        ),
        AppPalette(
            id = "naranja", name = "Blanco y naranja", isDark = false,
            bg = c(0xFFF6EE), line = c(0xCF5307), surface = c(0xFFFFFF), surface2 = c(0xFFEDDC),
            text = c(0x2E2118), textMuted = c(0x8A6D58), border = c(0xFBDCC0),
            primary = c(0xF06A12), primaryText = c(0xFFFFFF), accent = c(0xFFA24D), danger = c(0xD92D20),
            previewFrom = c(0xFFFFFF), previewTo = c(0xF88A2B),
        ),
        AppPalette(
            id = "morado", name = "Blanco y morado", isDark = false,
            bg = c(0xF6F2FF), line = c(0x6429CB), surface = c(0xFFFFFF), surface2 = c(0xECE6FB),
            text = c(0x231A3A), textMuted = c(0x75679A), border = c(0xDCD2F5),
            primary = c(0x7C3AED), primaryText = c(0xFFFFFF), accent = c(0xA78BFA), danger = c(0xDC2626),
            previewFrom = c(0xFFFFFF), previewTo = c(0x8B5CF6),
        ),
        AppPalette(
            id = "rojo", name = "Rojo y negro", isDark = true,
            bg = c(0x1C1C20), line = c(0x8A1C38), surface = c(0x28282D), surface2 = c(0x34343A),
            text = c(0xF4F1F1), textMuted = c(0xB3AAAA), border = c(0x44393C),
            primary = c(0x8A1C38), primaryText = c(0xFFFFFF), accent = c(0xB8364F), danger = c(0xFF8A5C),
            primaryInk = c(0xD95B78),
            previewFrom = c(0x28282D), previewTo = c(0x8A1C38),
        ),
        AppPalette(
            id = "lima", name = "Lima y negro", isDark = true,
            bg = c(0x141414), line = c(0x76A633), surface = c(0x1F1F1F), surface2 = c(0x2A2A2A),
            text = c(0xF2F2F2), textMuted = c(0xA6A6A6), border = c(0x383838),
            primary = c(0x86BD3C), primaryText = c(0x0A0C06), accent = c(0xB3D98A), danger = c(0xF87171),
            previewFrom = c(0x1F1F1F), previewTo = c(0x86BD3C),
        ),
    )

    /** La paleta con ese id, o null si es la predeterminada (o un id que ya no existe). */
    fun byId(id: String?): AppPalette? = all.firstOrNull { it.id == id }

    fun isKnown(id: String?): Boolean = id == DEFAULT_ID || byId(id) != null
}

/** Relacion de contraste entre dos colores (WCAG): 1 = igual, 21 = blanco sobre negro. */
internal fun contrast(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}

/** [preferred] si se lee bien sobre [background]; si no, blanco o casi negro, el que mejor se lea. */
internal fun readableOn(background: Color, preferred: Color): Color {
    if (contrast(preferred, background) >= 4.5f) return preferred
    return listOf(Color.White, Color(0xFF0A0C06)).maxBy { contrast(it, background) }
}

/**
 * El tono del color principal que mejor se lee sobre el fondo de la combinacion.
 * En Material el mismo "primary" sirve de fondo de boton y de color de texto, asi
 * que se elige entre los tonos de IntCom (primary, primary-ink y line) el de mayor contraste.
 */
internal fun AppPalette.ink(): Color =
    listOf(primary, primaryInk, line).distinct().maxBy { contrast(it, bg) }

internal fun AppPalette.toColorScheme(): ColorScheme {
    val ink = ink()
    val onInk = readableOn(ink, primaryText)
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = ink,
        onPrimary = onInk,
        primaryContainer = lerp(surface, ink, 0.16f),
        onPrimaryContainer = text,
        secondary = accent,
        onSecondary = readableOn(accent, primaryText),
        secondaryContainer = lerp(surface, accent, 0.22f),
        onSecondaryContainer = text,
        tertiary = accent,
        onTertiary = readableOn(accent, primaryText),
        background = bg,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surface2,
        onSurfaceVariant = textMuted,
        surfaceTint = ink,
        // `--line-soft` de IntCom: mezcla del color propio del tema con el borde neutro.
        outline = lerp(border, line, 0.55f),
        outlineVariant = border,
        error = danger,
        onError = readableOn(danger, Color.White),
        errorContainer = lerp(surface, danger, 0.18f),
        onErrorContainer = text,
        inverseSurface = text,
        inverseOnSurface = bg,
        surfaceContainerLowest = bg,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface2,
        surfaceContainerHighest = lerp(surface2, border, 0.5f),
    )
}

internal fun AppPalette.toBrandColors(): BrandColors {
    val start = ink()
    val end = lerp(start, accent, 0.6f)
    return BrandColors(start, end, readableOn(lerp(start, end, 0.5f), Color.White))
}
