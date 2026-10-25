package com.fidelia.app.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidelia.app.data.CardRepository
import com.fidelia.app.data.LoyaltyCard
import com.fidelia.app.data.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
