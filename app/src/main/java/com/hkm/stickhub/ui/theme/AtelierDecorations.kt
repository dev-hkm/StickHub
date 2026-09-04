package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Gallery hairline backdrop for the Atelier theme.
 * Whisper-quiet wide-spaced vertical hairlines — restraint as decoration.
 * Zero bitmap textures, zero network assets.
 */
fun Modifier.galleryHairlines(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val lineColor = if (isDark) Color(0xFFFFE9D2).copy(alpha = 0.07f)
    else Color(0xFFB2532B).copy(alpha = 0.06f)

    val spacingPx = 96.dp.toPx()
    val strokePx = 1.dp.toPx()

    var x = spacingPx / 2f
    while (x < size.width) {
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = strokePx
        )
        x += spacingPx
    }
}

/**
 * Gallery frame motif: a thin terracotta frame holding a rising sun disc.
 * Minimalist exhibition label energy for airy spaces (empty states).
 */
@Composable
fun AtelierFrameMotif(
    modifier: Modifier = Modifier,
    frame: Color = Color(0xFFB2532B),
    sun: Color = Color(0xFFE67D54),
    paper: Color = Color(0xFFFFFFFF)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val frameLeft = w * 0.24f
        val frameTop = h * 0.10f
        val frameRight = w * 0.76f
        val frameBottom = h * 0.90f
        val frameSize = Size(frameRight - frameLeft, frameBottom - frameTop)
        val radius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

        // 1. Paper card
        drawRoundRect(
            color = paper,
            topLeft = Offset(frameLeft, frameTop),
            size = frameSize,
            cornerRadius = radius
        )
        // 2. Thin terracotta frame
        drawRoundRect(
            color = frame,
            topLeft = Offset(frameLeft, frameTop),
            size = frameSize,
            cornerRadius = radius,
            style = Stroke(width = 2.dp.toPx())
        )

        // 3. Rising sun disc, centered upper third
        val sunCx = (frameLeft + frameRight) / 2f
        val sunCy = frameTop + frameSize.height * 0.38f
        drawCircle(color = sun, radius = frameSize.width * 0.17f, center = Offset(sunCx, sunCy))

        // 4. Horizon hairline + two caption rules beneath
        val innerLeft = frameLeft + 10.dp.toPx()
        val innerRight = frameRight - 10.dp.toPx()
        var y = frameTop + frameSize.height * 0.62f
        drawLine(
            color = frame,
            start = Offset(innerLeft, y),
            end = Offset(innerRight, y),
            strokeWidth = 1.5.dp.toPx()
        )
        y += 8.dp.toPx()
        drawLine(
            color = frame.copy(alpha = 0.55f),
            start = Offset(innerLeft, y),
            end = Offset(innerLeft + (innerRight - innerLeft) * 0.7f, y),
            strokeWidth = 1.5.dp.toPx()
        )
        y += 6.dp.toPx()
        drawLine(
            color = frame.copy(alpha = 0.35f),
            start = Offset(innerLeft, y),
            end = Offset(innerLeft + (innerRight - innerLeft) * 0.45f, y),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}
