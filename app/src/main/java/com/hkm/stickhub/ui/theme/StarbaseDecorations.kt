package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mission-reticle backdrop for the Starbase theme.
 * Sparse HUD plus-marks on a quiet field — retro-futurist instrument calm.
 * Zero bitmap textures, zero network assets.
 */
fun Modifier.missionReticle(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val markColor = if (isDark) Color(0xFFFFE3C3).copy(alpha = 0.10f)
    else Color(0xFF73001C).copy(alpha = 0.08f)

    val spacingPx = 84.dp.toPx()
    val armPx = 4.dp.toPx()
    val strokePx = 1.dp.toPx()

    var y = spacingPx / 2f
    while (y < size.height) {
        var x = spacingPx / 2f
        while (x < size.width) {
            drawLine(
                color = markColor,
                start = Offset(x - armPx, y),
                end = Offset(x + armPx, y),
                strokeWidth = strokePx
            )
            drawLine(
                color = markColor,
                start = Offset(x, y - armPx),
                end = Offset(x, y + armPx),
                strokeWidth = strokePx
            )
            x += spacingPx
        }
        y += spacingPx
    }
}

/**
 * Ringed-planet motif: a peach world with a tilted orbit ring and signal stars.
 * Used in airy spaces (empty states) for the Starbase theme.
 */
@Composable
fun StarbasePlanetMotif(
    modifier: Modifier = Modifier,
    planet: Color = Color(0xFFFFE3C3),
    ring: Color = Color(0xFF73001C),
    signal: Color = Color(0xFF2E6B62)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.46f
        val cy = h * 0.44f
        val planetR = size.minDimension * 0.20f

        // 1. Tilted orbit ring (drawn first so the planet sits on top)
        rotate(degrees = -18f, pivot = Offset(cx, cy)) {
            drawOval(
                color = ring,
                topLeft = Offset(cx - planetR * 1.9f, cy - planetR * 0.62f),
                size = androidx.compose.ui.geometry.Size(planetR * 3.8f, planetR * 1.24f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        // 2. Planet disc
        drawCircle(color = planet, radius = planetR, center = Offset(cx, cy))
        drawCircle(
            color = ring,
            radius = planetR,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5.dp.toPx())
        )
        // 3. Craters
        drawCircle(color = ring.copy(alpha = 0.35f), radius = planetR * 0.18f, center = Offset(cx - planetR * 0.3f, cy - planetR * 0.25f))
        drawCircle(color = ring.copy(alpha = 0.25f), radius = planetR * 0.12f, center = Offset(cx + planetR * 0.25f, cy + planetR * 0.3f))

        // 4. Signal stars
        val starR = 2.dp.toPx()
        val starAngles = listOf(0.5f, 1.7f, 2.9f, 4.4f, 5.6f)
        starAngles.forEachIndexed { index, a ->
            val orbit = planetR * (2.1f + (index % 2) * 0.5f)
            val sx = cx + orbit * cos(a).toFloat()
            val sy = cy + orbit * sin(a).toFloat() * 0.7f
            if (sx in 0f..w && sy in 0f..h) {
                drawCircle(
                    color = if (index % 2 == 0) signal else ring.copy(alpha = 0.7f),
                    radius = starR,
                    center = Offset(sx, sy)
                )
            }
        }

        // 5. Telemetry baseline
        val baseY = h * 0.86f
        drawLine(
            color = ring.copy(alpha = 0.6f),
            start = Offset(w * 0.2f, baseY),
            end = Offset(w * 0.8f, baseY),
            strokeWidth = 1.5.dp.toPx()
        )
        var tickX = w * 0.2f
        while (tickX <= w * 0.8f) {
            drawLine(
                color = ring.copy(alpha = 0.6f),
                start = Offset(tickX, baseY - 3.dp.toPx()),
                end = Offset(tickX, baseY),
                strokeWidth = 1.5.dp.toPx()
            )
            tickX += (w * 0.6f) / 6f
        }
    }
}
