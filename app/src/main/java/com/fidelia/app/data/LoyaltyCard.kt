package com.fidelia.app.data

/**
 * Una tarjeta de fidelizacion guardada.
 *
 * [storeId] es la tienda elegida del listado (null = "mi tienda no esta en la
 * lista"). [imageFile] es una imagen propia (foto o galeria) dentro de
 * filesDir/images; si es null se usa el logo de la tienda o, sin tienda, una
 * imagen generica. [barcodeFormat] es el nombre de com.google.zxing.BarcodeFormat.
 */
data class LoyaltyCard(
    val id: String,
    val name: String,
    val storeId: String?,
    val imageFile: String?,
    val barcodeValue: String,
    val barcodeFormat: String,
    val createdAt: Long,
)
