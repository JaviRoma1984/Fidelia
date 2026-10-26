package com.fidelia.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Campo de texto con todo centrado, en horizontal y en vertical, dentro de su recuadro:
 *  - vacio y sin tocar: la descripcion ([label]) en el centro;
 *  - al tocarlo o con texto: la descripcion pequena arriba y lo escrito debajo, ambos centrados.
 * [leading] y [trailing] son iconos opcionales pegados a los bordes (lupa, borrar...).
 */
@Composable
fun CenteredTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val labelOnTop = focused || value.isNotEmpty()
    val sidePadding = if (leading != null || trailing != null) 52.dp else 16.dp

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(colors.primary),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { innerTextField ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 68.dp)
                    .clip(shape)
                    .border(
                        width = if (focused) 2.dp else 1.dp,
                        color = if (focused) colors.primary else colors.outline,
                        shape = shape,
                    )
                    // Tocar en cualquier parte del recuadro (no solo en el texto) escribe en el.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        focusRequester.requestFocus()
                        keyboard?.show()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    Modifier.padding(horizontal = sidePadding, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (labelOnTop) {
                        Text(
                            label,
                            color = if (focused) colors.primary else colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                    Box(contentAlignment = Alignment.Center) {
                        if (!labelOnTop) {
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                        innerTextField()
                    }
                }
                if (leading != null) {
                    Box(Modifier.align(Alignment.CenterStart).padding(start = 8.dp)) { leading() }
                }
                if (trailing != null) {
                    Box(Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) { trailing() }
                }
            }
        },
    )
}
