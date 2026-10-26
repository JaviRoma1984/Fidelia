package com.fidelia.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/**
 * Reordenar una rejilla: se mantiene pulsada una tarjeta, se arrastra y al soltar
 * se queda donde esta. Las demas se apartan mientras se arrastra (el orden se
 * va cambiando con [onMove]); quien usa esto guarda el orden final al soltar.
 */
@Stable
class GridReorderState internal constructor(
    private val grid: LazyGridState,
    private val onMove: (from: Int, to: Int) -> Unit,
) {
    /** Clave de la tarjeta que se esta arrastrando, o null. */
    var draggingKey: Any? by mutableStateOf(null)
        private set

    private var pointer by mutableStateOf(Offset.Zero)
    private var grab = Offset.Zero
    private var waitingForIndex: Int? = null

    private fun LazyGridItemInfo.center() = Offset(offset.x + size.width / 2f, offset.y + size.height / 2f)

    private fun LazyGridItemInfo.hit(p: Offset) =
        p.x >= offset.x && p.x < offset.x + size.width && p.y >= offset.y && p.y < offset.y + size.height

    private fun infoOf(key: Any): LazyGridItemInfo? =
        grid.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }

    internal fun keyAt(position: Offset): Any? =
        grid.layoutInfo.visibleItemsInfo.firstOrNull { it.hit(position) }?.key

    /** Cuanto hay que mover la tarjeta arrastrada para que siga bajo el dedo. */
    fun translationOf(key: Any): Offset {
        if (key != draggingKey) return Offset.Zero
        val info = infoOf(key) ?: return Offset.Zero
        return pointer - grab - info.center()
    }

    internal fun start(key: Any, position: Offset) {
        val info = infoOf(key) ?: return
        pointer = position
        grab = position - info.center()
        waitingForIndex = null
        draggingKey = key
    }

    internal fun dragTo(position: Offset) {
        pointer = position
        moveIfNeeded()
    }

    internal fun end() {
        draggingKey = null
        waitingForIndex = null
    }

    private fun moveIfNeeded() {
        val key = draggingKey ?: return
        val items = grid.layoutInfo.visibleItemsInfo
        val current = items.firstOrNull { it.key == key } ?: return
        // Tras mover, se espera a que la rejilla se recoloque antes de volver a mover.
        waitingForIndex?.let { if (current.index == it) waitingForIndex = null else return }
        val target = items.firstOrNull { it.key != key && it.hit(pointer) } ?: return
        if (target.index == current.index) return
        waitingForIndex = target.index
        onMove(current.index, target.index)
    }

    /** Si el dedo llega al borde de arriba o de abajo, la lista se desplaza sola. */
    internal suspend fun autoScroll() {
        val edge = 140f
        while (draggingKey != null) {
            val height = grid.layoutInfo.viewportSize.height
            val y = pointer.y
            val delta = when {
                y < edge -> -(edge - y) / edge * 30f
                y > height - edge -> (y - (height - edge)) / edge * 30f
                else -> 0f
            }
            if (delta != 0f) {
                grid.scrollBy(delta)
                moveIfNeeded()
            }
            delay(16)
        }
    }
}

@Composable
fun rememberGridReorderState(grid: LazyGridState, onMove: (from: Int, to: Int) -> Unit): GridReorderState {
    val latestOnMove by rememberUpdatedState(onMove)
    val state = remember(grid) { GridReorderState(grid) { from, to -> latestOnMove(from, to) } }
    val dragging = state.draggingKey != null
    LaunchedEffect(dragging) {
        if (dragging) state.autoScroll()
    }
    return state
}

/**
 * Un toque corto sobre una tarjeta llama a [onTap]; mantenerla pulsada la
 * selecciona y se puede arrastrar; al soltar se llama a [onDrop].
 */
@Composable
fun Modifier.gridReorderGestures(
    state: GridReorderState,
    haptic: HapticFeedback,
    onTap: (key: Any) -> Unit,
    onDrop: () -> Unit,
): Modifier {
    val latestOnTap by rememberUpdatedState(onTap)
    val latestOnDrop by rememberUpdatedState(onDrop)
    return pointerInput(state) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = true)
            val key = state.keyAt(down.position) ?: return@awaitEachGesture

            val longPress = awaitLongPressOrCancellation(down.id)
            if (longPress == null) {
                // No fue una pulsacion larga: si el dedo se levanto sin moverse, es un toque.
                val last = currentEvent.changes.firstOrNull { it.id == down.id }
                if (last != null && last.changedToUp()) latestOnTap(key)
                return@awaitEachGesture
            }

            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            state.start(key, longPress.position)
            while (true) {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == longPress.id } ?: break
                if (!change.pressed) break
                state.dragTo(change.position)
                change.consume()
            }
            state.end()
            latestOnDrop()
        }
    }
}
