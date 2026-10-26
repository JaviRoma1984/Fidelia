package com.fidelia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fidelia.app.data.CardRepository
import com.fidelia.app.data.TradeIcons

/**
 * La "cara" de una tarjeta de visita: la foto de la propia tarjeta si la tiene o, si no,
 * el icono del oficio elegido sobre su color (y sin ninguno, el de "Empresa").
 */
@Composable
fun BusinessArt(
    tradeId: String?,
    imageFile: String?,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    var base = modifier
        .aspectRatio(CARD_RATIO)
        .shadow(4.dp, shape)
        .clip(shape)
    if (description != null) base = base.semantics { contentDescription = description }

    if (imageFile != null) {
        val context = LocalContext.current
        val file = remember(imageFile) { CardRepository.imageFile(context, imageFile) }
        AsyncImage(
            model = file,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = base.background(Color(0xFFE5E7EB)),
        )
    } else {
        val trade = TradeIcons.find(tradeId) ?: TradeIcons.all.first()
        BoxWithConstraints(
            modifier = base.background(Brush.linearGradient(listOf(trade.color, trade.color.shade(0.78f)))),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    Color.White.copy(alpha = 0.10f),
                    radius = size.width * 0.35f,
                    center = Offset(size.width * 0.95f, size.height * 0.05f),
                )
                drawCircle(
                    Color.White.copy(alpha = 0.07f),
                    radius = size.width * 0.28f,
                    center = Offset(size.width * 0.05f, size.height),
                )
            }
            Icon(
                imageVector = trade.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(maxWidth * 0.36f),
            )
        }
    }
}
