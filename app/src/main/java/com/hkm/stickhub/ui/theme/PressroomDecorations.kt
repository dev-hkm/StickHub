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
 * Column-rule backdrop for the Pressroom theme.
 * Sparse vertical hairlines like a broadsheet grid — airy, editorial calm.
 * Zero bitmap textures, zero network assets.
 */
fun Modifier.pressroomColumns(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val lineColor = if (isDark) Color(0xFFFFE3C3).copy(alpha = 0.08f)
    else Color(0xFF5B3A30).copy(alpha = 0.07f)

    val spacingPx = 72.dp.toPx()
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
 * Front-page motif: a miniature broadsheet with masthead bar and column rules.
 * Used in airy spaces (empty states) for the Pressroom theme.
 */
@Composable
fun PressFrontPageMotif(
    modifier: Modifier = Modifier,
    paper: Color = Color(0xFFFFFDF8),
    ink: Color = Color(0xFF5B3A30),
    accent: Color = Color(0xFFFFE3C3)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val pageLeft = w * 0.22f
        val pageTop = h * 0.08f
        val pageRight = w * 0.78f
        val pageBottom = h * 0.92f
        val pageSize = Size(pageRight - pageLeft, pageBottom - pageTop)
        val radius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        val border = Stroke(width = 2.5.dp.toPx())

        // 1. Page shadow wash (flat, offset)
        drawRoundRect(
            color = ink.copy(alpha = 0.18f),
            topLeft = Offset(pageLeft + 5.dp.toPx(), pageTop + 5.dp.toPx()),
            size = pageSize,
            cornerRadius = radius
        )
        // 2. Paper
        drawRoundRect(
            color = paper,
            topLeft = Offset(pageLeft, pageTop),
            size = pageSize,
            cornerRadius = radius
        )
        drawRoundRect(
            color = ink,
            topLeft = Offset(pageLeft, pageTop),
            size = pageSize,
            cornerRadius = radius,
            style = border
        )

        val innerLeft = pageLeft + 8.dp.toPx()
        val innerRight = pageRight - 8.dp.toPx()
        var y = pageTop + 10.dp.toPx()

        // 3. Masthead bar (peach highlight strip)
        drawRect(
            color = accent,
            topLeft = Offset(innerLeft, y),
            size = Size(innerRight - innerLeft, 9.dp.toPx())
        )
        y += 14.dp.toPx()

        // 4. Headline rule (thick ink)
        drawRect(
            color = ink,
            topLeft = Offset(innerLeft, y),
            size = Size((innerRight - innerLeft) * 0.82f, 4.dp.toPx())
        )
        y += 10.dp.toPx()

        // 5. Body column rules (two columns)
        val gutter = 5.dp.toPx()
        val colW = (innerRight - innerLeft - gutter) / 2f
        val ruleH = 2.dp.toPx()
        val lineGap = 5.dp.toPx()
        val lines = 5
        repeat(lines) {
            if (y + ruleH > pageBottom - 8.dp.toPx()) return@repeat
            drawRect(color = ink.copy(alpha = 0.55f), topLeft = Offset(innerLeft, y), size = Size(colW, ruleH))
            drawRect(
                color = ink.copy(alpha = 0.55f),
                topLeft = Offset(innerLeft + colW + gutter, y),
                size = Size(colW * if (it == lines - 1) 0.6f else 1f, ruleH)
            )
            y += lineGap
        }
    }
}
