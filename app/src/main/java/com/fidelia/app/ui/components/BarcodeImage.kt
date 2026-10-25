package com.fidelia.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fidelia.app.barcode.BarcodeRenderer

/**
 * Pinta el codigo sin suavizar los bordes (si no, las barras salen borrosas).
 * En los codigos de barras lineales solo importa el ancho de las barras, asi que
 * se puede fijar la altura sin perder legibilidad.
 */
@Composable
fun BarcodeImage(
    rendered: BarcodeRenderer.Rendered,
    modifier: Modifier = Modifier,
    barHeight: Dp = 110.dp,
    squareSize: Dp = 190.dp,
) {
    val sizing = if (rendered.isSquare) {
        Modifier.size(squareSize)
    } else {
        Modifier
            .fillMaxWidth()
            .height(barHeight)
    }
    Image(
        bitmap = rendered.bitmap.asImageBitmap(),
        contentDescription = "Código de barras",
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.None,
        modifier = modifier.then(sizing),
    )
}
