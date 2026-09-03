package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

object BotanicalTokens {
    val CornerSmall = 8.dp
    val CornerStandard = 12.dp
    val CornerCard = 16.dp
    val CornerBottomSheet = 28.dp
    val SpacingRhythm = 8.dp
}

/**
 * Procedural parchment wash applied to backgrounds for the Herbarium theme.
 * Uses lightweight radial gradients and noise-free soft washes; zero bitmap texture overhead.
 */
fun Modifier.parchmentWash(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val baseWarm = if (isDark) Color(0xFF241E17) else Color(0xFFE5D5BC)
    val secondaryWarm = if (isDark) Color(0xFF1E1914) else Color(0xFFF7EEDD)

    // Top-left organic warm paper wash
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(baseWarm.copy(alpha = if (isDark) 0.12f else 0.18f), Color.Transparent),
            center = Offset(size.width * 0.15f, size.height * 0.1f),
            radius = size.width * 0.7f
        )
    )

    // Bottom-right subtle ink tint wash
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(secondaryWarm.copy(alpha = if (isDark) 0.08f else 0.15f), Color.Transparent),
            center = Offset(size.width * 0.85f, size.height * 0.85f),
            radius = size.width * 0.8f
        )
    )
}

/**
 * Delicate botanical line-art illustration rendered in vector paths.
 * Used exclusively in airy empty states, header accents, and settings preview surfaces.
 */
@Composable
fun BotanicalLeafMotif(
    modifier: Modifier = Modifier,
    tint: Color = BotanicalColors.LightLeafGreenPrimary,
    alpha: Float = 0.08f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val effectiveColor = tint.copy(alpha = alpha)
        val strokeStyle = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Main stem
        val stemPath = Path().apply {
            moveTo(w * 0.5f, h * 0.95f)
            cubicTo(
                w * 0.48f, h * 0.70f,
                w * 0.54f, h * 0.40f,
                w * 0.50f, h * 0.05f
            )
        }
        drawPath(stemPath, color = effectiveColor, style = strokeStyle)

        // Leaf pair 1 (lower)
        val leaf1Left = Path().apply {
            moveTo(w * 0.49f, h * 0.75f)
            cubicTo(w * 0.32f, h * 0.72f, w * 0.20f, h * 0.65f, w * 0.18f, h * 0.58f)
            cubicTo(w * 0.25f, h * 0.56f, w * 0.40f, h * 0.63f, w * 0.49f, h * 0.70f)
        }
        drawPath(leaf1Left, color = effectiveColor, style = strokeStyle)

        val leaf1Right = Path().apply {
            moveTo(w * 0.51f, h * 0.70f)
            cubicTo(w * 0.68f, h * 0.66f, w * 0.80f, h * 0.58f, w * 0.82f, h * 0.50f)
            cubicTo(w * 0.75f, h * 0.50f, w * 0.60f, h * 0.57f, w * 0.51f, h * 0.65f)
        }
        drawPath(leaf1Right, color = effectiveColor, style = strokeStyle)

        // Leaf pair 2 (middle)
        val leaf2Left = Path().apply {
            moveTo(w * 0.51f, h * 0.50f)
            cubicTo(w * 0.35f, h * 0.46f, w * 0.25f, h * 0.38f, w * 0.23f, h * 0.30f)
            cubicTo(w * 0.30f, h * 0.30f, w * 0.43f, h * 0.38f, w * 0.51f, h * 0.45f)
        }
        drawPath(leaf2Left, color = effectiveColor, style = strokeStyle)

        val leaf2Right = Path().apply {
            moveTo(w * 0.52f, h * 0.45f)
            cubicTo(w * 0.67f, h * 0.40f, w * 0.76f, h * 0.32f, w * 0.78f, h * 0.24f)
            cubicTo(w * 0.71f, h * 0.24f, w * 0.59f, h * 0.32f, w * 0.52f, h * 0.40f)
        }
        drawPath(leaf2Right, color = effectiveColor, style = strokeStyle)

        // Top terminal leaf
        val topLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.20f)
            cubicTo(w * 0.42f, h * 0.12f, w * 0.45f, h * 0.04f, w * 0.50f, 0f)
            cubicTo(w * 0.55f, h * 0.04f, w * 0.58f, h * 0.12f, w * 0.50f, h * 0.20f)
        }
        drawPath(topLeaf, color = effectiveColor, style = strokeStyle)
    }
}
