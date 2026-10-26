package com.fidelia.app.data

/** Una tarjeta leida de un archivo de copia, todavia sin guardar en la app. */
class IncomingCard(
    val name: String,
    val storeId: String?,
    val image: ByteArray?,
    val barcodeValue: String,
    val barcodeFormat: String,
    val createdAt: Long,
)

class PlannedCard(val incoming: IncomingCard, val finalName: String, val renamed: Boolean)

class MergePlan(val toAdd: List<PlannedCard>, val skipped: Int)

/**
 * Reglas al importar:
 *  - mismo nombre y mismo codigo de barras que una tarjeta que ya esta: se omite;
 *  - mismo nombre pero otro codigo: se anade, con "(importada)" en el nombre para distinguirla;
 *  - el resto: se anade tal cual.
 * Importar dos veces el mismo archivo no duplica nada: las ya renombradas
 * ("Nombre (importada)") cuentan como el mismo nombre al comparar.
 */
object CardMerge {

    private val importedSuffix = Regex(" \\(importada( \\d+)?\\)$")

    private fun key(name: String) = name.trim().lowercase()

    private fun baseKey(name: String) = key(name.trim().replace(importedSuffix, ""))

    fun plan(existing: List<LoyaltyCard>, incoming: List<IncomingCard>): MergePlan {
        val usedNames = existing.mapTo(HashSet()) { key(it.name) }
        val known = existing.mapTo(HashSet()) { baseKey(it.name) to it.barcodeValue }
        val toAdd = mutableListOf<PlannedCard>()
        var skipped = 0

        for (card in incoming) {
            val identity = baseKey(card.name) to card.barcodeValue
            if (identity in known) {
                skipped++
                continue
            }
            var name = card.name.trim()
            var renamed = false
            if (key(name) in usedNames) {
                name = uniqueName(name.replace(importedSuffix, ""), usedNames)
                renamed = true
            }
            usedNames += key(name)
            known += identity
            toAdd += PlannedCard(card, name, renamed)
        }
        return MergePlan(toAdd, skipped)
    }

    private fun uniqueName(name: String, used: Set<String>): String {
        var n = 1
        while (true) {
            val candidate = if (n == 1) "$name (importada)" else "$name (importada $n)"
            if (key(candidate) !in used) return candidate
            n++
        }
    }
}
