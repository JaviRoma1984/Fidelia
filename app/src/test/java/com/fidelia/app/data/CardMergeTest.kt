package com.fidelia.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardMergeTest {

    private fun saved(name: String, code: String) = LoyaltyCard(
        id = "id-$name-$code",
        name = name,
        storeId = null,
        imageFile = null,
        barcodeValue = code,
        barcodeFormat = "CODE_128",
        createdAt = 1L,
    )

    private fun incoming(name: String, code: String) = IncomingCard(
        name = name,
        storeId = null,
        image = null,
        barcodeValue = code,
        barcodeFormat = "CODE_128",
        createdAt = 1L,
    )

    private fun names(plan: MergePlan) = plan.toAdd.map { it.finalName }

    @Test
    fun `mismo nombre y mismo codigo se omite`() {
        val plan = CardMerge.plan(listOf(saved("Decathlon", "111")), listOf(incoming("Decathlon", "111")))
        assertTrue(plan.toAdd.isEmpty())
        assertEquals(1, plan.skipped)
    }

    @Test
    fun `mismo nombre con otro codigo se anade distinguiendola en el nombre`() {
        val plan = CardMerge.plan(listOf(saved("Decathlon", "111")), listOf(incoming("Decathlon", "222")))
        assertEquals(listOf("Decathlon (importada)"), names(plan))
        assertTrue(plan.toAdd.single().renamed)
        assertEquals(0, plan.skipped)
    }

    @Test
    fun `un nombre distinto se anade tal cual`() {
        val plan = CardMerge.plan(listOf(saved("Decathlon", "111")), listOf(incoming("Carrefour", "333")))
        assertEquals(listOf("Carrefour"), names(plan))
        assertFalse(plan.toAdd.single().renamed)
    }

    @Test
    fun `el mismo codigo con otro nombre tambien se anade`() {
        val plan = CardMerge.plan(listOf(saved("Decathlon", "111")), listOf(incoming("Mi gimnasio", "111")))
        assertEquals(listOf("Mi gimnasio"), names(plan))
    }

    @Test
    fun `mayusculas y espacios no cuentan al comparar nombres`() {
        val plan = CardMerge.plan(listOf(saved("Decathlon", "111")), listOf(incoming("  DECATHLON ", "111")))
        assertTrue(plan.toAdd.isEmpty())
        assertEquals(1, plan.skipped)
    }

    @Test
    fun `varias del mismo nombre con codigos distintos se numeran`() {
        val plan = CardMerge.plan(
            listOf(saved("Decathlon", "111")),
            listOf(incoming("Decathlon", "222"), incoming("Decathlon", "333")),
        )
        assertEquals(listOf("Decathlon (importada)", "Decathlon (importada 2)"), names(plan))
    }

    @Test
    fun `dentro del propio archivo una repetida exacta se omite`() {
        val plan = CardMerge.plan(emptyList(), listOf(incoming("Zara", "9"), incoming("Zara", "9")))
        assertEquals(listOf("Zara"), names(plan))
        assertEquals(1, plan.skipped)
    }

    @Test
    fun `importar dos veces el mismo archivo no duplica nada`() {
        val file = listOf(incoming("Decathlon", "111"), incoming("Decathlon", "222"), incoming("Zara", "9"))
        val first = CardMerge.plan(listOf(saved("Decathlon", "111")), file)
        // Tras la primera importacion la app tiene estas tarjetas.
        val afterFirst = listOf(saved("Decathlon", "111")) +
            first.toAdd.map { saved(it.finalName, it.incoming.barcodeValue) }
        val second = CardMerge.plan(afterFirst, file)
        assertTrue("no debe anadir nada: ${names(second)}", second.toAdd.isEmpty())
        assertEquals(3, second.skipped)
    }

    @Test
    fun `si el nombre importado ya trae el sufijo no se duplica`() {
        val plan = CardMerge.plan(
            listOf(saved("Decathlon", "111"), saved("Decathlon (importada)", "222")),
            listOf(incoming("Decathlon (importada)", "333")),
        )
        assertEquals(listOf("Decathlon (importada 2)"), names(plan))
    }
}
