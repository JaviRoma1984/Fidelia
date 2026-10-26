package com.fidelia.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Archivo de copia de las tarjetas (extension .fidelia): un zip con `cards.json`
 * y las imagenes propias en `images/`. Solo se leen esas dos cosas y con limites
 * de tamano, porque el archivo puede venir de otra persona.
 */
object CardTransfer {

    const val MIME = "application/octet-stream"

    private const val APP = "Fidelia"
    private const val FORMAT_VERSION = 1
    private const val JSON_ENTRY = "cards.json"
    private const val IMAGES_PREFIX = "images/"
    private const val MAX_CARDS = 2000
    private const val MAX_JSON_BYTES = 8_000_000
    private const val MAX_IMAGE_BYTES = 8_000_000
    private const val MAX_TOTAL_BYTES = 80_000_000L
    private const val MAX_NAME = 100
    private const val MAX_CODE = 500

    class ReadResult(val cards: List<IncomingCard>, val invalid: Int)

    fun suggestedFileName(now: Long = System.currentTimeMillis()): String =
        "Fidelia-tarjetas-" + SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(now)) + ".fidelia"

    /** Escribe las tarjetas en [out] y lo cierra. [readImage] da los bytes de una imagen propia. */
    fun write(cards: List<LoyaltyCard>, readImage: (String) -> ByteArray?, out: OutputStream) {
        val images = LinkedHashMap<String, ByteArray>()
        val array = JSONArray()
        cards.forEachIndexed { index, card ->
            val item = JSONObject()
                .put("name", card.name)
                .put("barcodeValue", card.barcodeValue)
                .put("barcodeFormat", card.barcodeFormat)
                .put("createdAt", card.createdAt)
            card.storeId?.let { item.put("storeId", it) }
            val bytes = card.imageFile?.let(readImage)
            if (bytes != null) {
                val entry = "${IMAGES_PREFIX}card_$index.jpg"
                images[entry] = bytes
                item.put("image", entry)
            }
            array.put(item)
        }
        val root = JSONObject()
            .put("app", APP)
            .put("version", FORMAT_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("cards", array)

        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(JSON_ENTRY))
            zip.write(root.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            for ((name, bytes) in images) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
    }

    /** Lee un archivo de copia. Lanza IOException si no es de Fidelia o esta danado. */
    fun read(input: InputStream): ReadResult {
        var json: ByteArray? = null
        val images = HashMap<String, ByteArray>()
        var total = 0L
        var entries = 0

        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (++entries > MAX_CARDS * 2 + 10) throw IOException("Demasiadas entradas")
                val name = entry.name
                val isJson = name == JSON_ENTRY
                if (!entry.isDirectory && (isJson || name.startsWith(IMAGES_PREFIX))) {
                    val bytes = readLimited(zip, if (isJson) MAX_JSON_BYTES else MAX_IMAGE_BYTES)
                    total += bytes.size
                    if (total > MAX_TOTAL_BYTES) throw IOException("Archivo demasiado grande")
                    if (isJson) json = bytes else images[name] = bytes
                }
                entry = zip.nextEntry
            }
        }

        val data = json ?: throw IOException("No es un archivo de Fidelia")
        val root = try {
            JSONObject(String(data, Charsets.UTF_8))
        } catch (e: Exception) {
            throw IOException("No es un archivo de Fidelia")
        }
        if (root.optString("app") != APP) throw IOException("No es un archivo de Fidelia")
        val array = root.optJSONArray("cards") ?: throw IOException("No es un archivo de Fidelia")

        val cards = ArrayList<IncomingCard>()
        var invalid = 0
        for (i in 0 until minOf(array.length(), MAX_CARDS)) {
            try {
                val item = array.getJSONObject(i)
                val name = item.getString("name").trim().take(MAX_NAME)
                val code = item.getString("barcodeValue").trim()
                if (name.isEmpty() || code.isEmpty() || code.length > MAX_CODE) {
                    invalid++
                    continue
                }
                cards += IncomingCard(
                    name = name,
                    storeId = if (item.has("storeId")) item.getString("storeId") else null,
                    image = if (item.has("image")) images[item.getString("image")] else null,
                    barcodeValue = code,
                    barcodeFormat = item.optString("barcodeFormat", "CODE_128").ifEmpty { "CODE_128" },
                    createdAt = item.optLong("createdAt"),
                )
            } catch (e: Exception) {
                invalid++
            }
        }
        if (array.length() > MAX_CARDS) invalid += array.length() - MAX_CARDS
        return ReadResult(cards, invalid)
    }

    private fun readLimited(input: InputStream, limit: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val n = input.read(buffer)
            if (n < 0) break
            if (out.size() + n > limit) throw IOException("Entrada demasiado grande")
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }
}
