package com.fidelia.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EmailsTest {

    @Test
    fun `al escribir se quitan los espacios`() {
        assertEquals("ana@acme.es", Emails.sanitizeInput(" ana @ acme.es "))
    }

    @Test
    fun `al escribir no se pasa de la longitud maxima`() {
        assertEquals(Emails.MAX_LENGTH, Emails.sanitizeInput("a".repeat(300)).length)
    }

    @Test
    fun `reconoce la forma de un correo`() {
        assertTrue(Emails.looksValid("ana@acme.es"))
        assertTrue(Emails.looksValid("  ana.lopez+tarjeta@sub.acme.com "))
        assertFalse(Emails.looksValid(""))
        assertFalse(Emails.looksValid("ana"))
        assertFalse(Emails.looksValid("ana@acme"))
        assertFalse(Emails.looksValid("@acme.es"))
        assertFalse(Emails.looksValid("ana@@acme.es"))
        assertFalse(Emails.looksValid("ana lopez@acme.es"))
    }

    @Test
    fun `el correo se exporta y se importa junto con la tarjeta de visita`() {
        val out = ByteArrayOutputStream()
        CardTransfer.write(
            cards = emptyList(),
            readImage = { null },
            out = out,
            businessCards = listOf(
                BusinessCard("b", "Acme", "Ana", "600111222", "", null, null, 5L, email = "ana@acme.es"),
                BusinessCard("c", "Sin correo", "Luis", "", "", null, null, 6L),
            ),
        )
        val read = CardTransfer.read(ByteArrayInputStream(out.toByteArray()))
        assertEquals(listOf("ana@acme.es", ""), read.businessCards.map { it.email })
    }

    @Test
    fun `una tarjeta de visita sin correo guardado antes vale igual`() {
        val card = BusinessCard("b", "Acme", "Ana", "600", "", null, null, 1L)
        assertEquals("", card.email)
    }
}
