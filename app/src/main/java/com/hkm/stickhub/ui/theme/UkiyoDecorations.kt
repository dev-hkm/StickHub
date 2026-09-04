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
 * Seigaiha wave backdrop: overlapping arc rows like the classic
 * blue-sea pattern — quiet, structural, unmistakably ukiyo-e.
 */
fun Modifier.seigaihaWaves(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val arcColor = if (isDark) Color(0xFF8FA3B8).copy(alpha = 0.10f)
    else Color(0xFF2A4056).copy(alpha = 0.08f)

    val radius = 34.dp.toPx()
    val strokePx = 1.dp.toPx()
    val rowH = radius * 0.52f

    var row = 0
    var y = 0f
    while (y < size.height + radius) {
        var x = if (row % 2 == 0) 0f else -radius
        while (x < size.width + radius) {
            drawArc(
                color = arcColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(x - radius, y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokePx)
            )
            x += radius * 2f
        }
        y += rowH
        row++
    }
}

/**
 * Great-wave motif: vermilion sun over three curling wave bands.
 * Used in airy spaces (empty states).
 */
@Composable
fun WaveMotif(
    modifier: Modifier = Modifier,
    sun: Color = Color(0xFFE85D35),
    wave: Color = Color(0xFF2A4056),
    foam: Color = Color(0xFFF0E3CE)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Vermilion sun
        val sunR = size.minDimension * 0.16f
        drawCircle(color = sun, radius = sunR, center = Offset(w * 0.68f, h * 0.26f))

        // 2. Three curling wave bands
        val bandTops = listOf(h * 0.52f, h * 0.66f, h * 0.80f)
        bandTops.forEachIndexed { index, topY ->
            val amp = h * 0.05f
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, topY)
                var x = 0f
                while (x <= w) {
                    val y = topY + amp * sin(x / w * 6.28f + index * 1.1f).toFloat()
                    lineTo(x, y)
                    x += w / 40f
                }
                lineTo(w, topY + amp * 2.4f)
                lineTo(0f, topY + amp * 2.4f)
                close()
            }
            drawPath(path = path, color = wave.copy(alpha = 0.85f - index * 0.12f))
            // Foam crest
            val crest = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, topY)
                var x = 0f
                while (x <= w) {
                    val y = topY + amp * sin(x / w * 6.28f + index * 1.1f).toFloat()
                    lineTo(x, y)
                    x += w / 40f
                }
            }
            drawPath(
                path = crest,
                color = foam.copy(alpha = 0.9f),
                style = Stroke(width = 2.dp.toPx())
            )
            // Claw droplet
            val dropX = w * (0.25f + index * 0.22f)
            drawCircle(color = foam, radius = 3.dp.toPx(), center = Offset(dropX, topY - amp * 0.9f))
        }
    }
}
