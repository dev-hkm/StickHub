package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Procedural dot-grid backdrop for the Neubrutalism theme.
 * Evenly spaced ink dots at low alpha — the signature neubrutalist canvas.
 * Zero bitmap textures, zero network assets.
 */
fun Modifier.neubrutalistDots(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val dotColor = if (isDark) Color(0xFFF2ECDA).copy(alpha = 0.10f)
    else Color(0xFF161616).copy(alpha = 0.08f)

    val spacingPx = 26.dp.toPx()
    val radiusPx = 1.6.dp.toPx()

    var y = spacingPx / 2f
    while (y < size.height) {
        var x = spacingPx / 2f
        while (x < size.width) {
            drawCircle(color = dotColor, radius = radiusPx, center = Offset(x, y))
            x += spacingPx
        }
        y += spacingPx
    }
}

/**
 * Hard-shadow sticker badge rendered cleanly via vector rects.
 * A candy fill card with a thick ink border and a solid offset shadow —
 * the defining neubrutalist molecule. Used in airy spaces (empty states).
 */
@Composable
fun NeoStickerMotif(
    modifier: Modifier = Modifier,
    fill: Color = Color(0xFFFFEB3B),
    ink: Color = Color(0xFF161616),
    accent: Color = Color(0xFFFF5252)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cardLeft = w * 0.16f
        val cardTop = h * 0.12f
        val cardRight = w * 0.78f
        val cardBottom = h * 0.74f
        val cardSize = Size(cardRight - cardLeft, cardBottom - cardTop)
        val radius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        val shadowOffset = 7.dp.toPx()
        val border = Stroke(width = 3.dp.toPx())

        // 1. Solid offset drop shadow (no blur, ever)
        drawRoundRect(
            color = ink,
            topLeft = Offset(cardLeft + shadowOffset, cardTop + shadowOffset),
            size = cardSize,
            cornerRadius = radius
        )
        // 2. Candy fill
        drawRoundRect(
            color = fill,
            topLeft = Offset(cardLeft, cardTop),
            size = cardSize,
            cornerRadius = radius
        )
        // 3. Thick ink border
        drawRoundRect(
            color = ink,
            topLeft = Offset(cardLeft, cardTop),
            size = cardSize,
            cornerRadius = radius,
            style = border
        )

        // 4. Playful accent star burst on the card
        val cx = (cardLeft + cardRight) / 2f
        val cy = (cardTop + cardBottom) / 2f
        val armLong = (cardSize.minDimension) * 0.26f
        val armShort = armLong * 0.38f
        val starPath = Path().apply {
            for (i in 0 until 8) {
                val long = i % 2 == 0
                val r = if (long) armLong else armShort
                val angle = (kotlin.math.PI.toFloat() / 4 * i - kotlin.math.PI.toFloat() / 2)
                val px = cx + r * kotlin.math.cos(angle)
                val py = cy + r * kotlin.math.sin(angle)
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(path = starPath, color = accent)
        drawPath(
            path = starPath,
            color = ink,
            style = Stroke(width = 2.dp.toPx())
        )

        // 5. Small satellite square, bottom-right, blue pop
        val sat = 16.dp.toPx()
        val satLeft = w * 0.74f
        val satTop = h * 0.70f
        drawRect(color = ink, topLeft = Offset(satLeft + 4.dp.toPx(), satTop + 4.dp.toPx()), size = Size(sat, sat))
        drawRect(color = Color(0xFF2196F3), topLeft = Offset(satLeft, satTop), size = Size(sat, sat))
        drawRect(
            color = ink,
            topLeft = Offset(satLeft, satTop),
            size = Size(sat, sat),
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}
