package com.hkm.stickhub.ui.components

/** One full-span header followed by uniform sticker rows. All dimensions are pixels. */
internal data class LibraryScrollGeometry(
    val totalItems: Int,
    val columns: Int,
    val headerHeight: Int,
    val rowHeight: Int,
    val spacing: Int,
    val viewport: Int,
    val padding: Int = 0
) {
    private val safeColumns = columns.coerceAtLeast(1)
    private val rows = ((totalItems - 1).coerceAtLeast(0) + safeColumns - 1) / safeColumns
    private val stride = (rowHeight + spacing).coerceAtLeast(1)
    private val headerStride = headerHeight + if (rows > 0) spacing else 0
    val contentSize: Float = (headerStride + rows * stride - if (rows > 0) spacing else 0).coerceAtLeast(0).toFloat() + padding
    val maxOffset: Float = (contentSize - viewport).coerceAtLeast(0f)
    fun offset(index: Int, offset: Int): Float =
        (if (index == 0) offset.toFloat() else headerStride + ((index - 1) / safeColumns) * stride + offset.toFloat()).coerceIn(0f, maxOffset)

    fun target(fraction: Float): Pair<Int, Int> {
        if (totalItems <= 1 || !fraction.isFinite() || fraction <= 0f || maxOffset <= 0f) return 0 to 0
        val pixels = (fraction.coerceIn(0f, 1f) * maxOffset).toInt()
        if (pixels < headerStride) return 0 to pixels
        val rest = pixels - headerStride
        return (1 + rest / stride * safeColumns).coerceAtMost(totalItems - 1) to (rest % stride)
    }
}
