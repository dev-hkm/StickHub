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

    data class PanelBounds(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    /**
     * Calculates the exact pixel width/height for a square grid cell.
     * Ensures: 3 * cellSize + (GRID_COLUMNS * 2 * cellMargin) + (2 * horizontalPadding) <= panelWidthPx.
     */
    fun gridCellSize(
        panelWidthPx: Int,
        horizontalContentPaddingPx: Int,
        cellMarginPx: Int
    ): Int {
        val totalPadding = horizontalContentPaddingPx * 2
        val totalMargins = GRID_COLUMNS * cellMarginPx * 2
        val available = panelWidthPx - totalPadding - totalMargins
        return max(1, available / GRID_COLUMNS)
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
        val effectiveMaxWidth = max(minWidth, maxWidth.coerceAtMost(screenWidth))
        val effectiveMaxHeight = max(minHeight, maxHeight.coerceAtMost(screenHeight))

        val clampedW = width.coerceIn(minWidth, effectiveMaxWidth)
        val clampedH = height.coerceIn(minHeight, effectiveMaxHeight)

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
     */
    fun clampBubbleBounds(
        x: Int,
        y: Int,
        bubbleSize: Int,
        screenWidth: Int,
        screenHeight: Int
    ): BubbleBounds {
        val safeSize = max(1, bubbleSize)
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
