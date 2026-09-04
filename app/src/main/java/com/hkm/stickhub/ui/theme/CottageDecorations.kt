package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Faded blossom scatter for the Cottage theme.
 * Tiny four-petal posies at a whisper alpha — vintage chintz calm.
 * Zero bitmap textures, zero network assets.
 */
fun Modifier.cottageBlossoms(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val petalColor = if (isDark) Color(0xFFE8C4C4).copy(alpha = 0.08f)
    else Color(0xFFA85D68).copy(alpha = 0.07f)
    val centerColor = if (isDark) Color(0xFFC5D5C5).copy(alpha = 0.10f)
    else Color(0xFF6E8B6E).copy(alpha = 0.10f)

    val spacingX = 88.dp.toPx()
    val spacingY = 96.dp.toPx()
    val petalR = 3.2.dp.toPx()
    val petalDist = 4.2.dp.toPx()

    var row = 0
    var y = spacingY / 2f
    while (y < size.height) {
        var x = spacingX / 2f + (if (row % 2 == 1) spacingX / 2f else 0f)
        while (x < size.width) {
            val center = Offset(x, y)
            for (p in 0 until 4) {
                val angle = p * (Math.PI.toFloat() / 2f) + row * 0.4f
                drawCircle(
                    color = petalColor,
                    radius = petalR,
                    center = Offset(
                        center.x + petalDist * cos(angle),
                        center.y + petalDist * sin(angle)
                    )
                )
            }
            drawCircle(color = centerColor, radius = petalR * 0.7f, center = center)
            x += spacingX
        }
        y += spacingY
        row++
    }
}

/**
 * Wild-rose sprig motif: a soft bloom with stem and two leaves.
 * Used in airy spaces (empty states) for the Cottage theme.
 */
@Composable
fun CottageRoseMotif(
    modifier: Modifier = Modifier,
    bloom: Color = Color(0xFFE8C4C4),
    ink: Color = Color(0xFFA85D68),
    leaf: Color = Color(0xFFC5D5C5)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f

        // 1. Stem
        val stemTop = h * 0.34f
        val stemBottom = h * 0.90f
        drawLine(
            color = ink.copy(alpha = 0.7f),
            start = Offset(cx, stemTop),
            end = Offset(cx, stemBottom),
            strokeWidth = 2.dp.toPx()
        )

        // 2. Two leaves (simple quadratic blades)
        val leafPath = { mirror: Float ->
            androidx.compose.ui.graphics.Path().apply {
                val baseX = cx
                val baseY = h * 0.66f
                moveTo(baseX, baseY)
                quadraticTo(
                    baseX + mirror * w * 0.22f, baseY - h * 0.10f,
                    baseX + mirror * w * 0.26f, baseY - h * 0.02f
                )
                quadraticTo(
                    baseX + mirror * w * 0.12f, baseY + h * 0.02f,
                    baseX, baseY
                )
                close()
            }
        }
        drawPath(path = leafPath(1f), color = leaf)
        drawPath(path = leafPath(-1f), color = leaf)

        // 3. Bloom: five soft petals around a heart
        val bloomCy = h * 0.24f
        val bloomR = size.minDimension * 0.13f
        for (p in 0 until 5) {
            val angle = p * (2f * Math.PI.toFloat() / 5f) - Math.PI.toFloat() / 2f
            drawCircle(
                color = bloom,
                radius = bloomR,
                center = Offset(
                    cx + bloomR * 1.05f * cos(angle),
                    bloomCy + bloomR * 1.05f * sin(angle)
                )
            )
        }
        drawCircle(color = bloom, radius = bloomR * 0.95f, center = Offset(cx, bloomCy))
        // 4. Heart stipple + thin keepsake ring
        drawCircle(color = ink.copy(alpha = 0.5f), radius = bloomR * 0.28f, center = Offset(cx, bloomCy))
        drawCircle(
            color = ink.copy(alpha = 0.5f),
            radius = bloomR * 2.15f,
            center = Offset(cx, bloomCy),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
