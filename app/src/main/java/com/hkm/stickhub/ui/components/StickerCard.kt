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
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.ui.theme.StickHubMotion
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickerCard(
    sticker: StickerItem,
    onClick: (StickerItem) -> Unit,
    onLongClick: ((StickerItem) -> Unit)? = null,
    onReorderStart: ((StickerItem) -> Unit)? = null,
    onReorderDrag: ((Offset) -> Unit)? = null,
    onReorderEnd: (() -> Unit)? = null,
    onReorderCancel: (() -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    showCopiedBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var dragStarted by remember(sticker.id) { mutableStateOf(false) }

    // Drag ownership lives on the exact sticker being held. Keeping this out of the
    // LazyVerticalGrid avoids a parent detector racing this card's tap-to-copy handler.
    val reorderGestureModifier = if (!isSelectionMode && onReorderStart != null && onReorderDrag != null) {
        Modifier.pointerInput(sticker.id) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    dragStarted = true
                    onReorderStart(sticker)
                },
                onDrag = { change, dragAmount ->
                    if (dragStarted) {
                        change.consume()
                        onReorderDrag(dragAmount)
                    }
                },
                onDragEnd = {
                    if (dragStarted) onReorderEnd?.invoke()
                    dragStarted = false
                },
                onDragCancel = {
                    if (dragStarted) onReorderCancel?.invoke()
                    dragStarted = false
                }
            )
        }
    } else {
        Modifier
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(StickHubMotion.DurationShort),
        label = "sticker_card_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    isPressed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier
            )
            .then(reorderGestureModifier)
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
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(sticker.filePath))
                // A whole filtered grid should never restart dozens of image fades together.
                .crossfade(false)
                .build(),
            contentDescription = sticker.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Favorite Indicator (Lucide Heart)
        if (sticker.isFavorite && !isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
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
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Selection Checkbox Badge (Lucide Check)
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
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

        // Instant Copied Pulse Badge (Lucide CheckCheck)
        AnimatedVisibility(
            visible = showCopiedBadge && !isSelectionMode,
            enter = fadeIn(tween(StickHubMotion.DurationShort)) + scaleIn(),
            exit = fadeOut(tween(StickHubMotion.DurationShort)) + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
