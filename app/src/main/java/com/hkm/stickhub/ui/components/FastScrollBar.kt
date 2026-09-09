package com.hkm.stickhub.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween

/**
 * A narrow, thumb-draggable scrollbar for long sticker collections. It is deliberately
 * implemented outside the lazy container so normal scrolling and card gestures keep their
 * ownership; the 24dp track remains easy to grab without making the visual chrome heavy.
 * The thumb is transparent at rest and briefly fades in while scrolling or dragging.
 */
@Composable
fun LazyGridFastScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    thumbColor: Color,
    columns: Int = 3
) {
    val layoutInfo by remember(state) { derivedStateOf { state.layoutInfo } }
    val visible = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    val viewport = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).coerceAtLeast(1)
    var headerHeight by remember(state) { mutableStateOf(0) }
    val measuredHeader = visible.firstOrNull { it.index == 0 }?.size?.height
    androidx.compose.runtime.SideEffect { measuredHeader?.let { headerHeight = it } }
    val geometry = LibraryScrollGeometry(totalItems, columns, measuredHeader ?: headerHeight,
        visible.firstOrNull { it.index > 0 }?.size?.height ?: 1,
        layoutInfo.mainAxisItemSpacing, viewport,
        layoutInfo.beforeContentPadding + layoutInfo.afterContentPadding)
    val contentSize = geometry.contentSize
    val scrollOffset = if (!state.canScrollForward) geometry.maxOffset else
        geometry.offset(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset)
    FastScrollBarTrack(
        modifier = modifier,
        totalItems = totalItems,
        viewportSize = viewport.toFloat(),
        contentSize = contentSize,
        scrollOffset = scrollOffset,
        isScrolling = state.isScrollInProgress,
        thumbColor = thumbColor,
        onScrollTo = { fraction ->
            val (index, offset) = geometry.target(fraction)
            state.scrollToItem(index, offset)
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
    var headerHeight by remember(state) { mutableStateOf(0) }
    val measuredHeader = visible.firstOrNull { it.index == 0 }?.size
    androidx.compose.runtime.SideEffect { measuredHeader?.let { headerHeight = it } }
    val geometry = LibraryScrollGeometry(totalItems, 1, measuredHeader ?: headerHeight,
        visible.firstOrNull { it.index > 0 }?.size ?: 1,
        layoutInfo.mainAxisItemSpacing, viewport,
        layoutInfo.beforeContentPadding + layoutInfo.afterContentPadding)
    val contentSize = geometry.contentSize
    val scrollOffset = if (!state.canScrollForward) geometry.maxOffset else
        geometry.offset(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset)
    FastScrollBarTrack(
        modifier = modifier,
        totalItems = totalItems,
        viewportSize = viewport.toFloat(),
        contentSize = contentSize,
        scrollOffset = scrollOffset,
        isScrolling = state.isScrollInProgress,
        thumbColor = thumbColor,
        onScrollTo = { fraction ->
            val (index, offset) = geometry.target(fraction)
            state.scrollToItem(index, offset)
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
    isScrolling: Boolean,
    thumbColor: Color,
    onScrollTo: suspend (Float) -> Unit
) {
    val canScroll = totalItems > 1 && contentSize > viewportSize + 1f
    if (!canScroll) return

    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val thumbFraction = (viewportSize / contentSize).coerceIn(0.08f, 1f)
    val latestThumbFraction by rememberUpdatedState(thumbFraction)
    val latestScroll by rememberUpdatedState(onScrollTo)
    val scrollFraction = (scrollOffset / (contentSize - viewportSize).coerceAtLeast(1f)).coerceIn(0f, 1f)
    val scrollRequests = remember { Channel<Float>(Channel.CONFLATED) }
    val thumbAlpha = remember { Animatable(0f) }

    LaunchedEffect(scrollRequests) {
        for (fraction in scrollRequests) latestScroll(fraction)
    }
    LaunchedEffect(isScrolling, dragging) {
        if (isScrolling || dragging) {
            thumbAlpha.animateTo(.62f, tween(120))
        } else {
            delay(650)
            thumbAlpha.animateTo(0f, tween(260))
        }
    }

    DisposableEffect(Unit) {
        onDispose { scrollRequests.close() }
    }

    Box(
        modifier = modifier
            .width(24.dp)
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(top = 12.dp, bottom = 88.dp)
            .clipToBounds()
            .semantics { contentDescription = "Fast scroll" }
            .pointerInput(Unit) {
                var dragTop = 0f
                detectDragGestures(
                    onDragStart = { position ->
                        dragging = true
                        val height = (size.height * latestThumbFraction).coerceAtLeast(32.dp.toPx()).coerceAtMost(size.height.toFloat())
                        val maxTop = (size.height - height).coerceAtLeast(1f)
                        dragTop = (position.y - height / 2).coerceIn(0f, maxTop)
                        dragFraction = dragTop / maxTop
                        scrollRequests.trySend((dragTop / maxTop).coerceIn(0f, 1f))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val height = (size.height * latestThumbFraction).coerceAtLeast(32.dp.toPx()).coerceAtMost(size.height.toFloat())
                        val maxTop = (size.height - height).coerceAtLeast(1f)
                        dragTop = (dragTop + dragAmount.y).coerceIn(0f, maxTop)
                        dragFraction = dragTop / maxTop
                        scrollRequests.trySend((dragTop / maxTop).coerceIn(0f, 1f))
                    },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = 3.dp.toPx()
            val thumbWidth = if (dragging) 6.dp.toPx() else 4.dp.toPx()
            val x = size.width - thumbWidth - inset
            val height = (size.height * thumbFraction).coerceAtLeast(32.dp.toPx())
            val top = (size.height - height).coerceAtLeast(0f) * (if (dragging) dragFraction else scrollFraction)
            drawRoundRect(
                color = thumbColor.copy(alpha = (thumbAlpha.value * if (dragging) 1.35f else 1f).coerceIn(0f, 1f)),
                topLeft = Offset(x, top),
                size = androidx.compose.ui.geometry.Size(thumbWidth, height),
                cornerRadius = CornerRadius(thumbWidth, thumbWidth)
            )
        }
    }
}
