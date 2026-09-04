package com.hkm.stickhub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.ui.theme.StickHubMotion
import com.hkm.stickhub.ui.theme.UbuntuFontFamily
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.library.StickerLibraryViewMode
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isOverlayRunning: Boolean,
    onToggleOverlay: () -> Unit,
    onOpenSettings: () -> Unit,
    currentViewMode: StickerLibraryViewMode = StickerLibraryViewMode.STANDARD_GRID,
    onOpenLayoutPicker: () -> Unit = {},
    resultCount: Int? = null,
    applyDefaultPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val haptics = rememberStickHubHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }

    // When the keyboard is dismissed while the field still holds focus, drop
    // the hover/focus glow 1s later so the bar settles back to idle.
    // (Restarting the effect on key change cancels a pending clear if the
    // keyboard comes back within the second.)
    val imeVisible = WindowInsets.isImeVisible
    var wasImeVisible by remember { mutableStateOf(imeVisible) }
    LaunchedEffect(imeVisible, isFocused) {
        if (wasImeVisible && !imeVisible && isFocused) {
            delay(1000)
            focusManager.clearFocus()
        }
        wasImeVisible = imeVisible
    }

    val elevation by animateDpAsState(
        targetValue = if (isFocused || query.isNotEmpty()) 4.dp else 1.dp,
        animationSpec = tween(StickHubMotion.DurationShort),
        label = "search_elevation"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
            query.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        },
        animationSpec = tween(StickHubMotion.DurationShort),
        label = "search_border"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isFocused || query.isNotEmpty()) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(StickHubMotion.DurationShort),
        label = "search_icon_color"
    )

    val boxModifier = if (applyDefaultPadding) {
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    } else {
        modifier.fillMaxWidth()
    }

    Box(modifier = boxModifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(elevation, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .border(1.2.dp, borderColor, RoundedCornerShape(26.dp))
                .clickable {
                    focusRequester.requestFocus()
                },
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            tonalElevation = elevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lucide Search Icon
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_search),
                    contentDescription = "Search",
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Text field & Placeholder
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search stickers...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                fontFamily = UbuntuFontFamily,
                                fontSize = 15.sp
                            ),
                            maxLines = 1
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        interactionSource = interactionSource,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = UbuntuFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }

                // Result count badge
                if (query.isNotEmpty() && resultCount != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = resultCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                // Clear Query Button (Lucide X)
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(tween(StickHubMotion.DurationShort)) + scaleIn(),
                    exit = fadeOut(tween(StickHubMotion.DurationShort)) + scaleOut()
                ) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_x),
                            contentDescription = "Clear",
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Overlay Quick Toggle (Lucide Layers3)
                AnimatedSearchActionButton(
                    onClick = onToggleOverlay,
                    contentDescription = "Floating Overlay",
                    active = isOverlayRunning
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOverlayRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_layers_2),
                            contentDescription = null,
                            tint = if (isOverlayRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Layout Switcher Button
                AnimatedSearchActionButton(
                    onClick = {
                        haptics.performNavigationTap()
                        onOpenLayoutPicker()
                    },
                    contentDescription = "Switch layout: ${currentViewMode.title}"
                ) {
                    Icon(
                        painter = painterResource(currentViewMode.iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Settings Button (Lucide Settings2)
                AnimatedSearchActionButton(
                    onClick = {
                        haptics.performNavigationTap()
                        onOpenSettings()
                    },
                    contentDescription = "Settings"
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_settings_2),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedSearchActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    active: Boolean = false,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(StickHubMotion.DurationMicro, easing = StickHubMotion.EasingEmphasizedDecelerate),
        label = "search_action_scale"
    )
    val containerColor by animateColorAsState(
        targetValue = when {
            isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            active -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            else -> Color.Transparent
        },
        animationSpec = tween(StickHubMotion.DurationMicro),
        label = "search_action_container"
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
