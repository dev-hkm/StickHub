package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sparkle-dust backdrop: tiny four-point star glints scattered over the base.
 */
fun Modifier.sparkleDust(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val glintColor = if (isDark) Color(0xFFFFB3D1).copy(alpha = 0.20f)
    else Color(0xFFC26BA8).copy(alpha = 0.14f)

    val spacingX = 64.dp.toPx()
    val spacingY = 72.dp.toPx()
    val arm = 4.dp.toPx()
    val strokePx = 1.2.dp.toPx()

    var row = 0
    var y = spacingY / 2f
    while (y < size.height) {
        var x = spacingX / 2f + (if (row % 2 == 1) spacingX / 2f else 0f)
        while (x < size.width) {
            val c = Offset(x, y)
            drawLine(glintColor, Offset(c.x - arm, c.y), Offset(c.x + arm, c.y), strokePx)
            drawLine(glintColor, Offset(c.x, c.y - arm), Offset(c.x, c.y + arm), strokePx)
            drawCircle(glintColor, arm * 0.35f, c)
            x += spacingX
        }
        y += spacingY
        row++
    }
}

private fun sparklePath(center: Offset, radius: Float): Path = Path().apply {
    // Four-point concave sparkle
    moveTo(center.x, center.y - radius)
    quadraticTo(center.x, center.y, center.x + radius, center.y)
    quadraticTo(center.x, center.y, center.x, center.y + radius)
    quadraticTo(center.x, center.y, center.x - radius, center.y)
    quadraticTo(center.x, center.y, center.x, center.y - radius)
    close()
}

/**
 * Kawaii cloud buddy motif: puffy cloud with dot eyes, smile and blush.
 * Used in airy spaces (empty states).
 */
@Composable
fun KawaiiCloudMotif(
    modifier: Modifier = Modifier,
    cloud: Color = Color(0xFFFFF9FB),
    outline: Color = Color(0xFF8E44AD),
    blush: Color = Color(0xFFFF9EBB)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.44f
        val baseR = size.minDimension * 0.17f

        // 1. Puffy cloud body
        val puffs = listOf(
            Offset(cx - baseR * 1.15f, cy + baseR * 0.25f) to baseR * 0.72f,
            Offset(cx - baseR * 0.45f, cy - baseR * 0.30f) to baseR * 0.95f,
            Offset(cx + baseR * 0.45f, cy - baseR * 0.25f) to baseR * 0.88f,
            Offset(cx + baseR * 1.10f, cy + baseR * 0.25f) to baseR * 0.68f,
            Offset(cx, cy + baseR * 0.35f) to baseR * 1.02f
        )
        puffs.forEach { (center, r) -> drawCircle(color = cloud, radius = r, center = center) }
        puffs.forEach { (center, r) ->
            drawArc(
                color = outline.copy(alpha = 0.55f),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(center.x - r, center.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2f, r * 2f),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 2. Dot eyes + open smile
        val eyeY = cy + baseR * 0.05f
        drawCircle(color = outline, radius = 3.dp.toPx(), center = Offset(cx - baseR * 0.42f, eyeY))
        drawCircle(color = outline, radius = 3.dp.toPx(), center = Offset(cx + baseR * 0.42f, eyeY))
        drawArc(
            color = outline,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - baseR * 0.28f, eyeY),
            size = androidx.compose.ui.geometry.Size(baseR * 0.56f, baseR * 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )
        // 3. Blush + brow sparkles
        drawCircle(color = blush.copy(alpha = 0.8f), radius = 4.dp.toPx(), center = Offset(cx - baseR * 0.78f, eyeY + baseR * 0.22f))
        drawCircle(color = blush.copy(alpha = 0.8f), radius = 4.dp.toPx(), center = Offset(cx + baseR * 0.78f, eyeY + baseR * 0.22f))
        drawPath(path = sparklePath(Offset(cx - baseR * 1.5f, cy - baseR * 0.9f), 7.dp.toPx()), color = blush)
        drawPath(path = sparklePath(Offset(cx + baseR * 1.55f, cy - baseR * 0.55f), 5.dp.toPx()), color = blush.copy(alpha = 0.8f))
    }
}
