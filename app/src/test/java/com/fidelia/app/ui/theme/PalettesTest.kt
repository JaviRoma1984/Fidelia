package com.fidelia.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PalettesTest {

    @Test
    fun `estan las ocho combinaciones de IntCom`() {
        assertEquals(
            listOf("claro", "oscuro", "rosa", "manzana", "naranja", "morado", "rojo", "lima"),
            Palettes.all.map { it.id },
        )
        assertEquals(
            listOf(
                "Claro", "Oscuro", "Rosa", "Blanco y manzana", "Blanco y naranja",
                "Blanco y morado", "Rojo y negro", "Lima y negro",
            ),
            Palettes.all.map { it.name },
        )
    }

    @Test
    fun `los temas oscuros son los de IntCom`() {
        assertEquals(setOf("oscuro", "rojo", "lima"), Palettes.all.filter { it.isDark }.map { it.id }.toSet())
    }

    @Test
    fun `los ids no se repiten y el predeterminado no choca con ninguno`() {
        assertEquals(Palettes.all.size, Palettes.all.map { it.id }.toSet().size)
        assertNull(Palettes.byId(Palettes.DEFAULT_ID))
        assertTrue(Palettes.isKnown(Palettes.DEFAULT_ID))
    }

    @Test
    fun `un id desconocido no es valido`() {
        assertNull(Palettes.byId("no-existe"))
        assertFalse(Palettes.isKnown("no-existe"))
        assertFalse(Palettes.isKnown(null))
        assertNotNull(Palettes.byId("lima"))
    }

    @Test
    fun `todas las combinaciones se leen bien`() {
        for (p in Palettes.all) {
            val scheme = p.toColorScheme()
            assertTrue("${p.id}: texto sobre fondo", contrast(p.text, p.bg) >= 4.5f)
            assertTrue("${p.id}: texto sobre superficie", contrast(p.text, p.surface) >= 4.5f)
            assertTrue("${p.id}: texto apagado sobre fondo", contrast(p.textMuted, p.bg) >= 3.0f)
            assertTrue("${p.id}: color principal sobre fondo", contrast(scheme.primary, p.bg) >= 3.0f)
            assertTrue("${p.id}: texto del boton principal", contrast(scheme.onPrimary, scheme.primary) >= 4.5f)
            assertTrue("${p.id}: texto del boton de peligro", contrast(scheme.onError, scheme.error) >= 4.5f)
            assertEquals("${p.id}: fondo", p.bg, scheme.background)
        }
    }

    @Test
    fun `el rojo y negro usa su tono claro para que se lea`() {
        val rojo = Palettes.byId("rojo")!!
        assertEquals(rojo.primaryInk, rojo.toColorScheme().primary)
    }

    @Test
    fun `la cabecera tiene texto legible sobre su degradado`() {
        for (p in Palettes.all) {
            val brand = p.toBrandColors()
            val middle = androidx.compose.ui.graphics.lerp(brand.headerStart, brand.headerEnd, 0.5f)
            assertTrue("${p.id}: texto de la cabecera", contrast(brand.onHeader, middle) >= 4.5f)
        }
    }
}
