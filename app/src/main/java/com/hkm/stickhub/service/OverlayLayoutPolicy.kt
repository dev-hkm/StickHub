package com.hkm.stickhub.service

import kotlin.math.max

/**
 * Pure math and layout rules for the quick-stickers floating overlay and sticker grid.
 *
 * Implements strict constraints:
 * - 3 columns grid that never clips on narrow or wide panels.
 * - Dynamic minimum height depending on enabled chrome (grid-only allows compact 2-row panel).
 * - Zero empty header or footer rows when chrome is disabled.
 * - Complete boundary clamping against screen dimensions.
 */
object OverlayLayoutPolicy {
    const val GRID_COLUMNS = 3

    // Default sizing tokens in DP
    const val DEFAULT_PANEL_WIDTH_DP = 320f
    const val DEFAULT_PANEL_HEIGHT_DP = 360f

    const val MIN_CELL_SIZE_DP = 56f
    const val CELL_MARGIN_DP = 3f

    const val CHROME_TITLE_HEIGHT_DP = 36f
    const val CHROME_SEARCH_HEIGHT_DP = 46f
    const val CHROME_CATEGORIES_HEIGHT_DP = 38f

    // Keep the close control compact; its touch target sits outside the panel
    // and must not consume a large corner of the popup.
    const val CLOSE_CONTROL_SIZE_DP = 30f
    const val CLOSE_DOCK_OFFSET_DP = 8f

    data class PanelBounds(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    /** Position of the compact close control in its own overlay window. */
    data class CloseOverlayPosition(val x: Int, val y: Int)

    /**
     * Unified layout snapshot capturing panel bounds, outside close button position,
     * bounds limits, and responsive grid column count.
     */
    data class PopupGeometry(
        val panelBounds: PanelBounds,
        val closePosition: CloseOverlayPosition,
        val minWidth: Int,
        val minHeight: Int,
        val maxWidth: Int,
        val maxHeight: Int,
        val columns: Int
    )

    /**
     * Dock the close control on the outside corner of the popup. The center
     * sits on the popup's top-right corner, leaving half of the control above
     * and to the right of the surface instead of consuming content space.
     */
    fun closeOverlayPosition(
        panelX: Int,
        panelY: Int,
        panelWidth: Int,
        closeSize: Int,
        screenWidth: Int,
        screenHeight: Int,
        offsetPx: Int = CLOSE_DOCK_OFFSET_DP.toInt()
    ): CloseOverlayPosition {
        val maxX = max(0, screenWidth - closeSize)
        val maxY = max(0, screenHeight - closeSize)
        return CloseOverlayPosition(
            x = (panelX + panelWidth + offsetPx - closeSize / 2).coerceIn(0, maxX),
            y = (panelY - offsetPx - closeSize / 2).coerceIn(0, maxY)
        )
    }

    /**
     * Dynamically determines column count based on available width.
     * Guarantees at least 3 columns and scales up smoothly for wider views/tablets.
     */
    fun calculateColumns(panelWidthPx: Int, density: Float, targetCellSizeDp: Float = 84f): Int {
        val widthDp = panelWidthPx / max(0.1f, density)
        val cols = (widthDp / targetCellSizeDp).toInt()
        return cols.coerceIn(3, 8)
    }

    /**
     * Calculates the exact pixel width/height for a square grid cell.
     * Ensures: columns * cellSize + (columns * 2 * cellMargin) + (2 * horizontalPadding) <= panelWidthPx.
     */
    fun gridCellSize(
        panelWidthPx: Int,
        horizontalContentPaddingPx: Int,
        cellMarginPx: Int,
        columns: Int = GRID_COLUMNS
    ): Int {
        val totalPadding = horizontalContentPaddingPx * 2
        val totalMargins = columns * cellMarginPx * 2
        val available = panelWidthPx - totalPadding - totalMargins
        return max(1, available / max(1, columns))
    }

    /**
     * Minimum width required so that 3 columns of size [minCellDp] + margins + padding fit without clipping.
     */
    fun minPanelWidthPx(
        density: Float,
        minCellDp: Float = MIN_CELL_SIZE_DP,
        horizontalPaddingDp: Float = 6f,
        cellMarginDp: Float = CELL_MARGIN_DP
    ): Int {
        val minWidthDp = (GRID_COLUMNS * minCellDp) +
                (GRID_COLUMNS * cellMarginDp * 2) +
                (horizontalPaddingDp * 2)
        return max(1, (minWidthDp * density).toInt())
    }

    /**
     * Computes the complete immutable geometry snapshot for the quick stickers popup.
     */
    fun computePopupGeometry(
        requestedX: Int,
        requestedY: Int,
        requestedWidth: Int,
        requestedHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
        density: Float,
        showTitle: Boolean,
        showSearch: Boolean,
        showCategories: Boolean,
        closeButtonSizePx: Int,
        closeOffsetPx: Int = 4
    ): PopupGeometry {
        val minW = minPanelWidthPx(density)
        val minH = minPanelHeightPx(density, showTitle, showSearch, showCategories)
        val maxW = (screenWidth * 0.94f).toInt()
        val maxH = (screenHeight * 0.85f).toInt()
        val panelBounds = clampPanelBounds(
            x = requestedX,
            y = requestedY,
            width = requestedWidth,
            height = requestedHeight,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            minWidth = minW,
            minHeight = minH,
            maxWidth = maxW,
            maxHeight = maxH
        )
        val closePos = closeOverlayPosition(
            panelX = panelBounds.x,
            panelY = panelBounds.y,
            panelWidth = panelBounds.width,
            closeSize = closeButtonSizePx,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            offsetPx = closeOffsetPx
        )
        val cols = calculateColumns(panelBounds.width, density)
        return PopupGeometry(
            panelBounds = panelBounds,
            closePosition = closePos,
            minWidth = minW,
            minHeight = minH,
            maxWidth = maxW,
            maxHeight = maxH,
            columns = cols
        )
    }

    /**
     * Extra vertical height required by the enabled chrome elements.
     * If all chrome is disabled, returns exactly 0 (no fixed empty header/footer).
     */
    fun chromeHeightPx(
        density: Float,
        showTitle: Boolean,
        showSearch: Boolean,
        showCategories: Boolean
    ): Int {
        var chromeDp = 0f
        if (showTitle) chromeDp += CHROME_TITLE_HEIGHT_DP
        if (showSearch) chromeDp += CHROME_SEARCH_HEIGHT_DP
        if (showCategories) chromeDp += CHROME_CATEGORIES_HEIGHT_DP
        return (chromeDp * density).toInt()
    }

    /**
     * Minimum height required: at least 2 rows of stickers + enabled chrome.
     * Grid-only mode requires at least 2 rows of stickers without any fixed footer.
     */
    fun minPanelHeightPx(
        density: Float,
        showTitle: Boolean,
        showSearch: Boolean,
        showCategories: Boolean,
        minCellDp: Float = MIN_CELL_SIZE_DP,
        cellMarginDp: Float = CELL_MARGIN_DP,
        verticalPaddingDp: Float = 6f
    ): Int {
        val twoRowsDp = (2 * minCellDp) + (2 * cellMarginDp * 2) + (verticalPaddingDp * 2)
        val chromePx = chromeHeightPx(density, showTitle, showSearch, showCategories)
        return (twoRowsDp * density).toInt() + chromePx
    }

    /**
     * Clamps panel size and coordinates so the overlay is always 100% inside the visible screen.
     * Nominal minimums never win over the real viewport: on tiny screens the
     * effective minimum shrinks so the panel still fits instead of overflowing.
     */
    fun clampPanelBounds(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        screenWidth: Int,
        screenHeight: Int,
        minWidth: Int,
        minHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): PanelBounds {
        val capW = max(1, minOf(maxWidth, screenWidth))
        val capH = max(1, minOf(maxHeight, screenHeight))
        val effectiveMinW = minOf(max(1, minWidth), capW)
        val effectiveMinH = minOf(max(1, minHeight), capH)

        val clampedW = width.coerceIn(effectiveMinW, capW)
        val clampedH = height.coerceIn(effectiveMinH, capH)

        val maxX = max(0, screenWidth - clampedW)
        val maxY = max(0, screenHeight - clampedH)

        val clampedX = x.coerceIn(0, maxX)
        val clampedY = y.coerceIn(0, maxY)

        return PanelBounds(
            x = clampedX,
            y = clampedY,
            width = clampedW,
            height = clampedH
        )
    }

    /**
     * Normalized relative position (0.0f .. 1.0f) across usable screen bounds.
     * Preserves relative bubble placement across portrait and landscape rotations.
     */
    data class NormalizedPosition(
        val fractionX: Float,
        val fractionY: Float
    )

    data class BubbleBounds(
        val x: Int,
        val y: Int,
        val size: Int,
        val maxX: Int,
        val maxY: Int
    )

    /**
     * Normalizes a pixel position relative to current maximum allowed coordinates (0..maxX, 0..maxY).
     * Handles zero or negative bounds safely without division by zero.
     */
    fun normalizePosition(x: Int, y: Int, maxX: Int, maxY: Int): NormalizedPosition {
        val safeMaxX = max(0, maxX)
        val safeMaxY = max(0, maxY)
        val fractionX = if (safeMaxX > 0) (x.toFloat() / safeMaxX).coerceIn(0f, 1f) else 0f
        val fractionY = if (safeMaxY > 0) (y.toFloat() / safeMaxY).coerceIn(0f, 1f) else 0f
        return NormalizedPosition(fractionX, fractionY)
    }

    /**
     * Denormalizes a relative fraction (0..1) back into pixel coordinates for new screen bounds.
     */
    fun denormalizePosition(normalized: NormalizedPosition, maxX: Int, maxY: Int): Pair<Int, Int> {
        val safeMaxX = max(0, maxX)
        val safeMaxY = max(0, maxY)
        val pxX = Math.round(normalized.fractionX.coerceIn(0f, 1f) * safeMaxX).toInt().coerceIn(0, safeMaxX)
        val pxY = Math.round(normalized.fractionY.coerceIn(0f, 1f) * safeMaxY).toInt().coerceIn(0, safeMaxY)
        return Pair(pxX, pxY)
    }

    /**
     * Clamps bubble size and coordinates so the bubble is guaranteed 100% inside screen bounds.
     * An oversized bubble shrinks to the viewport instead of overflowing it.
     */
    fun clampBubbleBounds(
        x: Int,
        y: Int,
        bubbleSize: Int,
        screenWidth: Int,
        screenHeight: Int
    ): BubbleBounds {
        val safeSize = max(1, minOf(bubbleSize, screenWidth, screenHeight))
        val maxX = max(0, screenWidth - safeSize)
        val maxY = max(0, screenHeight - safeSize)
        val clampedX = x.coerceIn(0, maxX)
        val clampedY = y.coerceIn(0, maxY)
        return BubbleBounds(
            x = clampedX,
            y = clampedY,
            size = safeSize,
            maxX = maxX,
            maxY = maxY
        )
    }

    /**
     * Clamps floating bubble size in DP to [OverlayPreferences.MIN_BUBBLE_SIZE_DP]..[OverlayPreferences.MAX_BUBBLE_SIZE_DP].
     */
    fun clampBubbleSize(sizeDp: Float): Float {
        return sizeDp.coerceIn(
            OverlayPreferences.MIN_BUBBLE_SIZE_DP,
            OverlayPreferences.MAX_BUBBLE_SIZE_DP
        )
    }
}
