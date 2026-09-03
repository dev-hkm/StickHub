package com.hkm.stickhub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.ui.theme.StickHubMotion
import java.io.File

/**
 * High-density 4-column compact sticker card with maximized artwork.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactStickerCard(
    sticker: StickerItem,
    onClick: (StickerItem) -> Unit,
    onLongClick: ((StickerItem) -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    showCopiedBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(StickHubMotion.DurationShort),
        label = "compact_card_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    isPressed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                }
            )
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            )
            .then(
                if (isSelectionMode) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick(sticker) },
                        onLongClick = { onLongClick?.invoke(sticker) }
                    )
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick(sticker) }
                    )
                }
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(sticker.filePath))
                .crossfade(false)
                .build(),
            contentDescription = sticker.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Favorite Badge
        if (sticker.isFavorite && !isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_heart),
                    contentDescription = "Favorite",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        // Selection Badge
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
                    .then(
                        if (!isSelected) Modifier.border(1.2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_check),
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Copied Feedback Badge
        AnimatedVisibility(
            visible = showCopiedBadge && !isSelectionMode,
            enter = fadeIn(tween(StickHubMotion.DurationShort)) + scaleIn(),
            exit = fadeOut(tween(StickHubMotion.DurationShort)) + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_check_check),
                    contentDescription = "Copied",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 2-column large sticker card with prominent preview and metadata.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LargeStickerCard(
    sticker: StickerItem,
    onClick: (StickerItem) -> Unit,
    onLongClick: ((StickerItem) -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    showCopiedBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = tween(StickHubMotion.DurationShort),
        label = "large_card_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            )
            .then(
                if (isSelectionMode) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick(sticker) },
                        onLongClick = { onLongClick?.invoke(sticker) }
                    )
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick(sticker) }
                    )
                }
            ),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        },
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Preview container with fixed aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(sticker.filePath))
                        .crossfade(false)
                        .build(),
                    contentDescription = sticker.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite Badge
                if (sticker.isFavorite && !isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_heart),
                            contentDescription = "Favorite",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Selection Checkbox Badge
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                            .then(
                                if (!isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_check),
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Copied Pulse Badge
                androidx.compose.animation.AnimatedVisibility(
                    visible = showCopiedBadge && !isSelectionMode,
                    enter = fadeIn(tween(StickHubMotion.DurationShort)) + scaleIn(),
                    exit = fadeOut(tween(StickHubMotion.DurationShort)) + scaleOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_check_check),
                            contentDescription = "Copied",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Category
            Text(
                text = sticker.title.ifEmpty { "Sticker #${sticker.id}" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sticker.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (sticker.usageCount > 0) {
                    Text(
                        text = "${sticker.usageCount}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Full-width list row layout for stickers with rich metadata.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickerListItem(
    sticker: StickerItem,
    onClick: (StickerItem) -> Unit,
    onLongClick: ((StickerItem) -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    showCopiedBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(StickHubMotion.DurationShort),
        label = "list_item_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            )
            .then(
                if (isSelectionMode) {
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick(sticker) },
                        onLongClick = { onLongClick?.invoke(sticker) }
                    )
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick(sticker) }
                    )
                }
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox (if in selection mode)
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .then(
                            if (!isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_check),
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Thumbnail
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(sticker.filePath))
                        .crossfade(false)
                        .build(),
                    contentDescription = sticker.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )

                // Copied Pulse Badge
                androidx.compose.animation.AnimatedVisibility(
                    visible = showCopiedBadge && !isSelectionMode,
                    enter = fadeIn(tween(StickHubMotion.DurationShort)) + scaleIn(),
                    exit = fadeOut(tween(StickHubMotion.DurationShort)) + scaleOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_check_check),
                            contentDescription = "Copied",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Metadata column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sticker.title.ifEmpty { "Sticker #${sticker.id}" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category capsule
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = sticker.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (sticker.usageCount > 0) {
                        Text(
                            text = "Copied ${sticker.usageCount} time${if (sticker.usageCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    } else if (sticker.tags.isNotBlank()) {
                        Text(
                            text = sticker.tags,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Favorite status
            if (sticker.isFavorite) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_heart),
                    contentDescription = "Favorite",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp)
                )
            }
        }
    }
}
