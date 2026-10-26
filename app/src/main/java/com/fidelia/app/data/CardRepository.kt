package com.fidelia.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

/**
 * Guarda las tarjetas en el almacenamiento privado de la app:
 * filesDir/cards.json (con copia cards.json.bak) y las fotos en filesDir/images.
 */
class CardRepository(private val context: Context) {

    private val imagesDir = File(context.filesDir, IMAGES_DIR).apply { mkdirs() }

    private val cardsFile = File(context.filesDir, "cards.json")
    private val businessFile = File(context.filesDir, "business_cards.json")

    // ---- Tarjetas de fidelizacion ----

    fun load(): List<LoyaltyCard> = loadList(cardsFile) { it.toCard() }

    fun save(cards: List<LoyaltyCard>): Boolean = saveList(cardsFile, cards) { it.toJson() }

    // ---- Tarjetas de visita ----

    fun loadBusiness(): List<BusinessCard> = loadList(businessFile) { it.toBusinessCard() }

    fun saveBusiness(cards: List<BusinessCard>): Boolean = saveList(businessFile, cards) { it.toJson() }

    private fun backupOf(file: File) = File(file.parentFile, file.name + ".bak")

    private fun <T> loadList(file: File, toItem: (JSONObject) -> T): List<T> =
        parseList(file, toItem) ?: parseList(backupOf(file), toItem) ?: emptyList()

    /** Escribe en un temporal y lo renombra: un corte a medias no deja el fichero roto. */
    private fun <T> saveList(file: File, items: List<T>, toJson: (T) -> JSONObject): Boolean = try {
        val array = JSONArray()
        items.forEach { array.put(toJson(it)) }
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(array.toString())
        // La copia solo se renueva si el fichero actual esta sano.
        if (parseList(file) { it } != null) file.copyTo(backupOf(file), overwrite = true)
        Files.move(
            tmp.toPath(),
            file.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE,
        )
        true
    } catch (e: Exception) {
        false
    }

    /** Copia la imagen elegida a la app (reducida, en JPEG). Devuelve el nombre del fichero. */
    fun importImage(uri: Uri): String? = try {
        val bitmap = decodeBitmap(context, uri, MAX_IMAGE_SIDE)
        // Sobre fondo blanco: un PNG con transparencia saldria negro en JPEG.
        val flat = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(flat).apply {
            drawColor(Color.WHITE)
            drawBitmap(bitmap, 0f, 0f, null)
        }
        val name = "img_${UUID.randomUUID()}.jpg"
        FileOutputStream(File(imagesDir, name)).use { flat.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        bitmap.recycle()
        flat.recycle()
        name
    } catch (e: Exception) {
        null
    }

    fun deleteImage(name: String) {
        File(imagesDir, name).delete()
    }

    /** Bytes de una imagen propia ya guardada (para exportarla). */
    fun readImageBytes(name: String): ByteArray? = try {
        File(imagesDir, name).takeIf { it.isFile }?.readBytes()
    } catch (e: Exception) {
        null
    }

    /** Guarda una imagen que viene de un archivo de copia, solo si de verdad es una imagen. */
    fun saveImageBytes(bytes: ByteArray): String? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val ok = bounds.outWidth in 1..MAX_IMPORTED_SIDE && bounds.outHeight in 1..MAX_IMPORTED_SIDE
        if (!ok) {
            null
        } else {
            val name = "img_${UUID.randomUUID()}.jpg"
            File(imagesDir, name).writeBytes(bytes)
            name
        }
    } catch (e: Exception) {
        null
    }

    /** Borra imagenes que ninguna tarjeta usa (restos de una carga cancelada). */
    fun cleanupOrphans(cards: List<LoyaltyCard>, business: List<BusinessCard>) {
        val used = cards.mapNotNull { it.imageFile }.toSet() + business.mapNotNull { it.imageFile }.toSet()
        imagesDir.listFiles()?.forEach { if (it.name !in used) it.delete() }
    }

    private fun <T> parseList(file: File, toItem: (JSONObject) -> T): List<T>? {
        if (!file.exists()) return null
        return try {
            val array = JSONArray(file.readText())
            List(array.length()) { toItem(array.getJSONObject(it)) }
        } catch (e: Exception) {
            null
        }
    }

    private fun BusinessCard.toJson() = JSONObject().apply {
        put("id", id)
        put("company", company)
        put("holder", holder)
        put("phone1", phone1)
        put("phone2", phone2)
        put("email", email)
        tradeId?.let { put("tradeId", it) }
        imageFile?.let { put("imageFile", it) }
        put("createdAt", createdAt)
    }

    private fun JSONObject.toBusinessCard() = BusinessCard(
        id = getString("id"),
        company = optString("company"),
        holder = optString("holder"),
        phone1 = optString("phone1"),
        phone2 = optString("phone2"),
        tradeId = if (has("tradeId")) getString("tradeId") else null,
        imageFile = if (has("imageFile")) getString("imageFile") else null,
        createdAt = optLong("createdAt"),
        email = optString("email"),
    )

    private fun LoyaltyCard.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        storeId?.let { put("storeId", it) }
        imageFile?.let { put("imageFile", it) }
        put("barcodeValue", barcodeValue)
        put("barcodeFormat", barcodeFormat)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toCard() = LoyaltyCard(
        id = getString("id"),
        name = getString("name"),
        storeId = if (has("storeId")) getString("storeId") else null,
        imageFile = if (has("imageFile")) getString("imageFile") else null,
        barcodeValue = getString("barcodeValue"),
        barcodeFormat = getString("barcodeFormat"),
        createdAt = optLong("createdAt"),
    )

    companion object {
        private const val IMAGES_DIR = "images"
        private const val MAX_IMAGE_SIDE = 1280
        private const val MAX_IMPORTED_SIDE = 8000

        fun imageFile(context: Context, name: String): File =
            File(File(context.filesDir, IMAGES_DIR), name)
    }
}
