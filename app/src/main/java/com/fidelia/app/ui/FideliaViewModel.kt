package com.fidelia.app.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.fidelia.app.barcode.BarcodeRenderer
import com.fidelia.app.data.CardMerge
import com.fidelia.app.data.CardRepository
import com.fidelia.app.data.CardTransfer
import com.fidelia.app.data.LoyaltyCard
import com.fidelia.app.data.Store
import com.fidelia.app.data.Stores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/** La tarjeta que se esta creando o editando en el formulario. */
data class Draft(
    val editingId: String? = null,
    val storeId: String? = null,
    val imageFile: String? = null,
    /** Imagen que ya tenia la tarjeta guardada (para no borrarla hasta guardar). */
    val originalImageFile: String? = null,
    val name: String = "",
    val barcodeValue: String = "",
    val barcodeFormat: String = "",
) {
    val canSave: Boolean get() = name.isNotBlank() && barcodeValue.isNotBlank()
}

class FideliaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CardRepository(application)

    var cards by mutableStateOf(repo.load())
        private set

    var stack by mutableStateOf<List<Screen>>(listOf(Screen.Home))
        private set

    /** Sentido de la ultima navegacion, para la animacion de pantallas. */
    var forward = true
        private set

    var draft by mutableStateOf(Draft())
        private set

    private var baseline by mutableStateOf(Draft())

    var message by mutableStateOf<String?>(null)
        private set

    val isDraftDirty: Boolean get() = draft != baseline

    init {
        repo.cleanupOrphans(cards)
    }

    // ---- Navegacion ----

    fun push(screen: Screen) {
        // Un doble toque rapido no debe abrir la misma pantalla dos veces.
        if (stack.last() == screen) return
        forward = true
        stack = stack + screen
    }

    fun back() {
        if (stack.size <= 1) return
        // Salir del formulario sin guardar: se descarta la imagen recien cargada.
        if (stack.last() == Screen.Form) discardNewDraftImage()
        forward = false
        stack = stack.dropLast(1)
    }

    fun openCardList() = push(Screen.CardList)

    fun openDetail(id: String) = push(Screen.Detail(id))

    fun showMessage(text: String) {
        message = text
    }

    fun consumeMessage() {
        message = null
    }

    // ---- Alta y edicion ----

    fun startNew() {
        draft = Draft()
        baseline = draft
        push(Screen.Stores)
    }

    /** [store] = null es "mi tienda no esta en la lista". */
    fun pickStore(store: Store?) {
        draft = Draft(storeId = store?.id, name = store?.name.orEmpty())
        baseline = draft
        push(Screen.Form)
    }

    fun startEdit(id: String) {
        val card = cards.find { it.id == id } ?: return
        draft = Draft(
            editingId = card.id,
            storeId = card.storeId,
            imageFile = card.imageFile,
            originalImageFile = card.imageFile,
            name = card.name,
            barcodeValue = card.barcodeValue,
            barcodeFormat = card.barcodeFormat,
        )
        baseline = draft
        push(Screen.Form)
    }

    fun setName(name: String) {
        draft = draft.copy(name = name)
    }

    fun setBarcode(value: String, format: String) {
        draft = draft.copy(barcodeValue = value, barcodeFormat = format)
    }

    fun importImage(uri: Uri) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { repo.importImage(uri) }
            if (file == null) message = "No he podido cargar esa imagen." else replaceDraftImage(file)
        }
    }

    fun clearImage() = replaceDraftImage(null)

    private fun replaceDraftImage(newFile: String?) {
        discardNewDraftImage()
        draft = draft.copy(imageFile = newFile)
    }

    /** Borra la imagen del borrador si no es la que la tarjeta ya tenia guardada. */
    private fun discardNewDraftImage() {
        val current = draft.imageFile ?: return
        if (current != draft.originalImageFile) repo.deleteImage(current)
    }

    fun save() {
        val d = draft
        if (!d.canSave) return
        val existing = d.editingId?.let { id -> cards.find { it.id == id } }
        val card = LoyaltyCard(
            id = existing?.id ?: UUID.randomUUID().toString(),
            name = d.name.trim(),
            storeId = d.storeId,
            imageFile = d.imageFile,
            barcodeValue = d.barcodeValue,
            barcodeFormat = d.barcodeFormat,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
        )
        val updated = if (existing != null) cards.map { if (it.id == card.id) card else it } else cards + card
        if (!repo.save(updated)) {
            message = "No se ha podido guardar la tarjeta. Inténtalo de nuevo."
            return
        }
        cards = updated
        d.originalImageFile?.takeIf { it != d.imageFile }?.let(repo::deleteImage)
        forward = false
        if (existing != null) {
            stack = stack.dropLast(1)
            message = "Cambios guardados"
        } else {
            stack = listOf(Screen.Home)
            message = "Tarjeta guardada"
        }
        // El borrador se queda como esta: la pantalla del formulario sigue visible
        // mientras se anima la salida y no debe verse vaciarse de golpe.
        baseline = draft
    }

    /** Guarda el orden que el usuario ha dado a las tarjetas (el de la lista es el orden guardado). */
    fun reorder(ids: List<String>) {
        val byId = cards.associateBy { it.id }
        val reordered = ids.mapNotNull(byId::get) + cards.filter { it.id !in ids }
        if (reordered == cards) return
        if (!repo.save(reordered)) {
            message = "No se ha podido guardar el nuevo orden."
            return
        }
        cards = reordered
    }

    // ---- Copia de las tarjetas: exportar e importar ----

    /** Resultado de una importacion, para ensenarselo al usuario. */
    class ImportReport(
        val added: Int,
        val skipped: Int,
        /** Nombres con los que se anadieron las tarjetas que repetian nombre con otro codigo. */
        val renamed: List<String>,
        val invalid: Int,
    )

    var importReport by mutableStateOf<ImportReport?>(null)
        private set

    fun dismissImportReport() {
        importReport = null
    }

    /** Escribe el archivo de copia en el sitio que haya elegido el usuario. */
    fun saveExportTo(uri: Uri) {
        val snapshot = cards
        val app = getApplication<Application>()
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val out = app.contentResolver.openOutputStream(uri, "w") ?: return@withContext false
                    CardTransfer.write(snapshot, repo::readImageBytes, out)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            message = if (ok) "Archivo guardado" else "No se ha podido guardar el archivo."
        }
    }

    /** Crea el archivo de copia en la cache y devuelve su Uri para compartirlo. */
    suspend fun createShareFile(): Uri? {
        val snapshot = cards
        val app = getApplication<Application>()
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(app.cacheDir, "exports").apply { mkdirs() }
                dir.listFiles()?.forEach { it.delete() }
                val file = File(dir, CardTransfer.suggestedFileName())
                FileOutputStream(file).use { CardTransfer.write(snapshot, repo::readImageBytes, it) }
                FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun importFrom(uri: Uri) {
        val current = cards
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { doImport(uri, current) }) {
                ImportResult.Unreadable ->
                    message = "Ese archivo no es una copia de Fidelia o está dañado."
                ImportResult.SaveFailed ->
                    message = "No se han podido guardar las tarjetas importadas. Inténtalo de nuevo."
                is ImportResult.Done -> {
                    cards = result.cards
                    importReport = result.report
                }
            }
        }
    }

    private sealed interface ImportResult {
        data object Unreadable : ImportResult
        data object SaveFailed : ImportResult
        class Done(val cards: List<LoyaltyCard>, val report: ImportReport) : ImportResult
    }

    private fun doImport(uri: Uri, current: List<LoyaltyCard>): ImportResult {
        val read: CardTransfer.ReadResult? = try {
            getApplication<Application>().contentResolver.openInputStream(uri)?.use(CardTransfer::read)
        } catch (e: Exception) {
            null
        }
        if (read == null) return ImportResult.Unreadable

        // Solo entran las tarjetas cuyo codigo se puede dibujar.
        val valid = read.cards.filter { BarcodeRenderer.render(it.barcodeValue, it.barcodeFormat) != null }
        val invalid = read.invalid + (read.cards.size - valid.size)

        val plan = CardMerge.plan(current, valid)
        val writtenImages = mutableListOf<String>()
        val now = System.currentTimeMillis()
        val added = plan.toAdd.map { planned ->
            val source = planned.incoming
            val image = source.image?.let(repo::saveImageBytes)?.also { writtenImages += it }
            LoyaltyCard(
                id = UUID.randomUUID().toString(),
                name = planned.finalName,
                storeId = source.storeId?.takeIf { Stores.find(it) != null },
                imageFile = image,
                barcodeValue = source.barcodeValue,
                barcodeFormat = BarcodeRenderer.render(source.barcodeValue, source.barcodeFormat)!!.format,
                createdAt = if (source.createdAt > 0) source.createdAt else now,
            )
        }
        val updated = current + added
        if (added.isNotEmpty() && !repo.save(updated)) {
            writtenImages.forEach(repo::deleteImage)
            return ImportResult.SaveFailed
        }
        val report = ImportReport(
            added = added.size,
            skipped = plan.skipped,
            renamed = plan.toAdd.filter { it.renamed }.map { it.finalName },
            invalid = invalid,
        )
        return ImportResult.Done(updated, report)
    }

    fun delete(id: String) {
        val card = cards.find { it.id == id } ?: return
        val updated = cards - card
        if (!repo.save(updated)) {
            message = "No se ha podido eliminar la tarjeta. Inténtalo de nuevo."
            return
        }
        cards = updated
        card.imageFile?.let(repo::deleteImage)
        message = "Tarjeta eliminada"
        // Si se borra desde la ficha, se vuelve a la pantalla anterior.
        if ((stack.last() as? Screen.Detail)?.id == id) back()
    }
}
