package com.hkm.stickhub.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import kotlin.math.cos
import kotlin.math.sin

object StickerEditorUtil {

    /**
     * Creates a die-cut sticker outline (classic white vinyl sticker border)
     * by dilating the alpha channel of the original image.
     */
    fun addDieCutOutline(
        source: Bitmap,
        strokeWidth: Int = 12,
        strokeColor: Int = Color.WHITE
    ): Bitmap {
        if (strokeWidth <= 0) return source

        val pad = strokeWidth + 4
        val outWidth = source.width + pad * 2
        val outHeight = source.height + pad * 2

        val result = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val alphaMask = source.extractAlpha()

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColor
            style = Paint.Style.FILL
            isFilterBitmap = true
        }

        // Draw dilated alpha mask along circular radial offsets
        val steps = 24
        for (radius in 1..strokeWidth step 2) {
            for (i in 0 until steps) {
                val angle = (2 * Math.PI * i / steps).toDouble()
                val dx = (radius * cos(angle)).toFloat()
                val dy = (radius * sin(angle)).toFloat()
                canvas.drawBitmap(alphaMask, pad + dx, pad + dy, strokePaint)
            }
        }

        // Draw the original image centered on top
        canvas.drawBitmap(source, pad.toFloat(), pad.toFloat(), Paint(Paint.ANTI_ALIAS_FLAG))

        return result
    }

    /**
     * Flips image horizontally (mirror)
     */
    fun flipHorizontal(source: Bitmap): Bitmap {
        val matrix = Matrix().apply { preScale(-1f, 1f) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Rotates image 90 degrees clockwise
     */
    fun rotate90(source: Bitmap): Bitmap {
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Adds meme-style text overlay with black stroke and white fill
     */
    fun addTextCaption(
        source: Bitmap,
        caption: String,
        isTop: Boolean = false
    ): Bitmap {
        if (caption.isBlank()) return source

        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val textSize = (source.width * 0.09f).coerceIn(24f, 72f)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = textSize * 0.2f
            color = Color.BLACK
            this.textSize = textSize
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val x = source.width / 2f
        val bounds = Rect()
        fillPaint.getTextBounds(caption, 0, caption.length, bounds)

        val y = if (isTop) {
            bounds.height() + textSize * 0.4f
        } else {
            source.height - textSize * 0.4f
        }

        canvas.drawText(caption, x, y, strokePaint)
        canvas.drawText(caption, x, y, fillPaint)

        return result
    }
}
