package com.fidelia.app.data

/** Una tarjeta de visita leida de un archivo de copia, todavia sin guardar en la app. */
class IncomingBusiness(
    val company: String,
    val holder: String,
    val phone1: String,
    val phone2: String,
    val tradeId: String?,
    val image: ByteArray?,
    val createdAt: Long,
    val email: String = "",
)

class PlannedBusiness(val incoming: IncomingBusiness, val finalCompany: String, val renamed: Boolean)

class BusinessMergePlan(val toAdd: List<PlannedBusiness>, val skipped: Int)

/**
 * Reglas al importar tarjetas de visita (las mismas que las de fidelizacion):
 *  - misma empresa y titular con los mismos telefonos que una que ya esta: se omite;
 *  - misma empresa y titular pero otros telefonos: se anade como "Empresa (importada)";
 *  - el resto: se anade tal cual.
 * Importar dos veces el mismo archivo no duplica nada.
 */
object BusinessMerge {

    private val importedSuffix = Regex(" \\(importada( \\d+)?\\)$")

    private fun key(text: String) = text.trim().lowercase()

    /** Empresa (sin el sufijo de importada) y titular, para saber si es la misma tarjeta. */
    private fun nameKey(company: String, holder: String) =
        key(company.trim().replace(importedSuffix, "")) + "|" + key(holder)

    private fun exactKey(company: String, holder: String) = key(company) + "|" + key(holder)

    /** Telefonos comparados solo por lo que se marca (da igual como se escriban). */
    private fun phoneSet(vararg phones: String): Set<String> =
        phones.map(PhoneNumbers::dialable).filter { it.isNotEmpty() }.toSet()

    fun plan(existing: List<BusinessCard>, incoming: List<IncomingBusiness>): BusinessMergePlan {
        val used = existing.mapTo(HashSet()) { exactKey(it.company, it.holder) }
        val known = existing.mapTo(HashSet()) { nameKey(it.company, it.holder) to phoneSet(it.phone1, it.phone2) }
        val toAdd = mutableListOf<PlannedBusiness>()
        var skipped = 0

        for (card in incoming) {
            val identity = nameKey(card.company, card.holder) to phoneSet(card.phone1, card.phone2)
            if (identity in known) {
                skipped++
                continue
            }
            var company = card.company.trim()
            var renamed = false
            if (exactKey(company, card.holder) in used) {
                company = uniqueCompany(company.replace(importedSuffix, ""), card.holder, used)
                renamed = true
            }
            used += exactKey(company, card.holder)
            known += identity
            toAdd += PlannedBusiness(card, company, renamed)
        }
        return BusinessMergePlan(toAdd, skipped)
    }

    private fun uniqueCompany(company: String, holder: String, used: Set<String>): String {
        // Sin nombre de empresa, el sufijo iria solo: se pone "Tarjeta" delante.
        val base = company.ifBlank { "Tarjeta" }
        var n = 1
        while (true) {
            val candidate = if (n == 1) "$base (importada)" else "$base (importada $n)"
            if (exactKey(candidate, holder) !in used) return candidate
            n++
        }
    }
}
