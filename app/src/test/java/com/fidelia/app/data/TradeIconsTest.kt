package com.fidelia.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TradeIconsTest {

    @Test
    fun `los ids y los nombres de los oficios no se repiten`() {
        assertEquals(TradeIcons.all.size, TradeIcons.all.map { it.id }.toSet().size)
        assertEquals(TradeIcons.all.size, TradeIcons.all.map { it.label.lowercase() }.toSet().size)
    }

    @Test
    fun `hay una lista razonable de oficios y el primero es empresa`() {
        assertTrue(TradeIcons.all.size >= 20)
        assertEquals("empresa", TradeIcons.all.first().id)
    }

    @Test
    fun `emergencias es un oficio en rojo y se ve siempre en blanco sobre rojo`() {
        val emergencias = TradeIcons.find("emergencias")!!
        assertEquals("Emergencias", emergencias.label)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFDC2626), emergencias.color)
        assertTrue(emergencias.solid)
        // Los demas oficios solo se rellenan al elegirlos.
        assertTrue(TradeIcons.all.filter { it.id != "emergencias" }.none { it.solid })
    }

    @Test
    fun `estan los oficios pedidos por el usuario`() {
        for (id in listOf("emergencias", "medico", "clinica", "peluqueria")) {
            assertNotNull("falta el oficio $id", TradeIcons.find(id))
        }
        assertEquals("Peluquería", TradeIcons.find("peluqueria")!!.label)
        // El usuario no quiere un "Peluquero" aparte: con "Peluquería" (tijeras) le vale.
        assertNull(TradeIcons.find("peluquero"))
    }

    @Test
    fun `se encuentra por id y un id desconocido da null`() {
        assertNotNull(TradeIcons.find("fontanero"))
        assertEquals("Fontanero", TradeIcons.find("fontanero")!!.label)
        assertNull(TradeIcons.find("no-existe"))
        assertNull(TradeIcons.find(null))
    }

    @Test
    fun `el titulo de una tarjeta es la empresa o, si no hay, el titular`() {
        val conEmpresa = BusinessCard("1", "Acme", "Ana", "", "", null, null, 0)
        val sinEmpresa = BusinessCard("2", "", "Ana García", "", "", null, null, 0)
        assertEquals("Acme", conEmpresa.title)
        assertEquals("Ana García", sinEmpresa.title)
    }

    @Test
    fun `solo cuentan los telefonos rellenados`() {
        assertEquals(emptyList<String>(), BusinessCard("1", "A", "", "", "", null, null, 0).phones)
        assertEquals(listOf("600"), BusinessCard("1", "A", "", "600", "  ", null, null, 0).phones)
        assertEquals(listOf("600", "911"), BusinessCard("1", "A", "", "600", "911", null, null, 0).phones)
    }
}
