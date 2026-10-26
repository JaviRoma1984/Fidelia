package com.fidelia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fidelia.app.data.CardRepository
import com.fidelia.app.data.Store
import com.fidelia.app.data.Stores

/** Proporcion de una tarjeta de plastico (85,6 x 54 mm). */
const val CARD_RATIO = 1.586f

/**
 * La "cara" de una tarjeta: su imagen propia si la tiene, si no el logo de la
 * tienda elegida y, sin tienda, una imagen generica.
 */
@Composable
fun CardArt(
    storeId: String?,
    imageFile: String?,
    modifier: Modifier = Modifier,
    description: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val store = Stores.find(storeId)
    val shape = RoundedCornerShape(16.dp)
    var base = modifier
        .aspectRatio(CARD_RATIO)
        .shadow(4.dp, shape)
        .clip(shape)
    if (onClick != null) base = base.clickable(onClick = onClick)
    if (description != null) base = base.semantics { contentDescription = description }

    when {
        imageFile != null -> {
            val context = LocalContext.current
            val file = remember(imageFile) { CardRepository.imageFile(context, imageFile) }
            AsyncImage(
                model = file,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = base.background(Color(0xFFE5E7EB)),
            )
        }
        store != null -> StoreLogoBox(store, base)
        else -> GenericCardBox(base)
    }
}

@Composable
private fun StoreLogoBox(store: Store, modifier: Modifier) {
    BoxWithConstraints(
        modifier = modifier.background(
            Brush.linearGradient(listOf(store.color, store.color.shade(0.78f)))
        ),
        contentAlignment = Alignment.Center,
    ) {
        // Tamano relativo al ancho de la tarjeta, sin depender del zoom de fuente.
        val cardWidth = maxWidth
        val textSize = with(LocalDensity.current) { (cardWidth * 0.13f).toSp() }
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
        // Sombra suave: sin ella, un texto blanco sobre un fondo claro (amarillo) no se lee.
        val lightText = store.logoParts != null || store.textColor.luminance() > 0.5f
        val shadow = if (lightText) Shadow(Color.Black.copy(alpha = 0.28f), Offset(0f, 3f), 6f) else Shadow.None
        val parts = store.logoParts
        val label = if (parts != null) {
            buildAnnotatedString {
                parts.forEach { part ->
                    withStyle(SpanStyle(color = part.color)) { append(part.text) }
                }
            }
        } else {
            AnnotatedString(store.name)
        }
        val nameText: @Composable () -> Unit = {
            Text(
                text = label,
                color = store.textColor,
                fontSize = textSize,
                lineHeight = textSize * 1.1f,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = shadow),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = maxWidth * 0.08f),
            )
        }
        val icon = store.icon
        if (icon != null) {
            // Dibujo encima del nombre (p. ej. el libro de la biblioteca).
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = store.textColor,
                    modifier = Modifier.size(cardWidth * 0.26f),
                )
                nameText()
            }
        } else {
            nameText()
        }
    }
}

@Composable
private fun GenericCardBox(modifier: Modifier) {
    BoxWithConstraints(
        modifier = modifier.background(
            Brush.linearGradient(listOf(Color(0xFF6B7280), Color(0xFF374151)))
        ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.CreditCard,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(maxWidth * 0.28f),
        )
    }
}

private fun Color.shade(factor: Float) = Color(red * factor, green * factor, blue * factor, alpha)
