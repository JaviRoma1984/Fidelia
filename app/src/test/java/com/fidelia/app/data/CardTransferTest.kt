package com.fidelia.app.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CardTransferTest {

    private val photo = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

    private fun card(name: String, code: String, storeId: String? = null, image: String? = null) = LoyaltyCard(
        id = "x",
        name = name,
        storeId = storeId,
        imageFile = image,
        barcodeValue = code,
        barcodeFormat = "EAN_13",
        createdAt = 1234L,
    )

    private fun export(cards: List<LoyaltyCard>): ByteArray {
        val out = ByteArrayOutputStream()
        CardTransfer.write(cards, { name -> if (name == "foto.jpg") photo else null }, out)
        return out.toByteArray()
    }

    private fun zip(entries: Map<String, String>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { z ->
            for ((name, text) in entries) {
                z.putNextEntry(ZipEntry(name))
                z.write(text.toByteArray())
                z.closeEntry()
            }
        }
        return out.toByteArray()
    }

    @Test
    fun `lo exportado se lee igual, con la imagen`() {
        val bytes = export(
            listOf(
                card("Decathlon", "5901234123457", storeId = "decathlon"),
                card("Mi tienda", "TEST-123", image = "foto.jpg"),
            )
        )
        val read = CardTransfer.read(ByteArrayInputStream(bytes))
        assertEquals(0, read.invalid)
        assertEquals(2, read.cards.size)

        val first = read.cards[0]
        assertEquals("Decathlon", first.name)
        assertEquals("decathlon", first.storeId)
        assertEquals("5901234123457", first.barcodeValue)
        assertEquals("EAN_13", first.barcodeFormat)
        assertNull(first.image)

        val second = read.cards[1]
        assertEquals("Mi tienda", second.name)
        assertNull(second.storeId)
        assertArrayEquals(photo, second.image)
    }

    @Test
    fun `una imagen que falta en el disco no impide exportar la tarjeta`() {
        val read = CardTransfer.read(ByteArrayInputStream(export(listOf(card("Zara", "9", image = "no-existe.jpg")))))
        assertEquals(1, read.cards.size)
        assertNull(read.cards.single().image)
    }

    @Test
    fun `sin tarjetas tambien es un archivo valido`() {
        val read = CardTransfer.read(ByteArrayInputStream(export(emptyList())))
        assertTrue(read.cards.isEmpty())
    }

    @Test
    fun `un archivo que no es un zip se rechaza`() {
        try {
            CardTransfer.read(ByteArrayInputStream("hola, esto no es un zip".toByteArray()))
            fail("debia lanzar IOException")
        } catch (e: IOException) {
            // esperado
        }
    }

    @Test
    fun `un zip de otra aplicacion se rechaza`() {
        val bytes = zip(mapOf("cards.json" to """{"app":"Otra","version":1,"cards":[]}"""))
        try {
            CardTransfer.read(ByteArrayInputStream(bytes))
            fail("debia lanzar IOException")
        } catch (e: IOException) {
            // esperado
        }
    }

    @Test
    fun `un zip sin cards json se rechaza`() {
        try {
            CardTransfer.read(ByteArrayInputStream(zip(mapOf("otra-cosa.txt" to "hola"))))
            fail("debia lanzar IOException")
        } catch (e: IOException) {
            // esperado
        }
    }

    @Test
    fun `las tarjetas sin nombre o sin codigo se cuentan como no validas`() {
        val json = """
            {"app":"Fidelia","version":1,"cards":[
              {"name":"Bien","barcodeValue":"111"},
              {"name":"   ","barcodeValue":"222"},
              {"name":"Sin codigo","barcodeValue":""},
              {"barcodeValue":"333"}
            ]}
        """.trimIndent()
        val read = CardTransfer.read(ByteArrayInputStream(zip(mapOf("cards.json" to json))))
        assertEquals(listOf("Bien"), read.cards.map { it.name })
        assertEquals(3, read.invalid)
        assertEquals("CODE_128", read.cards.single().barcodeFormat)
    }

    @Test
    fun `solo se leen cards json e imagenes, otras entradas se ignoran`() {
        val json = """{"app":"Fidelia","version":1,"cards":[{"name":"Zara","barcodeValue":"9"}]}"""
        val bytes = zip(mapOf("cards.json" to json, "../../evil.txt" to "x", "otra/cosa.bin" to "y"))
        val read = CardTransfer.read(ByteArrayInputStream(bytes))
        assertEquals(1, read.cards.size)
    }

    private fun business(company: String, phone1: String, tradeId: String? = null, image: String? = null) =
        BusinessCard("b", company, "Titular $company", phone1, "", tradeId, image, 99L)

    @Test
    fun `las tarjetas de visita se exportan y se leen igual, con su foto`() {
        val out = ByteArrayOutputStream()
        CardTransfer.write(
            cards = listOf(card("Decathlon", "5901234123457")),
            readImage = { name -> if (name == "foto.jpg") photo else null },
            out = out,
            businessCards = listOf(
                business("Fontanería Pérez", "600111222", tradeId = "fontanero"),
                business("Con foto", "911222333", image = "foto.jpg"),
            ),
        )
        val read = CardTransfer.read(ByteArrayInputStream(out.toByteArray()))

        assertEquals(1, read.cards.size)
        assertEquals(2, read.businessCards.size)
        assertEquals(0, read.invalidBusiness)

        val first = read.businessCards[0]
        assertEquals("Fontanería Pérez", first.company)
        assertEquals("Titular Fontanería Pérez", first.holder)
        assertEquals("600111222", first.phone1)
        assertEquals("fontanero", first.tradeId)
        assertNull(first.image)

        val second = read.businessCards[1]
        assertNull(second.tradeId)
        assertArrayEquals(photo, second.image)
    }

    @Test
    fun `un archivo antiguo, sin tarjetas de visita, se sigue leyendo`() {
        val json = """{"app":"Fidelia","version":1,"cards":[{"name":"Zara","barcodeValue":"9"}]}"""
        val read = CardTransfer.read(ByteArrayInputStream(zip(mapOf("cards.json" to json))))
        assertEquals(1, read.cards.size)
        assertTrue(read.businessCards.isEmpty())
        assertEquals(0, read.invalidBusiness)
    }

    @Test
    fun `las tarjetas de visita sin empresa ni titular no son validas`() {
        val json = """
            {"app":"Fidelia","version":1,"cards":[],"businessCards":[
              {"company":"Acme","phone1":"600"},
              {"holder":"Solo titular"},
              {"company":"  ","holder":""},
              {"phone1":"123"}
            ]}
        """.trimIndent()
        val read = CardTransfer.read(ByteArrayInputStream(zip(mapOf("cards.json" to json))))
        assertEquals(listOf("Acme", ""), read.businessCards.map { it.company })
        assertEquals(2, read.invalidBusiness)
    }

    @Test
    fun `los telefonos importados se recortan a la longitud maxima`() {
        val json = """{"app":"Fidelia","version":1,"cards":[],"businessCards":[{"company":"A","phone1":"${"6".repeat(80)}"}]}"""
        val read = CardTransfer.read(ByteArrayInputStream(zip(mapOf("cards.json" to json))))
        assertEquals(PhoneNumbers.MAX_LENGTH, read.businessCards.single().phone1.length)
    }

    @Test
    fun `el nombre del archivo sugerido lleva la fecha y la extension`() {
        val name = CardTransfer.suggestedFileName(0L)
        assertTrue(name, name.startsWith("Fidelia-tarjetas-19") && name.endsWith(".fidelia"))
    }
}
