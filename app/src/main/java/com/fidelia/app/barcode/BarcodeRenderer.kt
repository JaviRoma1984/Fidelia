package com.fidelia.app.barcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

/** Dibuja un codigo de barras / QR como imagen nitida (negro sobre blanco). */
object BarcodeRenderer {

    class Rendered(val bitmap: Bitmap, val format: String) {
        val isSquare: Boolean get() = format in SQUARE_FORMATS
    }

    private val SQUARE_FORMATS = setOf("QR_CODE", "DATA_MATRIX", "AZTEC")

    /**
     * Intenta dibujar [value] en [format]; si ese formato no admite el valor
     * (p. ej. un EAN-13 con digito de control erroneo) prueba Code 128 y por
     * ultimo QR. Devuelve el formato realmente usado, o null si no se pudo.
     */
    fun render(value: String, format: String): Rendered? {
        if (value.isEmpty()) return null
        for (name in listOf(format, "CODE_128", "QR_CODE").distinct()) {
            try {
                return Rendered(encode(value, name), name)
            } catch (e: Exception) {
                // siguiente formato
            }
        }
        return null
    }

    /** Formato mas probable para un numero escrito a mano. */
    fun guessFormat(value: String): String {
        val digits = value.isNotEmpty() && value.all { it in '0'..'9' }
        return when {
            digits && value.length == 13 && gtinChecksumOk(value) -> "EAN_13"
            digits && value.length == 12 && gtinChecksumOk(value) -> "UPC_A"
            digits && value.length == 8 && gtinChecksumOk(value) -> "EAN_8"
            value.all { it.code in 32..126 } -> "CODE_128"
            else -> "QR_CODE"
        }
    }

    private fun encode(value: String, name: String): Bitmap {
        val square = name in SQUARE_FORMATS
        val width = if (square) 900 else 1200
        val height = if (square) 900 else 480
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        val matrix = MultiFormatWriter().encode(value, BarcodeFormat.valueOf(name), width, height, hints)
        val w = matrix.width
        val h = matrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                pixels[row + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }

    /** Digito de control GTIN (EAN-8, UPC-A, EAN-13): pesos 3,1,3,1... desde la derecha. */
    private fun gtinChecksumOk(v: String): Boolean {
        var sum = 0
        for (i in v.length - 2 downTo 0) {
            val fromRight = v.length - 2 - i
            sum += (v[i] - '0') * if (fromRight % 2 == 0) 3 else 1
        }
        return (10 - sum % 10) % 10 == v.last() - '0'
    }
}
