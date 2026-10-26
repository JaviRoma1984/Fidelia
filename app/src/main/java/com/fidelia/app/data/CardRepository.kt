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

    private val jsonFile = File(context.filesDir, "cards.json")
    private val backupFile = File(context.filesDir, "cards.json.bak")
    private val imagesDir = File(context.filesDir, IMAGES_DIR).apply { mkdirs() }

    fun load(): List<LoyaltyCard> = parse(jsonFile) ?: parse(backupFile) ?: emptyList()

    /** Escribe en un temporal y lo renombra: un corte a medias no deja el fichero roto. */
    fun save(cards: List<LoyaltyCard>): Boolean = try {
        val array = JSONArray()
        cards.forEach { array.put(it.toJson()) }
        val tmp = File(context.filesDir, "cards.json.tmp")
        tmp.writeText(array.toString())
        // La copia solo se renueva si el fichero actual esta sano.
        if (parse(jsonFile) != null) jsonFile.copyTo(backupFile, overwrite = true)
        Files.move(
            tmp.toPath(),
            jsonFile.toPath(),
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
    fun cleanupOrphans(cards: List<LoyaltyCard>) {
        val used = cards.mapNotNull { it.imageFile }.toSet()
        imagesDir.listFiles()?.forEach { if (it.name !in used) it.delete() }
    }

    private fun parse(file: File): List<LoyaltyCard>? {
        if (!file.exists()) return null
        return try {
            val array = JSONArray(file.readText())
            List(array.length()) { array.getJSONObject(it).toCard() }
        } catch (e: Exception) {
            null
        }
    }

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
