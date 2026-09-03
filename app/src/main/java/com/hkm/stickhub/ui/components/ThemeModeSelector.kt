package com.hkm.stickhub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.StickHubMotion

private data class ThemeModeOption(val mode: AppThemeMode, val label: String, val icon: Int)

/** An animated Material-3-style segmented control for an intentional app-wide state change. */
@Composable
fun ThemeModeSelector(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        ThemeModeOption(AppThemeMode.SYSTEM, "System", LucideR.drawable.lucide_ic_monitor),
        ThemeModeOption(AppThemeMode.LIGHT, "Light", LucideR.drawable.lucide_ic_sun),
        ThemeModeOption(AppThemeMode.DARK, "Dark", LucideR.drawable.lucide_ic_moon)
    )
    val selectedIndex = options.indexOfFirst { it.mode == themeMode }.coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
            .padding(3.dp)
    ) {
        val segmentWidth = (maxWidth - 6.dp) / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = tween(StickHubMotion.DurationMedium, easing = StickHubMotion.EasingEmphasizedDecelerate),
            label = "theme_mode_indicator_offset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        )

        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEach { option ->
                val selected = option.mode == themeMode
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(StickHubMotion.DurationShort, easing = StickHubMotion.EasingEmphasizedDecelerate),
                    label = "theme_mode_content_color"
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1f else 0.9f,
                    animationSpec = tween(StickHubMotion.DurationShort, easing = StickHubMotion.EasingEmphasizedDecelerate),
                    label = "theme_mode_icon_scale"
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(13.dp))
                        .clickable { onThemeModeChange(option.mode) }
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(option.icon),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                    Text(
                        text = option.label,
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            }
        }
    }
}
