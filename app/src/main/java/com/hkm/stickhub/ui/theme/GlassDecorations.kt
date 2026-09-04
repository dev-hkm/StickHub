package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Frosted-blob backdrop: large translucent color fields floating over the
 * base — liquid light, frozen mid-flow. Static for GPU sanity.
 */
fun Modifier.glassBlobs(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val a = if (isDark) 0.30f else 0.22f
    val blue = Color(0xFF0066FF).copy(alpha = a)
    val violet = Color(0xFF7C5CFC).copy(alpha = a * 0.9f)
    val cyan = Color(0xFF00B8D4).copy(alpha = a * 0.85f)

    fun blob(center: Offset, color: Color, rx: Float, ry: Float) {
        drawOval(color = color, topLeft = Offset(center.x - rx, center.y - ry),
            size = androidx.compose.ui.geometry.Size(rx * 2f, ry * 2f))
    }
    blob(Offset(size.width * 0.12f, size.height * 0.10f), blue, size.minDimension * 0.42f, size.minDimension * 0.30f)
    blob(Offset(size.width * 0.90f, size.height * 0.38f), violet, size.minDimension * 0.45f, size.minDimension * 0.32f)
    blob(Offset(size.width * 0.42f, size.height * 0.96f), cyan, size.minDimension * 0.48f, size.minDimension * 0.30f)

    // Frost sheen diagonal
    drawRect(
        brush = Brush.linearGradient(
            0.0f to Color.White.copy(alpha = if (isDark) 0.05f else 0.14f),
            0.5f to Color.Transparent,
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.6f, size.height * 0.6f)
        ),
        size = size
    )
}

/**
 * Droplet motif: a glass drop with crescent shine and orbiting bubbles.
 * Used in airy spaces (empty states).
 */
@Composable
fun GlassDropletMotif(
    modifier: Modifier = Modifier,
    glass: Color = Color(0xFF0066FF),
    shine: Color = Color(0xFFFFFFFF)
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height * 0.46f
        val r = size.minDimension * 0.20f

        // 1. Halo
        drawCircle(color = glass.copy(alpha = 0.18f), radius = r * 1.7f, center = Offset(cx, cy))
        // 2. Droplet body (round bottom, soft tip)
        val body = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy - r * 1.5f)
            quadraticTo(cx + r * 0.9f, cy - r * 0.2f, cx + r * 0.9f, cy + r * 0.25f)
            quadraticTo(cx + r * 0.9f, cy + r * 1.05f, cx, cy + r * 1.05f)
            quadraticTo(cx - r * 0.9f, cy + r * 1.05f, cx - r * 0.9f, cy + r * 0.25f)
            quadraticTo(cx - r * 0.9f, cy - r * 0.2f, cx, cy - r * 1.5f)
            close()
        }
        drawPath(path = body, color = glass.copy(alpha = 0.55f))
        drawPath(
            path = body,
            color = shine.copy(alpha = 0.8f),
            style = Stroke(width = 2.dp.toPx())
        )
        // 3. Crescent shine
        drawArc(
            color = shine.copy(alpha = 0.9f),
            startAngle = 160f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(cx - r * 0.62f, cy - r * 0.15f),
            size = androidx.compose.ui.geometry.Size(r * 0.7f, r * 1.1f),
            style = Stroke(width = 2.5.dp.toPx())
        )
        // 4. Orbiting micro-bubbles
        drawCircle(color = glass.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(cx + r * 1.5f, cy - r * 0.9f))
        drawCircle(color = glass.copy(alpha = 0.35f), radius = 2.dp.toPx(), center = Offset(cx - r * 1.6f, cy + r * 0.5f))
        drawCircle(color = shine.copy(alpha = 0.7f), radius = 1.6.dp.toPx(), center = Offset(cx + r * 1.1f, cy + r * 1.3f))
        // 5. Baseline reflection
        drawRoundRect(
            color = glass.copy(alpha = 0.25f),
            topLeft = Offset(cx - r * 1.2f, cy + r * 1.55f),
            size = androidx.compose.ui.geometry.Size(r * 2.4f, 3.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
    }
}
