package com.hkm.stickhub.data.cutout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/** Renders one cropped subject on the common transparent sticker canvas. */
object StickerCanvasNormalizer {

    fun normalize(source: Bitmap): Bitmap {
        val plan = StickerCanvasSpec.plan(source.width, source.height)
        val output = Bitmap.createBitmap(
            plan.canvasSize,
            plan.canvasSize,
            Bitmap.Config.ARGB_8888
        )
        val destination = RectF(
            plan.left.toFloat(),
            plan.top.toFloat(),
            (plan.left + plan.contentWidth).toFloat(),
            (plan.top + plan.contentHeight).toFloat()
        )
        Canvas(output).drawBitmap(
            source,
            null,
            destination,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
        return output
    }
}
