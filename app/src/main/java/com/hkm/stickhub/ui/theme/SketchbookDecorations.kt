package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Procedural notebook paper decoration for the Sketchbook theme.
 * Renders subtle horizontal ruled lines and a delicate vertical margin rule using drawBehind;
 * zero bitmap textures, zero network assets.
 */
fun Modifier.notebookPaperLines(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val lineColor = if (isDark) Color(0xFF2C394F).copy(alpha = 0.40f) else Color(0xFFD6D0C4).copy(alpha = 0.55f)
    val marginLineColor = if (isDark) Color(0xFF3F5475).copy(alpha = 0.35f) else Color(0xFFB84942).copy(alpha = 0.18f)

    val lineSpacingPx = 28.dp.toPx()
    val marginOffsetPx = 36.dp.toPx()
    val strokeWidthPx = 1.dp.toPx()

    // 1. Delicate vertical notebook margin rule on the left edge
    if (size.width > marginOffsetPx * 1.5f) {
        drawLine(
            color = marginLineColor,
            start = Offset(marginOffsetPx, 0f),
            end = Offset(marginOffsetPx, size.height),
            strokeWidth = strokeWidthPx * 1.2f
        )
    }

    // 2. Horizontal ruled notebook lines
    var currentY = lineSpacingPx
    while (currentY < size.height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, currentY),
            end = Offset(size.width, currentY),
            strokeWidth = strokeWidthPx
        )
        currentY += lineSpacingPx
    }
}

/**
 * Hand-drawn doodle illustration rendered cleanly via vector paths.
 * Used exclusively in airy spaces (empty states, settings preview).
 */
@Composable
fun SketchDoodleMotif(
    modifier: Modifier = Modifier,
    tint: Color = SketchbookColors.LightNavyInkPrimary,
    accentTint: Color = SketchbookColors.LightMutedRedAccent,
    highlightTint: Color = SketchbookColors.LightHighlighterYellow
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val mainStroke = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // 1. Soft highlighter wash backdrop behind doodle
        drawRoundRect(
            color = highlightTint.copy(alpha = 0.35f),
            topLeft = Offset(w * 0.15f, h * 0.20f),
            size = androidx.compose.ui.geometry.Size(w * 0.70f, h * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
        )

        // 2. Hand-drawn notebook box / frame
        val boxPath = Path().apply {
            moveTo(w * 0.20f, h * 0.22f)
            lineTo(w * 0.82f, h * 0.20f)
            lineTo(w * 0.80f, h * 0.68f)
            lineTo(w * 0.18f, h * 0.70f)
            close()
        }
        drawPath(boxPath, color = tint.copy(alpha = 0.85f), style = mainStroke)

        // 3. Hand-drawn wavy note lines inside frame
        val line1 = Path().apply {
            moveTo(w * 0.26f, h * 0.34f)
            cubicTo(w * 0.40f, h * 0.33f, w * 0.55f, h * 0.35f, w * 0.72f, h * 0.33f)
        }
        drawPath(line1, color = tint.copy(alpha = 0.60f), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

        val line2 = Path().apply {
            moveTo(w * 0.26f, h * 0.46f)
            cubicTo(w * 0.38f, h * 0.48f, w * 0.50f, h * 0.45f, w * 0.65f, h * 0.47f)
        }
        drawPath(line2, color = tint.copy(alpha = 0.60f), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

        val line3 = Path().apply {
            moveTo(w * 0.26f, h * 0.58f)
            cubicTo(w * 0.36f, h * 0.57f, w * 0.46f, h * 0.59f, w * 0.54f, h * 0.57f)
        }
        drawPath(line3, color = tint.copy(alpha = 0.60f), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

        // 4. Playful hand-drawn curly arrow at bottom right
        val arrowPath = Path().apply {
            moveTo(w * 0.68f, h * 0.76f)
            cubicTo(w * 0.76f, h * 0.82f, w * 0.84f, h * 0.84f, w * 0.82f, h * 0.94f)
        }
        drawPath(arrowPath, color = accentTint.copy(alpha = 0.85f), style = mainStroke)

        // Arrow head
        val arrowHead = Path().apply {
            moveTo(w * 0.74f, h * 0.90f)
            lineTo(w * 0.82f, h * 0.94f)
            lineTo(w * 0.86f, h * 0.86f)
        }
        drawPath(arrowHead, color = accentTint.copy(alpha = 0.85f), style = mainStroke)

        // 5. Hand-drawn spark/asterisk in top-left corner
        val starCenterX = w * 0.12f
        val starCenterY = h * 0.16f
        val starSize = 7.dp.toPx()
        drawLine(
            color = accentTint.copy(alpha = 0.75f),
            start = Offset(starCenterX, starCenterY - starSize),
            end = Offset(starCenterX, starCenterY + starSize),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = accentTint.copy(alpha = 0.75f),
            start = Offset(starCenterX - starSize * 0.86f, starCenterY - starSize * 0.5f),
            end = Offset(starCenterX + starSize * 0.86f, starCenterY + starSize * 0.5f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = accentTint.copy(alpha = 0.75f),
            start = Offset(starCenterX - starSize * 0.86f, starCenterY + starSize * 0.5f),
            end = Offset(starCenterX + starSize * 0.86f, starCenterY - starSize * 0.5f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
