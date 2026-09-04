package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Solar-glow backdrop: a warm sun aura blooming from the top edge over the
 * base — morning light through leaves.
 */
fun Modifier.solarGlow(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val glowAlpha = if (isDark) 0.30f else 0.22f
    val gold = Color(0xFFFFB703).copy(alpha = glowAlpha)
    drawRect(
        brush = Brush.radialGradient(
            0.0f to gold,
            0.6f to gold.copy(alpha = glowAlpha * 0.4f),
            1.0f to Color.Transparent,
            center = Offset(size.width * 0.5f, -size.minDimension * 0.15f),
            radius = size.minDimension * 1.1f
        ),
        size = size
    )
}

/**
 * Solar-sprout motif: a rising sun cradling a two-leaf seedling.
 * Used in airy spaces (empty states).
 */
@Composable
fun SolarLeafMotif(
    modifier: Modifier = Modifier,
    sun: Color = Color(0xFFFFB703),
    leaf: Color = Color(0xFF2D6A4F),
    stem: Color = Color(0xFF52B788)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // 1. Rising sun with rays
        val sunCy = h * 0.34f
        val sunR = size.minDimension * 0.15f
        drawCircle(color = sun, radius = sunR, center = Offset(cx, sunCy))
        for (i in 0 until 12) {
            val angle = i * (2f * Math.PI.toFloat() / 12f)
            val r0 = sunR * 1.25f
            val r1 = sunR * (if (i % 2 == 0) 1.65f else 1.45f)
            drawLine(
                color = sun.copy(alpha = 0.8f),
                start = Offset(cx + r0 * cos(angle), sunCy + r0 * sin(angle)),
                end = Offset(cx + r1 * cos(angle), sunCy + r1 * sin(angle)),
                strokeWidth = 2.dp.toPx()
            )
        }

        // 2. Seedling stem
        val stemTop = h * 0.56f
        val stemBottom = h * 0.88f
        val stemPath = Path().apply {
            moveTo(cx, stemBottom)
            quadraticTo(cx - w * 0.04f, (stemTop + stemBottom) / 2f, cx + w * 0.02f, stemTop)
        }
        drawPath(path = stemPath, color = stem, style = Stroke(width = 2.5.dp.toPx()))

        // 3. Two leaves
        fun leafBlade(mirror: Float): Path = Path().apply {
            val base = Offset(cx + w * 0.015f, h * 0.70f)
            moveTo(base.x, base.y)
            quadraticTo(
                base.x + mirror * w * 0.20f, base.y - h * 0.13f,
                base.x + mirror * w * 0.30f, base.y - h * 0.03f
            )
            quadraticTo(
                base.x + mirror * w * 0.16f, base.y + h * 0.03f,
                base.x, base.y
            )
            close()
        }
        drawPath(path = leafBlade(1f), color = leaf)
        drawPath(path = leafBlade(-1f), color = leaf.copy(alpha = 0.75f))

        // 4. Soil line
        drawLine(
            color = stem.copy(alpha = 0.6f),
            start = Offset(w * 0.24f, stemBottom),
            end = Offset(w * 0.76f, stemBottom),
            strokeWidth = 2.dp.toPx()
        )
    }
}
