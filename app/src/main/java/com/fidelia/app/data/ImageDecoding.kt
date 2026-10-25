package com.fidelia.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Decodifica una imagen (foto de la camara o de la galeria) respetando su
 * orientacion EXIF y reduciendola para que su lado mayor no pase de [maxSide].
 */
fun decodeBitmap(context: Context, uri: Uri, maxSide: Int): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val longest = max(info.size.width, info.size.height)
        if (longest > maxSide) {
            val scale = maxSide.toFloat() / longest
            decoder.setTargetSize(
                (info.size.width * scale).roundToInt().coerceAtLeast(1),
                (info.size.height * scale).roundToInt().coerceAtLeast(1),
            )
        }
    }
}
