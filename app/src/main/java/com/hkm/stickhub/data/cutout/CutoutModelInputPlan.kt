package com.hkm.stickhub.data.cutout

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Uniform scaling plus centered padding bounds allocation without distorting the subject. */
data class CutoutModelInputPlan(
    val width: Int,
    val height: Int,
    val contentWidth: Int,
    val contentHeight: Int,
    val left: Int,
    val top: Int
) {
    fun normalizedX(modelX: Float): Float = (modelX - left) / contentWidth
    fun normalizedY(modelY: Float): Float = (modelY - top) / contentHeight

    companion object {
        fun create(sourceWidth: Int, sourceHeight: Int): CutoutModelInputPlan {
            require(sourceWidth > 0 && sourceHeight > 0)
            val scale = min(
                max(512.0 / sourceWidth, 512.0 / sourceHeight).coerceAtLeast(1.0),
                2048.0 / max(sourceWidth, sourceHeight)
            )
            val contentWidth = (sourceWidth * scale).roundToInt().coerceIn(1, 2048)
            val contentHeight = (sourceHeight * scale).roundToInt().coerceIn(1, 2048)
            val width = max(512, contentWidth)
            val height = max(512, contentHeight)
            return CutoutModelInputPlan(width, height, contentWidth, contentHeight,
                (width - contentWidth) / 2, (height - contentHeight) / 2)
        }
    }
}
