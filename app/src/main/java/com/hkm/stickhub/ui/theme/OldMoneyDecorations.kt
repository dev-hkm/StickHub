package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hkm.stickhub.R

/**
 * Pinstripe suiting backdrop for the Old Money theme.
 * Faint vertical hairlines in brass — quiet heritage texture.
 * Zero bitmap textures, zero network assets.
 */
fun Modifier.editorialPinstripes(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val lineColor = if (isDark) Color(0xFFC4B773).copy(alpha = 0.07f)
    else Color(0xFF8A7B4A).copy(alpha = 0.10f)

    val spacingPx = 28.dp.toPx()
    val strokePx = 1.dp.toPx()

    var x = spacingPx / 2f
    while (x < size.width) {
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = strokePx
        )
        x += spacingPx
    }
}

private val SealSerif = FontFamily(
    Font(R.font.fraunces_bold, FontWeight.Bold)
)

/**
 * Heritage wax-seal motif: double brass ring with a serif initial.
 * Used in airy spaces (empty states) for the Old Money theme.
 */
@Composable
fun OldMoneySealMotif(
    modifier: Modifier = Modifier,
    ring: Color = Color(0xFF8A7B4A),
    initial: Color = Color(0xFF114A34),
    initialText: String = "S"
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember(initial) {
        TextStyle(
            fontFamily = SealSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 44.sp,
            color = initial
        )
    }
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension / 2f - 2.dp.toPx()
        val innerR = outerR - 7.dp.toPx()

        drawCircle(color = ring, radius = outerR, style = Stroke(width = 2.5.dp.toPx()))
        drawCircle(color = ring.copy(alpha = 0.65f), radius = innerR, style = Stroke(width = 1.dp.toPx()))

        val measured = textMeasurer.measure(text = initialText, style = labelStyle)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                cx - measured.size.width / 2f,
                cy - measured.size.height / 2f
            )
        )
    }
}
