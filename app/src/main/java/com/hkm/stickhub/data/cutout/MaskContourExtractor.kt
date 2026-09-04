package com.hkm.stickhub.data.cutout

import kotlin.math.max

/**
 * Converts a subject confidence mask into the small line segments that form
 * its visible silhouette. Segments are intentionally independent rather than
 * forced into one polygon: this preserves holes, disconnected details, and
 * diagonal contacts without a fragile contour-ordering pass.
 */
object MaskContourExtractor {
    private const val DEFAULT_THRESHOLD = 0.30f
    private const val MAX_SEGMENTS = 20_000

    fun extract(
        mask: FloatArray,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
        toNormalizedX: (Float) -> Float,
        toNormalizedY: (Float) -> Float,
        threshold: Float = DEFAULT_THRESHOLD
    ): List<CutoutContourSegment> {
        if (width <= 0 || height <= 0 || mask.size < width * height) return emptyList()
        val edge = threshold.coerceIn(0f, 1f)
        fun foreground(x: Int, y: Int): Boolean {
            return x in 0 until width && y in 0 until height && mask[y * width + x] >= edge
        }

        fun point(x: Int, y: Int): CutoutContourPoint {
            return CutoutContourPoint(
                x = toNormalizedX((startX + x).toFloat()).coerceIn(0f, 1f),
                y = toNormalizedY((startY + y).toFloat()).coerceIn(0f, 1f)
            )
        }

        // A clean subject's boundary is roughly its perimeter. The generous
        // cap protects Compose from pathological, noisy masks without adding
        // any work to the inference or save path.
        val reserve = minOf(MAX_SEGMENTS, max(16, (width + height) * 2))
        val segments = ArrayList<CutoutContourSegment>(reserve)
        fun add(startX: Int, startY: Int, endX: Int, endY: Int) {
            if (segments.size >= MAX_SEGMENTS) return
            segments += CutoutContourSegment(point(startX, startY), point(endX, endY))
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (!foreground(x, y)) continue
                if (!foreground(x, y - 1)) add(x, y, x + 1, y)
                if (!foreground(x + 1, y)) add(x + 1, y, x + 1, y + 1)
                if (!foreground(x, y + 1)) add(x + 1, y + 1, x, y + 1)
                if (!foreground(x - 1, y)) add(x, y + 1, x, y)
            }
        }
        return segments
    }
}
