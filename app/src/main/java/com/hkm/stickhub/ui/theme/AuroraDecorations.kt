package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Aurora mesh backdrop: three soft overlapping light fields (blue / magenta /
 * cyan) breathing over the base. Static radial fields — cheap on GPU.
 */
fun Modifier.auroraMesh(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val alpha = if (isDark) 0.34f else 0.20f
    val blue = Color(0xFF0080FF).copy(alpha = alpha)
    val magenta = Color(0xFFFF1493).copy(alpha = alpha * 0.9f)
    val cyan = Color(0xFF00C8C8).copy(alpha = alpha * 0.9f)
    val transparent = Color.Transparent

    drawRect(
        brush = Brush.radialGradient(
            0.0f to blue, 0.55f to blue.copy(alpha = alpha * 0.35f), 1.0f to transparent,
            center = Offset(size.width * 0.18f, size.height * 0.12f),
            radius = size.minDimension * 0.85f
        ),
        size = size
    )
    drawRect(
        brush = Brush.radialGradient(
            0.0f to magenta, 0.55f to magenta.copy(alpha = alpha * 0.35f), 1.0f to transparent,
            center = Offset(size.width * 0.88f, size.height * 0.42f),
            radius = size.minDimension * 0.9f
        ),
        size = size
    )
    drawRect(
        brush = Brush.radialGradient(
            0.0f to cyan, 0.55f to cyan.copy(alpha = alpha * 0.35f), 1.0f to transparent,
            center = Offset(size.width * 0.45f, size.height * 0.95f),
            radius = size.minDimension * 0.95f
        ),
        size = size
    )
}

/**
 * Aurora ribbon motif: three flowing sine bands (blue / magenta / cyan)
 * crossing like northern lights. Used in airy spaces (empty states).
 */
@Composable
fun AuroraRibbonMotif(
    modifier: Modifier = Modifier,
    bandA: Color = Color(0xFF0080FF),
    bandB: Color = Color(0xFFFF1493),
    bandC: Color = Color(0xFF00C8C8)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bands = listOf(
            Triple(bandA, h * 0.30f, h * 0.10f),
            Triple(bandB, h * 0.50f, h * 0.13f),
            Triple(bandC, h * 0.70f, h * 0.09f)
        )
        bands.forEachIndexed { index, (color, baseY, amplitude) ->
            val path = Path().apply {
                moveTo(0f, baseY)
                var x = 0f
                val phase = index * 1.4f
                while (x <= w) {
                    lineTo(x, (baseY + amplitude * sin(x / w * 6.28f + phase).toFloat()).toFloat())
                    x += w / 48f
                }
            }
            drawPath(
                path = path,
                color = color.copy(alpha = 0.75f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = (5 - index).dp.toPx())
            )
        }
        // Scattered sparkle dust
        val dust = listOf(
            Offset(w * 0.15f, h * 0.12f), Offset(w * 0.72f, h * 0.18f),
            Offset(w * 0.88f, h * 0.62f), Offset(w * 0.30f, h * 0.85f),
            Offset(w * 0.55f, h * 0.08f)
        )
        dust.forEach { drawCircle(color = bandC.copy(alpha = 0.6f), radius = 1.6.dp.toPx(), center = it) }
    }
}
