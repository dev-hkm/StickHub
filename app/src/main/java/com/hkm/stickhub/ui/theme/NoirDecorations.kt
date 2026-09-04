package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Venetian-blind backdrop: alternating horizontal slat bands with a soft
 * spotlight cone from the top — conceal to reveal.
 */
fun Modifier.venetianSlats(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val slatColor = if (isDark) Color.Black.copy(alpha = 0.28f)
    else Color(0xFF232323).copy(alpha = 0.05f)

    val slatH = 14.dp.toPx()
    val gapH = 26.dp.toPx()
    var y = 0f
    while (y < size.height) {
        drawRect(color = slatColor, topLeft = Offset(0f, y), size = Size(size.width, slatH))
        y += slatH + gapH
    }

    // Spotlight cone wash
    val lampColor = if (isDark) Color(0xFFE8C547).copy(alpha = 0.07f)
    else Color(0xFFE8C547).copy(alpha = 0.10f)
    drawRect(
        brush = Brush.linearGradient(
            0.0f to lampColor,
            1.0f to Color.Transparent,
            start = Offset(size.width * 0.5f, 0f),
            end = Offset(size.width * 0.5f, size.height * 0.6f)
        ),
        size = size
    )
}

/**
 * Streetlamp motif: glowing lamp head, light cone and rain slashes.
 * Used in airy spaces (empty states).
 */
@Composable
fun NoirLampMotif(
    modifier: Modifier = Modifier,
    lamp: Color = Color(0xFFE8C547),
    ink: Color = Color(0xFF1A1A1E),
    rain: Color = Color(0xFF8E8E96)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f

        // 1. Lamp post
        drawLine(
            color = ink,
            start = Offset(cx, h * 0.26f),
            end = Offset(cx, h * 0.92f),
            strokeWidth = 3.dp.toPx()
        )
        // 2. Lamp head cap + glowing bulb
        drawRect(color = ink, topLeft = Offset(cx - 12.dp.toPx(), h * 0.18f), size = Size(24.dp.toPx(), 5.dp.toPx()))
        drawCircle(color = lamp, radius = 9.dp.toPx(), center = Offset(cx, h * 0.28f))
        drawCircle(color = lamp.copy(alpha = 0.25f), radius = 16.dp.toPx(), center = Offset(cx, h * 0.28f))
        // 3. Light cone
        val cone = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx - 9.dp.toPx(), h * 0.30f)
            lineTo(cx + 9.dp.toPx(), h * 0.30f)
            lineTo(cx + w * 0.26f, h * 0.92f)
            lineTo(cx - w * 0.26f, h * 0.92f)
            close()
        }
        drawPath(path = cone, color = lamp.copy(alpha = 0.14f))
        // 4. Rain slashes
        val rainLines = listOf(
            Offset(w * 0.16f, h * 0.30f) to Offset(w * 0.12f, h * 0.48f),
            Offset(w * 0.84f, h * 0.22f) to Offset(w * 0.80f, h * 0.40f),
            Offset(w * 0.72f, h * 0.55f) to Offset(w * 0.68f, h * 0.73f),
            Offset(w * 0.26f, h * 0.62f) to Offset(w * 0.22f, h * 0.80f)
        )
        rainLines.forEach { (a, b) ->
            drawLine(color = rain.copy(alpha = 0.7f), start = a, end = b, strokeWidth = 1.5.dp.toPx())
        }
    }
}
