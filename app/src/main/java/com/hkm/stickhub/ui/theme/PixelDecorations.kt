package com.hkm.stickhub.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * CRT scanline backdrop: faint horizontal beam lines over the base.
 * Pure nostalgia, near-zero cost.
 */
fun Modifier.crtScanlines(
    enabled: Boolean,
    isDark: Boolean
): Modifier = if (!enabled) this else this.drawBehind {
    val lineColor = if (isDark) Color.Black.copy(alpha = 0.22f)
    else Color(0xFF0B5A30).copy(alpha = 0.06f)

    val lineH = 1.dp.toPx()
    val gap = 3.dp.toPx()
    var y = 0f
    while (y < size.height) {
        drawRect(color = lineColor, topLeft = Offset(0f, y), size = Size(size.width, lineH))
        y += lineH + gap
    }
}

/**
 * Pixel-invader motif drawn from an 11x8 sprite bitmap — every pixel earns
 * its place. Used in airy spaces (empty states).
 */
@Composable
fun PixelInvaderMotif(
    modifier: Modifier = Modifier,
    pixel: Color = Color(0xFF2CFF56),
    accent: Color = Color(0xFFFFB200)
) {
    Canvas(modifier = modifier) {
        // Classic invader sprite rows (X = filled)
        val sprite = listOf(
            "..X.....X..",
            "...X...X...",
            "..XXXXXXX..",
            ".XX.XXX.XX.",
            "XXXXXXXXXXX",
            "X.XXXXXXX.X",
            "X.X.....X.X",
            "...XX.XX..."
        )
        val rows = sprite.size
        val cols = sprite[0].length
        val px = size.minDimension / 11f
        val gridW = cols * px
        val gridH = rows * px
        val ox = (size.width - gridW) / 2f
        val oy = (size.height - gridH) / 2f

        sprite.forEachIndexed { r, row ->
            row.forEachIndexed { c, ch ->
                if (ch == 'X') {
                    val isEye = (r == 3 && (c == 2 || c == 3 || c == 7 || c == 8))
                    drawRect(
                        color = if (isEye) accent else pixel,
                        topLeft = Offset(ox + c * px, oy + r * px),
                        size = Size(px, px)
                    )
                }
            }
        }
        // Ground shadow strip
        drawRect(
            color = pixel.copy(alpha = 0.35f),
            topLeft = Offset(ox, oy + gridH + 6.dp.toPx()),
            size = Size(gridW, 3.dp.toPx())
        )
    }
}
