package com.hkm.stickhub.data.cutout

import kotlin.math.min
import kotlin.math.roundToInt

data class StickerCanvasPlan(
    val canvasSize: Int,
    val contentWidth: Int,
    val contentHeight: Int,
    val left: Int,
    val top: Int
)

/** Keeps every ML cutout visually consistent without distorting its aspect ratio. */
object StickerCanvasSpec {
    const val CANVAS_SIZE = 1024
    private const val CONTENT_MAX_SIZE = 800

    fun plan(sourceWidth: Int, sourceHeight: Int): StickerCanvasPlan {
        require(sourceWidth > 0) { "sourceWidth must be positive" }
        require(sourceHeight > 0) { "sourceHeight must be positive" }

        val scale = min(
            CONTENT_MAX_SIZE.toFloat() / sourceWidth,
            CONTENT_MAX_SIZE.toFloat() / sourceHeight
        )
        val contentWidth = (sourceWidth * scale).roundToInt().coerceAtLeast(1)
        val contentHeight = (sourceHeight * scale).roundToInt().coerceAtLeast(1)
        return StickerCanvasPlan(
            canvasSize = CANVAS_SIZE,
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            left = (CANVAS_SIZE - contentWidth) / 2,
            top = (CANVAS_SIZE - contentHeight) / 2
        )
    }
}
