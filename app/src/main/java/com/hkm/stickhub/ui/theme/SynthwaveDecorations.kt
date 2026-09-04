package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Synthwave grid backdrop: perspective-free neon lattice with a horizon glow
 * band. Restrained so lists stay readable under it.
 */
fun Modifier.synthGrid(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val lineColor = if (isDark) Color(0xFF3D9BFF).copy(alpha = 0.13f)
    else Color(0xFF5D34D0).copy(alpha = 0.10f)

    val stepPx = 44.dp.toPx()
    val strokePx = 1.dp.toPx()

    var x = stepPx / 2f
    while (x < size.width) {
        drawLine(color = lineColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = strokePx)
        x += stepPx
    }
    var y = stepPx / 2f
    while (y < size.height) {
        drawLine(color = lineColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = strokePx)
        y += stepPx
    }
    // Horizon glow band
    val horizonY = size.height * 0.30f
    drawRect(
        color = lineColor.copy(alpha = 0.35f),
        topLeft = Offset(0f, horizonY),
        size = Size(size.width, 2.dp.toPx())
    )
}

/**
 * Chrome-sun motif: slatted synthwave sun rising over a lattice floor.
 * Used in airy spaces (empty states).
 */
@Composable
fun SynthSunMotif(
    modifier: Modifier = Modifier,
    sun: Color = Color(0xFFFF4D9A),
    sunLow: Color = Color(0xFFFFB703),
    grid: Color = Color(0xFF3D9BFF)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val sunR = size.minDimension * 0.24f
        val sunCy = h * 0.40f

        // 1. Sun disc (two-tone split)
        drawCircle(color = sun, radius = sunR, center = Offset(cx, sunCy))
        drawRect(
            color = sunLow.copy(alpha = 0.85f),
            topLeft = Offset(cx - sunR, sunCy),
            size = Size(sunR * 2f, sunR)
        )
        // 2. Slat cutouts across the lower sun (slatted-sun illusion)
        var slatY = sunCy + sunR * 0.05f
        var slatH = 2.dp.toPx()
        while (slatY < sunCy + sunR) {
            drawRect(
                color = grid.copy(alpha = 0.85f),
                topLeft = Offset(cx - sunR, slatY),
                size = Size(sunR * 2f, slatH)
            )
            slatY += slatH + 4.dp.toPx()
            slatH += 1.dp.toPx()
        }
        // 3. Lattice floor
        val floorTop = h * 0.72f
        val laneCount = 6
        for (i in 0..laneCount) {
            val t = i / laneCount.toFloat()
            val y = floorTop + (h - floorTop) * t * t
            drawLine(
                color = grid.copy(alpha = 0.7f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.5.dp.toPx()
            )
        }
        for (i in -4..4) {
            drawLine(
                color = grid.copy(alpha = 0.45f),
                start = Offset(cx + i * w * 0.06f, floorTop),
                end = Offset(cx + i * w * 0.22f, h),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}
