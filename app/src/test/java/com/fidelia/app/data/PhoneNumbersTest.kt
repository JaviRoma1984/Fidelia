package com.fidelia.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumbersTest {

    @Test
    fun `al escribir solo se admiten los signos de un telefono`() {
        assertEquals("+34 600-11 22 33", PhoneNumbers.sanitizeInput("+34 600-11 22 33"))
        assertEquals("(985) 12 34 56", PhoneNumbers.sanitizeInput("(985) 12 34 56"))
        assertEquals("600112233", PhoneNumbers.sanitizeInput("600abc112233xyz"))
    }

    @Test
    fun `al escribir no se pasa de la longitud maxima`() {
        val largo = "6".repeat(50)
        assertEquals(PhoneNumbers.MAX_LENGTH, PhoneNumbers.sanitizeInput(largo).length)
    }

    @Test
    fun `lo que se marca no lleva espacios, guiones ni parentesis`() {
        assertEquals("+34600112233", PhoneNumbers.dialable("+34 600-11 22 33"))
        assertEquals("985123456", PhoneNumbers.dialable("(985) 12 34 56"))
    }

    @Test
    fun `el mas solo vale al principio`() {
        // Un "+" en medio no es parte de un numero: se descarta.
        assertEquals("346001", PhoneNumbers.dialable("34 600 + 1"))
        assertEquals("+34", PhoneNumbers.dialable("+34"))
    }

    @Test
    fun `se conservan los signos de marcacion`() {
        assertEquals("*#31#", PhoneNumbers.dialable("*#31#"))
    }

    @Test
    fun `solo se puede llamar si hay alguna cifra`() {
        assertTrue(PhoneNumbers.isCallable("600 11 22 33"))
        assertTrue(PhoneNumbers.isCallable("112"))
        assertFalse(PhoneNumbers.isCallable(""))
        assertFalse(PhoneNumbers.isCallable("   "))
        assertFalse(PhoneNumbers.isCallable("+"))
    }

    @Test
    fun `el telefono se muestra sin espacios sobrantes`() {
        assertEquals("600 11 22 33", PhoneNumbers.display("  600 11 22 33 "))
    }
}
