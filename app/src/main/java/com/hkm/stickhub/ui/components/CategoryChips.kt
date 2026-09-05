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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.library.CategoryDragSession
import com.hkm.stickhub.ui.theme.StickHubMotion
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class LibraryFilter(
    val key: String,
    val label: String,
    val iconRes: Int? = null,
    val category: CategoryItem? = null,
    val isSystem: Boolean = false
)

/** A compact, animated filter rail allowing reordering of all categories and smart filters. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryChips(
    categories: List<CategoryItem>,
    categoryOrder: List<String> = emptyList(),
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onCategoryLongClick: ((CategoryItem) -> Unit)? = null,
    onReorderCategories: ((List<String>) -> Unit)? = null,
    onReorderCustomCategories: ((List<String>) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    modifier: Modifier = Modifier
) {
    val haptics = rememberStickHubHaptics()
    val scope = rememberCoroutineScope()

    val systemFilterMap = mapOf(
        "all" to LibraryFilter("All", "All", isSystem = true),
        "favorites" to LibraryFilter("Favorites", "Favorites", LucideR.drawable.lucide_ic_heart, isSystem = true),
        "frequent" to LibraryFilter("Frequent", "Frequent", LucideR.drawable.lucide_ic_gauge, isSystem = true)
    )

    val allKnownNames = StickerRepository.SYSTEM_CATEGORIES + categories.map { it.name }
    val effectiveOrder = if (categoryOrder.isNotEmpty()) {
        categoryOrder.filter { name -> allKnownNames.any { it.equals(name, ignoreCase = true) } } +
            allKnownNames.filter { name -> categoryOrder.none { it.equals(name, ignoreCase = true) } }
    } else {
        StickerRepository.SYSTEM_CATEGORIES + categories.sortedBy { it.displayOrder }.map { it.name }
    }

    val seen = mutableSetOf<String>()
    val uniqueOrder = effectiveOrder.filter { seen.add(it.lowercase()) }

    val dragSession = remember { CategoryDragSession(uniqueOrder) }
    LaunchedEffect(uniqueOrder) { dragSession.syncExternal(uniqueOrder) }

    val customMap = categories.associateBy { it.name.lowercase() }

    fun resolveFilter(key: String): LibraryFilter {
        val lower = key.lowercase()
        systemFilterMap[lower]?.let { return it }
        val cat = customMap[lower]
        return LibraryFilter(
            key = cat?.name ?: key,
            label = cat?.name ?: key,
            iconRes = LucideR.drawable.lucide_ic_folder,
            category = cat,
            isSystem = false
        )
    }

    val filters = dragSession.order.map { resolveFilter(it) } +
        uniqueOrder.filter { name -> dragSession.order.none { it.equals(name, ignoreCase = true) } }.map { resolveFilter(it) }

    val listState = rememberLazyListState()
    var dragGrabDX by remember { mutableFloatStateOf(0f) }
    var dragTranslateX by remember { mutableFloatStateOf(0f) }
    var dragMoved by remember { mutableStateOf(false) }
    val draggedKey = dragSession.draggedKey

    val latestFilters by rememberUpdatedState(filters)
    val latestReorderCategories by rememberUpdatedState(onReorderCategories)
    val latestReorderCustoms by rememberUpdatedState(onReorderCustomCategories)
    val latestLongClick by rememberUpdatedState(onCategoryLongClick)

    LazyRow(
        state = listState,
        modifier = modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { touch ->
                    val current = latestFilters
                    val hit = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                        touch.x >= info.offset && touch.x <= info.offset + info.size
                    }
                    val key = hit?.let { current.getOrNull(it.index)?.key }
                    if (key != null && dragSession.start(key)) {
                        dragGrabDX = touch.x - (hit?.offset?.toFloat() ?: touch.x)
                        dragTranslateX = 0f
                        dragMoved = false
                        haptics.performLongPress()
                    }
                },
                onDrag = { change, _ ->
                    change.consume()
                    val key = dragSession.draggedKey ?: return@detectDragGesturesAfterLongPress
                    val current = latestFilters
                    val itemInfo = listState.layoutInfo.visibleItemsInfo
                        .firstOrNull { current.getOrNull(it.index)?.key == key }
                    if (itemInfo != null) {
                        dragTranslateX = change.position.x - dragGrabDX - itemInfo.offset
                        if (abs(dragTranslateX) > 4f) dragMoved = true
                        val draggedCenter = itemInfo.offset + dragTranslateX + itemInfo.size / 2f
                        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                            draggedCenter >= info.offset && draggedCenter <= info.offset + info.size
                        }?.let { current.getOrNull(it.index)?.key }
                        if (target != null && dragSession.moveTo(target)) {
                            haptics.performTick()
                        }
                    }
                    val viewportEnd = listState.layoutInfo.viewportEndOffset.toFloat()
                    scope.launch {
                        when {
                            change.position.x < 120f -> listState.scrollBy(-28f)
                            change.position.x > viewportEnd - 120f -> listState.scrollBy(28f)
                        }
                    }
                },
                onDragEnd = {
                    val key = dragSession.draggedKey
                    dragTranslateX = 0f
                    if (key != null && !dragMoved) {
                        val filter = latestFilters.find { it.key == key }
                        val category = filter?.category
                        if (category != null && !category.name.equals("General", ignoreCase = true) && !filter.isSystem) {
                            latestLongClick?.let {
                                haptics.performLongPress()
                                it(category)
                            }
                        }
                    } else if (key != null) {
                        haptics.performTick()
                    }
                    dragSession.finish()?.let { newOrder ->
                        if (latestReorderCategories != null) {
                            latestReorderCategories?.invoke(newOrder)
                        } else if (latestReorderCustoms != null) {
                            val nonSystem = newOrder.filterNot { StickerRepository.SYSTEM_CATEGORIES.any { sys -> sys.equals(it, ignoreCase = true) } }
                            latestReorderCustoms?.invoke(nonSystem)
                        }
                    }
                },
                onDragCancel = {
                    dragSession.cancel()
                    dragTranslateX = 0f
                }
            )
        },
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(filters, key = { it.key }) { filter ->
            val isSelected = selectedCategory.equals(filter.key, ignoreCase = true)
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
                            translationX = dragTranslateX
                            scaleX = 1.06f
                            scaleY = 1.06f
                        }
                    }
                    .zIndex(if (isDragged) 1f else 0f)
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        role = Role.Tab,
                        onClick = {
                            if (!isSelected) {
                                haptics.performTick()
                                onSelectCategory(filter.key)
                            }
                        },
                        onLongClick = {
                            if (onReorderCategories == null && onReorderCustomCategories == null) {
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
