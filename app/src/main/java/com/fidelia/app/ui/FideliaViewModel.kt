package com.fidelia.app.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidelia.app.barcode.BarcodeRenderer
import com.fidelia.app.data.BusinessCard
import com.fidelia.app.data.BusinessMerge
import com.fidelia.app.data.CardMerge
import com.fidelia.app.data.CardRepository
import com.fidelia.app.data.CardTransfer
import com.fidelia.app.data.Emails
import com.fidelia.app.data.LoyaltyCard
import com.fidelia.app.data.PhoneNumbers
import com.fidelia.app.data.Store
import com.fidelia.app.data.Stores
import com.fidelia.app.data.TradeIcons
import com.fidelia.app.ui.theme.Palettes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/** La tarjeta de fidelizacion que se esta creando o editando en el formulario. */
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

/** La tarjeta de visita que se esta creando o editando en el formulario. */
data class BusinessDraft(
    val editingId: String? = null,
    val tradeId: String? = null,
    val imageFile: String? = null,
    val originalImageFile: String? = null,
    val company: String = "",
    val holder: String = "",
    val phone1: String = "",
    val phone2: String = "",
    val email: String = "",
) {
    /** Basta con el nombre de la empresa o el del titular; telefonos y correo son opcionales. */
    val canSave: Boolean get() = company.isNotBlank() || holder.isNotBlank()
}

class FideliaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CardRepository(application)

    var cards by mutableStateOf(repo.load())
        private set

    var businessCards by mutableStateOf(repo.loadBusiness())
        private set

    var stack by mutableStateOf<List<Screen>>(listOf(Screen.Home))
        private set

    /** Sentido de la ultima navegacion, para la animacion de pantallas. */
    var forward = true
        private set

    var draft by mutableStateOf(Draft())
        private set

    private var baseline by mutableStateOf(Draft())

    var businessDraft by mutableStateOf(BusinessDraft())
        private set

    private var businessBaseline by mutableStateOf(BusinessDraft())

    var message by mutableStateOf<String?>(null)
        private set

    val isDraftDirty: Boolean get() = draft != baseline

    val isBusinessDraftDirty: Boolean get() = businessDraft != businessBaseline

    // ---- Configuracion ----

    private val settings = application.getSharedPreferences("fidelia_settings", Context.MODE_PRIVATE)

    /** Combinacion de colores elegida: un id de [Palettes.all] o [Palettes.DEFAULT_ID]. */
    var themeId by mutableStateOf(
        settings.getString("theme", null)?.takeIf(Palettes::isKnown) ?: Palettes.DEFAULT_ID
    )
        private set

    fun setTheme(id: String) {
        if (!Palettes.isKnown(id)) return
        themeId = id
        settings.edit().putString("theme", id).apply()
    }

    init {
        repo.cleanupOrphans(cards, businessCards)
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
        // Salir de un formulario sin guardar: se descarta la imagen recien cargada.
        when (stack.last()) {
            Screen.Form -> discardNewDraftImage()
            Screen.BusinessForm -> discardNewBusinessImage()
            else -> Unit
        }
        forward = false
        stack = stack.dropLast(1)
    }

    fun openLoyaltyMenu() = push(Screen.Loyalty)

    fun openBusinessMenu() = push(Screen.Business)

    fun openSettings() = push(Screen.Settings)

    fun openCardList() = push(Screen.CardList)

    fun openDetail(id: String) = push(Screen.Detail(id))

    fun openBusinessList() = push(Screen.BusinessList)

    fun openBusinessDetail(id: String) = push(Screen.BusinessDetail(id))

    /** Lleva a la lista de tarjetas de fidelizacion desde cualquier sitio (p. ej. tras importar). */
    fun showLoyaltyList() {
        forward = true
        stack = listOf(Screen.Home, Screen.Loyalty, Screen.CardList)
    }

    /** Lleva a la lista de tarjetas de visita desde cualquier sitio. */
    fun showBusinessList() {
        forward = true
        stack = listOf(Screen.Home, Screen.Business, Screen.BusinessList)
    }

    fun showMessage(text: String) {
        message = text
    }

    fun consumeMessage() {
        message = null
    }

    // ---- Tarjetas de fidelizacion: alta y edicion ----

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

    /** Abre la lista de logos de la app para elegir el de la tarjeta del formulario. */
    fun openLogoChoice() = push(Screen.StoreChoice)

    /**
     * Pone a la tarjeta del formulario el logo de una tienda de la lista (o ninguno, con null).
     * Sustituye a la foto o imagen propia que tuviera, y si aun no tenia nombre le pone el de la tienda.
     */
    fun chooseDraftStore(store: Store?) {
        discardNewDraftImage()
        draft = draft.copy(
            storeId = store?.id,
            imageFile = null,
            name = draft.name.ifBlank { store?.name.orEmpty() },
        )
        back()
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
            stack = listOf(Screen.Home, Screen.Loyalty)
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

    // ---- Tarjetas de visita: alta y edicion ----

    fun startNewBusiness() {
        businessDraft = BusinessDraft()
        businessBaseline = businessDraft
        push(Screen.BusinessForm)
    }

    fun startEditBusiness(id: String) {
        val card = businessCards.find { it.id == id } ?: return
        businessDraft = BusinessDraft(
            editingId = card.id,
            tradeId = card.tradeId,
            imageFile = card.imageFile,
            originalImageFile = card.imageFile,
            company = card.company,
            holder = card.holder,
            phone1 = card.phone1,
            phone2 = card.phone2,
            email = card.email,
        )
        businessBaseline = businessDraft
        push(Screen.BusinessForm)
    }

    fun setBusinessCompany(value: String) {
        businessDraft = businessDraft.copy(company = value)
    }

    fun setBusinessHolder(value: String) {
        businessDraft = businessDraft.copy(holder = value)
    }

    fun setBusinessPhone1(value: String) {
        businessDraft = businessDraft.copy(phone1 = PhoneNumbers.sanitizeInput(value))
    }

    fun setBusinessPhone2(value: String) {
        businessDraft = businessDraft.copy(phone2 = PhoneNumbers.sanitizeInput(value))
    }

    fun setBusinessEmail(value: String) {
        businessDraft = businessDraft.copy(email = Emails.sanitizeInput(value))
    }

    /** Elegir un icono de oficio sustituye a la foto (es una cosa o la otra). */
    fun chooseTrade(id: String) {
        discardNewBusinessImage()
        businessDraft = businessDraft.copy(tradeId = id, imageFile = null)
    }

    /** Hacer una foto a la tarjeta sustituye al icono. */
    fun importBusinessImage(uri: Uri) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { repo.importImage(uri) }
            if (file == null) {
                message = "No he podido cargar esa imagen."
            } else {
                discardNewBusinessImage()
                businessDraft = businessDraft.copy(imageFile = file, tradeId = null)
            }
        }
    }

    fun clearBusinessImage() {
        discardNewBusinessImage()
        businessDraft = businessDraft.copy(imageFile = null)
    }

    private fun discardNewBusinessImage() {
        val current = businessDraft.imageFile ?: return
        if (current != businessDraft.originalImageFile) repo.deleteImage(current)
    }

    fun saveBusiness() {
        val d = businessDraft
        if (!d.canSave) return
        val existing = d.editingId?.let { id -> businessCards.find { it.id == id } }
        val card = BusinessCard(
            id = existing?.id ?: UUID.randomUUID().toString(),
            company = d.company.trim(),
            holder = d.holder.trim(),
            phone1 = PhoneNumbers.display(d.phone1),
            phone2 = PhoneNumbers.display(d.phone2),
            tradeId = d.tradeId,
            imageFile = d.imageFile,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            email = d.email.trim(),
        )
        val updated = if (existing != null) {
            businessCards.map { if (it.id == card.id) card else it }
        } else {
            businessCards + card
        }
        if (!repo.saveBusiness(updated)) {
            message = "No se ha podido guardar la tarjeta. Inténtalo de nuevo."
            return
        }
        businessCards = updated
        d.originalImageFile?.takeIf { it != d.imageFile }?.let(repo::deleteImage)
        forward = false
        if (existing != null) {
            stack = stack.dropLast(1)
            message = "Cambios guardados"
        } else {
            stack = listOf(Screen.Home, Screen.Business)
            message = "Tarjeta de visita guardada"
        }
        businessBaseline = businessDraft
    }

    fun deleteBusiness(id: String) {
        val card = businessCards.find { it.id == id } ?: return
        val updated = businessCards - card
        if (!repo.saveBusiness(updated)) {
            message = "No se ha podido eliminar la tarjeta. Inténtalo de nuevo."
            return
        }
        businessCards = updated
        card.imageFile?.let(repo::deleteImage)
        message = "Tarjeta eliminada"
        if ((stack.last() as? Screen.BusinessDetail)?.id == id) back()
    }

    fun reorderBusiness(ids: List<String>) {
        val byId = businessCards.associateBy { it.id }
        val reordered = ids.mapNotNull(byId::get) + businessCards.filter { it.id !in ids }
        if (reordered == businessCards) return
        if (!repo.saveBusiness(reordered)) {
            message = "No se ha podido guardar el nuevo orden."
            return
        }
        businessCards = reordered
    }

    // ---- Copia de las tarjetas: exportar e importar ----

    /** Resultado de una importacion, para ensenarselo al usuario. */
    class ImportReport(
        val added: Int,
        val skipped: Int,
        /** Nombres con los que se anadieron las tarjetas que repetian nombre con otro codigo. */
        val renamed: List<String>,
        val invalid: Int,
        val addedBusiness: Int,
        val skippedBusiness: Int,
        val renamedBusiness: List<String>,
        val invalidBusiness: Int,
    ) {
        val hadLoyalty: Boolean get() = added + skipped + invalid > 0
        val hadBusiness: Boolean get() = addedBusiness + skippedBusiness + invalidBusiness > 0
        val totalAdded: Int get() = added + addedBusiness
        val totalSkipped: Int get() = skipped + skippedBusiness
    }

    var importReport by mutableStateOf<ImportReport?>(null)
        private set

    fun dismissImportReport() {
        importReport = null
    }

    private fun writeExport(out: java.io.OutputStream, loyalty: List<LoyaltyCard>, business: List<BusinessCard>) =
        CardTransfer.write(loyalty, repo::readImageBytes, out, business)

    /** Escribe el archivo de copia en el sitio que haya elegido el usuario. */
    fun saveExportTo(uri: Uri) {
        val loyalty = cards
        val business = businessCards
        val app = getApplication<Application>()
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val out = app.contentResolver.openOutputStream(uri, "w") ?: return@withContext false
                    writeExport(out, loyalty, business)
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
        val loyalty = cards
        val business = businessCards
        val app = getApplication<Application>()
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(app.cacheDir, "exports").apply { mkdirs() }
                dir.listFiles()?.forEach { it.delete() }
                val file = File(dir, CardTransfer.suggestedFileName())
                FileOutputStream(file).use { writeExport(it, loyalty, business) }
                FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun importFrom(uri: Uri) {
        val currentLoyalty = cards
        val currentBusiness = businessCards
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { doImport(uri, currentLoyalty, currentBusiness) }) {
                ImportResult.Unreadable ->
                    message = "Ese archivo no es una copia de Fidelia o está dañado."
                ImportResult.SaveFailed ->
                    message = "No se han podido guardar las tarjetas importadas. Inténtalo de nuevo."
                is ImportResult.Done -> {
                    cards = result.cards
                    businessCards = result.businessCards
                    importReport = result.report
                }
            }
        }
    }

    private sealed interface ImportResult {
        data object Unreadable : ImportResult
        data object SaveFailed : ImportResult
        class Done(
            val cards: List<LoyaltyCard>,
            val businessCards: List<BusinessCard>,
            val report: ImportReport,
        ) : ImportResult
    }

    private fun doImport(uri: Uri, current: List<LoyaltyCard>, currentBusiness: List<BusinessCard>): ImportResult {
        val read: CardTransfer.ReadResult? = try {
            getApplication<Application>().contentResolver.openInputStream(uri)?.use(CardTransfer::read)
        } catch (e: Exception) {
            null
        }
        if (read == null) return ImportResult.Unreadable

        val writtenImages = mutableListOf<String>()
        val now = System.currentTimeMillis()

        // Tarjetas de fidelizacion: solo entran las cuyo codigo se puede dibujar.
        val valid = read.cards.filter { BarcodeRenderer.render(it.barcodeValue, it.barcodeFormat) != null }
        val invalid = read.invalid + (read.cards.size - valid.size)
        val plan = CardMerge.plan(current, valid)
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

        // Tarjetas de visita.
        val businessPlan = BusinessMerge.plan(currentBusiness, read.businessCards)
        val addedBusiness = businessPlan.toAdd.map { planned ->
            val source = planned.incoming
            val image = source.image?.let(repo::saveImageBytes)?.also { writtenImages += it }
            BusinessCard(
                id = UUID.randomUUID().toString(),
                company = planned.finalCompany,
                holder = source.holder,
                phone1 = source.phone1,
                phone2 = source.phone2,
                tradeId = source.tradeId?.takeIf { TradeIcons.find(it) != null },
                imageFile = image,
                createdAt = if (source.createdAt > 0) source.createdAt else now,
                email = source.email,
            )
        }

        val updated = current + added
        val updatedBusiness = currentBusiness + addedBusiness
        if (added.isNotEmpty() && !repo.save(updated)) {
            writtenImages.forEach(repo::deleteImage)
            return ImportResult.SaveFailed
        }
        if (addedBusiness.isNotEmpty() && !repo.saveBusiness(updatedBusiness)) {
            // Se deshace lo ya guardado para no dejar la importacion a medias.
            if (added.isNotEmpty()) repo.save(current)
            writtenImages.forEach(repo::deleteImage)
            return ImportResult.SaveFailed
        }

        val report = ImportReport(
            added = added.size,
            skipped = plan.skipped,
            renamed = plan.toAdd.filter { it.renamed }.map { it.finalName },
            invalid = invalid,
            addedBusiness = addedBusiness.size,
            skippedBusiness = businessPlan.skipped,
            renamedBusiness = businessPlan.toAdd.filter { it.renamed }.map { it.finalCompany },
            invalidBusiness = read.invalidBusiness,
        )
        return ImportResult.Done(updated, updatedBusiness, report)
    }
}
