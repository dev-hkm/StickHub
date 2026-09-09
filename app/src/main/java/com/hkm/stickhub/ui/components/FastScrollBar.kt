package com.hkm.stickhub.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A narrow, thumb-draggable scrollbar for long sticker collections. It is deliberately
 * implemented outside the lazy container so normal scrolling and card gestures keep their
 * ownership; the 24dp track remains easy to grab without making the visual chrome heavy.
 */
@Composable
fun LazyGridFastScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    thumbColor: Color
) {
    val layoutInfo by remember(state) { derivedStateOf { state.layoutInfo } }
    val visible = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    val viewport = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).coerceAtLeast(1)
    val averageItemSize = visible.map { it.size.height }.average().toFloat().coerceAtLeast(1f)
    val contentSize = averageItemSize * totalItems.coerceAtLeast(1)
    val scrollOffset = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
    FastScrollBarTrack(
        modifier = modifier,
        totalItems = totalItems,
        viewportSize = viewport.toFloat(),
        contentSize = contentSize,
        scrollOffset = scrollOffset,
        thumbColor = thumbColor,
        onScrollTo = { fraction ->
            val target = (fraction * (totalItems - 1).coerceAtLeast(0)).roundToInt()
            state.scrollToItem(target)
        }
    )
}

@Composable
fun LazyListFastScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    thumbColor: Color
) {
    val layoutInfo by remember(state) { derivedStateOf { state.layoutInfo } }
    val visible = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    val viewport = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).coerceAtLeast(1)
    val averageItemSize = visible.map { it.size }.average().toFloat().coerceAtLeast(1f)
    val contentSize = averageItemSize * totalItems.coerceAtLeast(1)
    val scrollOffset = state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
    FastScrollBarTrack(
        modifier = modifier,
        totalItems = totalItems,
        viewportSize = viewport.toFloat(),
        contentSize = contentSize,
        scrollOffset = scrollOffset,
        thumbColor = thumbColor,
        onScrollTo = { fraction ->
            val target = (fraction * (totalItems - 1).coerceAtLeast(0)).roundToInt()
            state.scrollToItem(target)
        }
    )
}

@Composable
private fun FastScrollBarTrack(
    modifier: Modifier,
    totalItems: Int,
    viewportSize: Float,
    contentSize: Float,
    scrollOffset: Float,
    thumbColor: Color,
    onScrollTo: suspend (Float) -> Unit
) {
    val canScroll = totalItems > 1 && contentSize > viewportSize + 1f
    if (!canScroll) return

    val scope = rememberCoroutineScope()
    var trackHeight by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var activeScrollJob by remember { mutableStateOf<Job?>(null) }
    val thumbFraction = (viewportSize / contentSize).coerceIn(0.08f, 1f)
    val maxThumbTop = (trackHeight * (1f - thumbFraction)).coerceAtLeast(0f)
    val scrollFraction = (scrollOffset / (contentSize - viewportSize).coerceAtLeast(1f)).coerceIn(0f, 1f)
    val thumbTop = maxThumbTop * scrollFraction

    DisposableEffect(Unit) {
        onDispose { activeScrollJob?.cancel() }
    }

    Box(
        modifier = modifier
            .width(24.dp)
            .fillMaxHeight()
            .onSizeChanged { trackHeight = it.height.toFloat() }
            .semantics { contentDescription = "Fast scroll" }
            .pointerInput(totalItems, thumbFraction, trackHeight) {
                var dragTop = thumbTop
                detectDragGestures(
                    onDragStart = { position ->
                        dragging = true
                        val maxTop = (size.height * (1f - thumbFraction)).coerceAtLeast(1f)
                        dragTop = (position.y - size.height * 0.5f * thumbFraction).coerceIn(0f, maxTop)
                        activeScrollJob?.cancel()
                        activeScrollJob = scope.launch {
                            onScrollTo((dragTop / maxTop).coerceIn(0f, 1f))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val maxTop = (size.height * (1f - thumbFraction)).coerceAtLeast(1f)
                        dragTop = (dragTop + dragAmount.y).coerceIn(0f, maxTop)
                        activeScrollJob?.cancel()
                        activeScrollJob = scope.launch {
                            onScrollTo((dragTop / maxTop).coerceIn(0f, 1f))
                        }
                    },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxHeight()) {
            val inset = 3.dp.toPx()
            val thumbWidth = if (dragging) 6.dp.toPx() else 4.dp.toPx()
            val x = size.width - thumbWidth - inset
            val height = (size.height * thumbFraction).coerceAtLeast(32.dp.toPx())
            val top = (thumbTop).coerceIn(0f, (size.height - height).coerceAtLeast(0f))
            drawRoundRect(
                color = thumbColor.copy(alpha = if (dragging) 0.9f else 0.55f),
                topLeft = Offset(x, top),
                size = androidx.compose.ui.geometry.Size(thumbWidth, height),
                cornerRadius = CornerRadius(thumbWidth, thumbWidth)
            )
        }
    }
}
