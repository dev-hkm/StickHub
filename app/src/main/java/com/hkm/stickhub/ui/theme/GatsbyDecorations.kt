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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Deco frame backdrop: a thin double-rule inset frame — black-tie geometry
 * that never fights list content.
 */
fun Modifier.decoFrame(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val ruleColor = if (isDark) Color(0xFFD4AF37).copy(alpha = 0.22f)
    else Color(0xFFA8862C).copy(alpha = 0.30f)

    val outer = 10.dp.toPx()
    val inner = 15.dp.toPx()
    drawRoundRect(
        color = ruleColor,
        topLeft = Offset(outer, outer),
        size = Size(size.width - outer * 2f, size.height - outer * 2f),
        cornerRadius = CornerRadius(10.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )
    drawRoundRect(
        color = ruleColor.copy(alpha = 0.6f),
        topLeft = Offset(inner, inner),
        size = Size(size.width - inner * 2f, size.height - inner * 2f),
        cornerRadius = CornerRadius(7.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )
}

/**
 * Deco sunburst motif: stepped arch with radiating gold rays.
 * Used in airy spaces (empty states).
 */
@Composable
fun DecoSunMotif(
    modifier: Modifier = Modifier,
    gold: Color = Color(0xFFD4AF37),
    ink: Color = Color(0xFF0B0B0E)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val baseY = h * 0.78f

        // 1. Radiating rays
        val rayCount = 13
        for (i in 0 until rayCount) {
            val t = i / (rayCount - 1f)
            val angle = Math.PI.toFloat() * (1f - t)
            val r0 = size.minDimension * 0.16f
            val r1 = size.minDimension * 0.42f
            drawLine(
                color = gold.copy(alpha = if (i % 2 == 0) 0.9f else 0.45f),
                start = Offset(cx + r0 * cos(angle), baseY - r0 * sin(angle)),
                end = Offset(cx + r1 * cos(angle), baseY - r1 * sin(angle)),
                strokeWidth = (if (i % 2 == 0) 2.5f else 1.5f).dp.toPx()
            )
        }
        // 2. Stepped arch base
        val stepH = 5.dp.toPx()
        val widths = listOf(0.62f, 0.50f, 0.38f)
        widths.forEachIndexed { index, frac ->
            val half = w * frac / 2f
            drawRect(
                color = if (index == 1) gold else ink,
                topLeft = Offset(cx - half, baseY + index * (stepH + 2.dp.toPx())),
                size = Size(half * 2f, stepH)
            )
        }
        // 3. Center medallion
        drawCircle(color = gold, radius = 7.dp.toPx(), center = Offset(cx, baseY - size.minDimension * 0.10f))
        drawCircle(
            color = ink,
            radius = 7.dp.toPx(),
            center = Offset(cx, baseY - size.minDimension * 0.10f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        // 4. Flanking chevrons
        val chevY = baseY + 3 * (stepH + 2.dp.toPx()) + 6.dp.toPx()
        listOf(-1f, 1f).forEach { side ->
            val chev = Path().apply {
                moveTo(cx + side * w * 0.10f, chevY)
                lineTo(cx + side * w * 0.20f, chevY - 6.dp.toPx())
                lineTo(cx + side * w * 0.30f, chevY)
            }
            drawPath(path = chev, color = gold.copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))
        }
    }
}
