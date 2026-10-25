package com.fidelia.app.barcode

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Un codigo leido: su texto y el formato (nombre de com.google.zxing.BarcodeFormat). */
data class DetectedBarcode(val value: String, val format: String)

object BarcodeDetector {

    val scannerOptions: BarcodeScannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    fun toDetected(barcode: Barcode): DetectedBarcode? {
        val value = barcode.rawValue
        if (value.isNullOrEmpty()) return null
        return DetectedBarcode(value, toZxingFormat(barcode.format))
    }

    private fun toZxingFormat(format: Int): String = when (format) {
        Barcode.FORMAT_EAN_13 -> "EAN_13"
        Barcode.FORMAT_EAN_8 -> "EAN_8"
        Barcode.FORMAT_UPC_A -> "UPC_A"
        Barcode.FORMAT_UPC_E -> "UPC_E"
        Barcode.FORMAT_CODE_128 -> "CODE_128"
        Barcode.FORMAT_CODE_39 -> "CODE_39"
        Barcode.FORMAT_CODE_93 -> "CODE_93"
        Barcode.FORMAT_CODABAR -> "CODABAR"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_QR_CODE -> "QR_CODE"
        Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
        Barcode.FORMAT_PDF417 -> "PDF_417"
        Barcode.FORMAT_AZTEC -> "AZTEC"
        else -> "CODE_128"
    }

    /** Busca un codigo en una imagen ya cargada (captura o foto de la galeria). */
    suspend fun detect(bitmap: Bitmap): DetectedBarcode? = suspendCancellableCoroutine { cont ->
        val scanner = BarcodeScanning.getClient(scannerOptions)
        scanner.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { list -> cont.resume(list.firstNotNullOfOrNull(::toDetected)) }
            .addOnFailureListener { cont.resume(null) }
            .addOnCompleteListener { scanner.close() }
    }
}
