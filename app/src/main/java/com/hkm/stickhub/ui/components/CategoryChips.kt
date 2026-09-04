package com.hkm.stickhub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.theme.StickHubMotion

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
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    modifier: Modifier = Modifier
) {
    val haptics = rememberStickHubHaptics()
    val filters = listOf(
        LibraryFilter("All", "All"),
        LibraryFilter("Favorites", "Favorites", LucideR.drawable.lucide_ic_heart),
        LibraryFilter("Frequent", "Frequent", LucideR.drawable.lucide_ic_gauge)
    ) + categories.map { category ->
        LibraryFilter(category.name, category.name, LucideR.drawable.lucide_ic_folder, category)
    }

    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(filters, key = { it.key }) { filter ->
            val isSelected = selectedCategory == filter.key
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
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        onClick = {
                            if (!isSelected) {
                                haptics.performTick()
                                onSelectCategory(filter.key)
                            }
                        },
                        onLongClick = {
                            val category = filter.category
                            if (category != null && !category.isDefault && onCategoryLongClick != null) {
                                haptics.performLongPress()
                                onCategoryLongClick(category)
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
