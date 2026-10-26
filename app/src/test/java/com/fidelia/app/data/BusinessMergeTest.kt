package com.fidelia.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessMergeTest {

    private fun saved(company: String, holder: String, phone1: String, phone2: String = "") = BusinessCard(
        id = "id-$company-$holder-$phone1",
        company = company,
        holder = holder,
        phone1 = phone1,
        phone2 = phone2,
        tradeId = null,
        imageFile = null,
        createdAt = 1L,
    )

    private fun incoming(company: String, holder: String, phone1: String, phone2: String = "") = IncomingBusiness(
        company = company,
        holder = holder,
        phone1 = phone1,
        phone2 = phone2,
        tradeId = null,
        image = null,
        createdAt = 1L,
    )

    private fun names(plan: BusinessMergePlan) = plan.toAdd.map { it.finalCompany }

    @Test
    fun `misma empresa, titular y telefonos se omite`() {
        val plan = BusinessMerge.plan(
            listOf(saved("Fontanería Pérez", "Luis Pérez", "600111222")),
            listOf(incoming("Fontanería Pérez", "Luis Pérez", "600111222")),
        )
        assertTrue(plan.toAdd.isEmpty())
        assertEquals(1, plan.skipped)
    }

    @Test
    fun `misma empresa y titular con otros telefonos se anade distinguiendola`() {
        val plan = BusinessMerge.plan(
            listOf(saved("Fontanería Pérez", "Luis Pérez", "600111222")),
            listOf(incoming("Fontanería Pérez", "Luis Pérez", "699888777")),
        )
        assertEquals(listOf("Fontanería Pérez (importada)"), names(plan))
        assertTrue(plan.toAdd.single().renamed)
    }

    @Test
    fun `los telefonos se comparan por lo que se marca, no por como se escriben`() {
        val plan = BusinessMerge.plan(
            listOf(saved("Acme", "Ana", "600 11 22 33")),
            listOf(incoming("Acme", "Ana", "600-112233")),
        )
        assertTrue(plan.toAdd.isEmpty())
        assertEquals(1, plan.skipped)
    }

    @Test
    fun `el orden de los dos telefonos no importa`() {
        val plan = BusinessMerge.plan(
            listOf(saved("Acme", "Ana", "600111222", "911222333")),
            listOf(incoming("Acme", "Ana", "911222333", "600111222")),
        )
        assertTrue(plan.toAdd.isEmpty())
    }

    @Test
    fun `otra empresa se anade tal cual`() {
        val plan = BusinessMerge.plan(
            listOf(saved("Acme", "Ana", "600111222")),
            listOf(incoming("Electricidad López", "Marta López", "600111222")),
        )
        assertEquals(listOf("Electricidad López"), names(plan))
        assertFalse(plan.toAdd.single().renamed)
    }

    @Test
    fun `mayusculas y espacios no cuentan al comparar nombres`() {
        val plan = BusinessMerge.plan(
            listOf(saved("Acme", "Ana", "600111222")),
            listOf(incoming("  ACME ", "ana", "600111222")),
        )
        assertTrue(plan.toAdd.isEmpty())
    }

    @Test
    fun `importar dos veces el mismo archivo no duplica nada`() {
        val file = listOf(
            incoming("Acme", "Ana", "699000111"),
            incoming("Acme", "Ana", "699000222"),
            incoming("Zeta", "Zoe", "600000000"),
        )
        val before = listOf(saved("Acme", "Ana", "600111222"))
        val first = BusinessMerge.plan(before, file)
        val afterFirst = before + first.toAdd.map {
            saved(it.finalCompany, it.incoming.holder, it.incoming.phone1, it.incoming.phone2)
        }
        val second = BusinessMerge.plan(afterFirst, file)
        assertTrue("no debe anadir nada: ${names(second)}", second.toAdd.isEmpty())
        assertEquals(3, second.skipped)
    }

    @Test
    fun `varias con el mismo nombre y distintos telefonos se numeran`() {
        val plan = BusinessMerge.plan(
            listOf(saved("Acme", "Ana", "600111222")),
            listOf(incoming("Acme", "Ana", "699000111"), incoming("Acme", "Ana", "699000222")),
        )
        assertEquals(listOf("Acme (importada)", "Acme (importada 2)"), names(plan))
    }

    @Test
    fun `una tarjeta sin empresa tambien se distingue`() {
        val plan = BusinessMerge.plan(
            listOf(saved("", "Ana García", "600111222")),
            listOf(incoming("", "Ana García", "699000111")),
        )
        assertEquals(1, plan.toAdd.size)
        assertTrue(plan.toAdd.single().renamed)
        assertTrue(plan.toAdd.single().finalCompany.endsWith("(importada)"))
    }
}
