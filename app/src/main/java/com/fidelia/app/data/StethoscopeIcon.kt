package com.fidelia.app.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Un estetoscopio dibujado a mano (la biblioteca de iconos de Material no trae ninguno):
 * las dos olivas arriba, los tubos en U, el tubo que baja y sube hasta la campana.
 * Es un dibujo de lineas, asi que se pinta del color que se le pida, como cualquier icono.
 */
val StethoscopeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Stethoscope",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        // Tubos en U con las olivas arriba, y el tubo que baja y sube hasta la campana.
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.9f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(6f, 3.5f)
            verticalLineTo(9f)
            arcTo(4f, 4f, 0f, false, false, 14f, 9f)
            verticalLineTo(3.5f)

            moveTo(10f, 13f)
            verticalLineTo(16.5f)
            arcTo(4f, 4f, 0f, false, false, 18f, 16.5f)
            verticalLineTo(14.5f)

            moveTo(4.5f, 3.5f)
            horizontalLineTo(7.5f)
            moveTo(12.5f, 3.5f)
            horizontalLineTo(15.5f)
        }
        // La campana (la parte que se apoya en el pecho).
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.9f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(18f, 10.5f)
            arcTo(2f, 2f, 0f, true, true, 18f, 14.5f)
            arcTo(2f, 2f, 0f, true, true, 18f, 10.5f)
            close()
        }
    }.build()
}
