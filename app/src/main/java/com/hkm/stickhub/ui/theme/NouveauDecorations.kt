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

/**
 * Whiplash-vine backdrop: slow S-curve tendrils climbing from the corners —
 * Mucha energy at a whisper so lists stay readable.
 */
fun Modifier.whiplashVines(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val vineColor = if (isDark) Color(0xFFCFB53B).copy(alpha = 0.10f)
    else Color(0xFF008080).copy(alpha = 0.08f)

    val w = size.width
    val h = size.height
    fun vine(x0: Float, y0: Float, x1: Float, y1: Float, bulge: Float): Path = Path().apply {
        moveTo(x0, y0)
        cubicTo(x0 + bulge, y0 + (y1 - y0) * 0.3f, x1 - bulge, y0 + (y1 - y0) * 0.7f, x1, y1)
    }
    // Bottom-left climber
    drawPath(path = vine(0f, h * 0.98f, w * 0.30f, h * 0.55f, w * 0.22f), color = vineColor, style = Stroke(width = 1.5.dp.toPx()))
    drawPath(path = vine(0f, h * 0.90f, w * 0.22f, h * 0.60f, w * 0.16f), color = vineColor.copy(alpha = 0.6f), style = Stroke(width = 1.dp.toPx()))
    // Top-right draper
    drawPath(path = vine(w, h * 0.02f, w * 0.70f, h * 0.40f, -w * 0.22f), color = vineColor, style = Stroke(width = 1.5.dp.toPx()))
    drawPath(path = vine(w, h * 0.10f, w * 0.78f, h * 0.36f, -w * 0.16f), color = vineColor.copy(alpha = 0.6f), style = Stroke(width = 1.dp.toPx()))
    // Tendril dots
    drawCircle(color = vineColor, radius = 2.dp.toPx(), center = Offset(w * 0.30f, h * 0.55f))
    drawCircle(color = vineColor, radius = 2.dp.toPx(), center = Offset(w * 0.70f, h * 0.40f))
}

/**
 * Nouveau bloom motif: a stylized whiplash iris in gold and teal.
 * Used in airy spaces (empty states).
 */
@Composable
fun NouveauBloomMotif(
    modifier: Modifier = Modifier,
    gold: Color = Color(0xFFCFB53B),
    teal: Color = Color(0xFF008080),
    wine: Color = Color(0xFF800020)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val bloomCy = h * 0.40f

        // 1. Three upright petals (quadratic blades)
        fun petal(tipX: Float, tipY: Float, widthFrac: Float): Path = Path().apply {
            moveTo(cx, bloomCy + h * 0.10f)
            quadraticTo(cx + (tipX - cx) * 0.4f - w * widthFrac, bloomCy - h * 0.05f, tipX, tipY)
            quadraticTo(cx + (tipX - cx) * 0.4f + w * widthFrac, bloomCy - h * 0.05f, cx, bloomCy + h * 0.10f)
            close()
        }
        drawPath(path = petal(cx, h * 0.08f, 0.10f), color = teal)
        drawPath(path = petal(cx - w * 0.20f, h * 0.20f, 0.09f), color = teal.copy(alpha = 0.8f))
        drawPath(path = petal(cx + w * 0.20f, h * 0.20f, 0.09f), color = teal.copy(alpha = 0.8f))

        // 2. Golden heart
        drawCircle(color = gold, radius = 8.dp.toPx(), center = Offset(cx, bloomCy + h * 0.04f))
        drawCircle(color = wine, radius = 3.5.dp.toPx(), center = Offset(cx, bloomCy + h * 0.04f))

        // 3. Drooping side tendrils
        listOf(-1f, 1f).forEach { side ->
            val curl = Path().apply {
                moveTo(cx + side * w * 0.06f, bloomCy + h * 0.12f)
                cubicTo(
                    cx + side * w * 0.30f, bloomCy + h * 0.16f,
                    cx + side * w * 0.34f, bloomCy + h * 0.34f,
                    cx + side * w * 0.22f, bloomCy + h * 0.36f
                )
            }
            drawPath(path = curl, color = gold, style = Stroke(width = 2.dp.toPx()))
            drawCircle(color = gold, radius = 2.5.dp.toPx(), center = Offset(cx + side * w * 0.22f, bloomCy + h * 0.36f))
        }

        // 4. Stem + ground flourish
        drawLine(
            color = teal,
            start = Offset(cx, bloomCy + h * 0.12f),
            end = Offset(cx, h * 0.90f),
            strokeWidth = 2.5.dp.toPx()
        )
        val ground = Path().apply {
            moveTo(cx - w * 0.24f, h * 0.90f)
            quadraticTo(cx, h * 0.86f, cx + w * 0.24f, h * 0.90f)
        }
        drawPath(path = ground, color = gold, style = Stroke(width = 2.dp.toPx()))
    }
}
