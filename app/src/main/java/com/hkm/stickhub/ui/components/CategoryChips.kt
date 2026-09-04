package com.hkm.stickhub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.theme.StickHubMotion
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class LibraryFilter(
    val key: String,
    val label: String,
    val iconRes: Int? = null,
    val category: CategoryItem? = null
)

/** A compact, animated filter rail rather than a mixture of legacy chip styles. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryChips(
    categories: List<CategoryItem>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onCategoryLongClick: ((CategoryItem) -> Unit)? = null,
    onReorderCustomCategories: ((List<String>) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    modifier: Modifier = Modifier
) {
    val haptics = rememberStickHubHaptics()
    val scope = rememberCoroutineScope()

    // System filters stay pinned; custom categories are freely arrangeable.
    val fixedFilters = listOf(
        LibraryFilter("All", "All"),
        LibraryFilter("Favorites", "Favorites", LucideR.drawable.lucide_ic_heart),
        LibraryFilter("Frequent", "Frequent", LucideR.drawable.lucide_ic_gauge)
    )
    // Local order mirrors the flow; resets only when the flow itself changes.
    var customOrder by remember(categories) {
        mutableStateOf(categories.map { it.name })
    }
    val orderedCustoms = customOrder.mapNotNull { name ->
        categories.find { it.name.equals(name, ignoreCase = true) }
    } + categories.filter { cat -> customOrder.none { it.equals(cat.name, ignoreCase = true) } }
    val filters = fixedFilters + orderedCustoms.map { category ->
        LibraryFilter(category.name, category.name, LucideR.drawable.lucide_ic_folder, category)
    }

    val listState = rememberLazyListState()
    var draggedKey by remember { mutableStateOf<String?>(null) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragTotalX by remember { mutableFloatStateOf(0f) }
    var orderDirty by remember { mutableStateOf(false) }

    fun persistOrderIfNeeded() {
        if (orderDirty) {
            orderDirty = false
            onReorderCustomCategories?.invoke(customOrder.toList())
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.pointerInput(filters) {
            detectDragGesturesAfterLongPress(
                onDragStart = { touch ->
                    // Only custom chips (past the pinned trio) start a drag.
                    val hit = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                        info.index >= fixedFilters.size &&
                            touch.x >= info.offset && touch.x <= info.offset + info.size
                    }
                    if (hit != null) {
                        draggedKey = filters.getOrNull(hit.index)?.key
                        dragOffsetX = 0f
                        dragTotalX = 0f
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val key = draggedKey ?: return@detectDragGesturesAfterLongPress
                    dragOffsetX += dragAmount.x
                    dragTotalX += abs(dragAmount.x)
                    val from = customOrder.indexOfFirst { it == key }
                    if (from < 0) return@detectDragGesturesAfterLongPress
                    // Dragged center in row coordinates → target slot.
                    val draggedCenter = (listState.layoutInfo.visibleItemsInfo
                        .firstOrNull { filters.getOrNull(it.index)?.key == key }
                        ?.let { it.offset + dragOffsetX + it.size / 2f }) ?: return@detectDragGesturesAfterLongPress
                    val targetInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                        info.index >= fixedFilters.size &&
                            draggedCenter >= info.offset && draggedCenter <= info.offset + info.size
                    }
                    val targetFilter = targetInfo?.let { filters.getOrNull(it.index) }
                    val to = targetFilter?.let { tf -> customOrder.indexOfFirst { it == tf.key } } ?: -1
                    if (to >= 0 && to != from) {
                        customOrder = customOrder.toMutableList().also { it.add(to, it.removeAt(from)) }
                        orderDirty = true
                        haptics.performTick()
                    }
                    // Edge auto-scroll while dragging.
                    val viewportEnd = listState.layoutInfo.viewportEndOffset.toFloat()
                    scope.launch {
                        when {
                            change.position.x < 120f -> listState.scrollBy(-28f)
                            change.position.x > viewportEnd - 120f -> listState.scrollBy(28f)
                        }
                    }
                },
                onDragEnd = {
                    val key = draggedKey
                    draggedKey = null
                    dragOffsetX = 0f
                    if (key != null && dragTotalX < 12f) {
                        // Treated as a long-press tap: legacy delete shortcut.
                        val category = orderedCustoms.find { it.name == key }
                        if (category != null && onCategoryLongClick != null) {
                            haptics.performLongPress()
                            onCategoryLongClick(category)
                        }
                    } else if (key != null) {
                        haptics.performTick()
                    }
                    persistOrderIfNeeded()
                },
                onDragCancel = {
                    draggedKey = null
                    dragOffsetX = 0f
                    persistOrderIfNeeded()
                }
            )
        },
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(filters, key = { it.key }) { filter ->
            val isSelected = selectedCategory == filter.key
            val isDragged = draggedKey == filter.key
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                animationSpec = tween(StickHubMotion.DurationShort, easing = StickHubMotion.EasingEmphasizedDecelerate),
                label = "library_filter_container"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(StickHubMotion.DurationShort, easing = StickHubMotion.EasingEmphasizedDecelerate),
                label = "library_filter_content"
            )

            Surface(
                color = containerColor,
                contentColor = contentColor,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(38.dp)
                    .animateItem()
                    .graphicsLayer {
                        if (isDragged) {
                            translationX = dragOffsetX
                            scaleX = 1.06f
                            scaleY = 1.06f
                        }
                    }
                    .zIndex(if (isDragged) 1f else 0f)
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        onClick = {
                            if (!isSelected) {
                                haptics.performTick()
                                onSelectCategory(filter.key)
                            }
                        },
                        onLongClick = {
                            // Long-press without drag keeps the legacy shortcut,
                            // but only when the reorder gesture is disabled.
                            if (onReorderCustomCategories == null) {
                                val category = filter.category
                                if (category != null && onCategoryLongClick != null) {
                                    haptics.performLongPress()
                                    onCategoryLongClick(category)
                                }
                            }
                        }
                    )
            ) {
                Row(
                    // Fixed geometry prevents every tag from shifting when selection changes.
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filter.iconRes?.let { iconRes ->
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                    }
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item(key = "add-category") {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        onClick = {
                            haptics.performTap()
                            onAddCategoryClick()
                        }
                    )
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_plus),
                    contentDescription = "Create category",
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
