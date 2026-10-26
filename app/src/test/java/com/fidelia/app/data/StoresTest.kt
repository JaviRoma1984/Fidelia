package com.fidelia.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoresTest {

    @Test
    fun `los ids no se repiten`() {
        val repetidos = Stores.all.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertTrue("ids repetidos: $repetidos", repetidos.isEmpty())
    }

    @Test
    fun `los nombres no se repiten`() {
        val repetidos = Stores.all.groupBy { it.name.lowercase() }.filterValues { it.size > 1 }.keys
        assertTrue("nombres repetidos: $repetidos", repetidos.isEmpty())
    }

    @Test
    fun `la busqueda ignora mayusculas y acentos`() {
        assertEquals("Álvaro Moreno", Stores.search("alvaro").single().name)
        assertEquals("Núñez de Arenas", Stores.search("NUNEZ").single().name)
        assertEquals("El Corte Inglés", Stores.search("corte ingles").single().name)
    }

    @Test
    fun `tiendanimal lleva Tiend en blanco y animal en verde`() {
        val tienda = Stores.find("tiendanimal")!!
        assertEquals("Tiendanimal", tienda.name)
        assertEquals(listOf("Tiend", "animal"), tienda.logoParts!!.map { it.text })
    }

    @Test
    fun `los logos genericos de biblioteca y gimnasio llevan un icono`() {
        assertEquals("Biblioteca", Stores.find("biblioteca")!!.name)
        assertTrue(Stores.find("biblioteca")!!.icon != null)
        assertEquals("Gimnasio", Stores.find("gimnasio")!!.name)
        assertTrue(Stores.find("gimnasio")!!.icon != null)
        assertEquals("Gimnasio", Stores.search("gimnas").single().name)
    }

    @Test
    fun `el listado esta ordenado sin tener en cuenta los acentos`() {
        val nombres = Stores.all.map { it.name }
        val a = nombres.indexOf("Alimerka")
        val b = nombres.indexOf("Álvaro Moreno")
        assertTrue(a in 0 until b)
    }
}
