package com.hkm.stickhub.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.CategoryValidator
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerOrderPolicy
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import kotlinx.coroutines.launch

/**
 * Unified category representation for the management screen.
 * Covers both smart system filters (All, Favorites, Frequent) and database categories.
 */
data class ManageableCategory(
    val id: String,
    val name: String,
    val isSystem: Boolean,
    val isDefaultFolder: Boolean = false,
    val stickerCount: Int = 0,
    val iconRes: Int,
    val subtitle: String,
    val canRename: Boolean,
    val canDelete: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryManagementScreen(
    categories: List<CategoryItem>,
    categoryOrder: List<String> = emptyList(),
    stickers: List<StickerItem>,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (oldName: String, newName: String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onReorderCategories: (List<String>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberStickHubHaptics()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<ManageableCategory?>(null) }
    var categoryToDelete by remember { mutableStateOf<ManageableCategory?>(null) }

    // Drag-and-drop state
    var draggedKey by remember { mutableStateOf<String?>(null) }
    var dragTranslateY by remember { mutableFloatStateOf(0f) }

    fun buildItemsList(): List<ManageableCategory> {
        val totalCount = stickers.size
        val favCount = stickers.count { it.isFavorite }
        val freqCount = stickers.count { it.usageCount > 0 }
        fun getCount(catName: String) = stickers.count { it.category.equals(catName, ignoreCase = true) }

        val systemMap = mapOf(
            "All" to ManageableCategory(
                id = "sys_All",
                name = "All",
                isSystem = true,
                iconRes = LucideR.drawable.lucide_ic_layers,
                subtitle = "$totalCount stickers • Smart filter",
                canRename = false,
                canDelete = false
            ),
            "Favorites" to ManageableCategory(
                id = "sys_Favorites",
                name = "Favorites",
                isSystem = true,
                iconRes = LucideR.drawable.lucide_ic_heart,
                subtitle = "$favCount starred • Smart filter",
                canRename = false,
                canDelete = false
            ),
            "Frequent" to ManageableCategory(
                id = "sys_Frequent",
                name = "Frequent",
                isSystem = true,
                iconRes = LucideR.drawable.lucide_ic_gauge,
                subtitle = "$freqCount frequently used • Smart filter",
                canRename = false,
                canDelete = false
            )
        )

        val dbMap = categories.associateBy { it.name.lowercase() }
        val effectiveOrder = if (categoryOrder.isNotEmpty()) {
            categoryOrder
        } else {
            listOf("All", "Favorites", "Frequent") + categories.sortedBy { it.displayOrder }.map { it.name }
        }

        val seen = mutableSetOf<String>()
        val result = mutableListOf<ManageableCategory>()

        for (name in effectiveOrder) {
            val lower = name.lowercase()
            if (lower in seen) continue
            seen.add(lower)

            val sys = systemMap.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
            if (sys != null) {
                result.add(sys)
            } else {
                val dbCat = dbMap[lower]
                if (dbCat != null) {
                    val isGeneral = dbCat.name.equals("General", ignoreCase = true)
                    result.add(
                        ManageableCategory(
                            id = "cat_${dbCat.id}",
                            name = dbCat.name,
                            isSystem = false,
                            isDefaultFolder = isGeneral,
                            stickerCount = getCount(dbCat.name),
                            iconRes = LucideR.drawable.lucide_ic_folder,
                            subtitle = if (isGeneral) "${getCount(dbCat.name)} stickers • Default folder" else "${getCount(dbCat.name)} stickers",
                            canRename = true,
                            canDelete = true
                        )
                    )
                }
            }
        }

        // Guarantee all system filters exist
        for ((sysName, sysItem) in systemMap) {
            if (sysName.lowercase() !in seen) {
                seen.add(sysName.lowercase())
                result.add(sysItem)
            }
        }

        // Guarantee all DB categories exist
        for (dbCat in categories.sortedBy { it.displayOrder }) {
            if (dbCat.name.lowercase() !in seen) {
                seen.add(dbCat.name.lowercase())
                val isGeneral = dbCat.name.equals("General", ignoreCase = true)
                result.add(
                    ManageableCategory(
                        id = "cat_${dbCat.id}",
                        name = dbCat.name,
                        isSystem = false,
                        isDefaultFolder = isGeneral,
                        stickerCount = getCount(dbCat.name),
                        iconRes = LucideR.drawable.lucide_ic_folder,
                        subtitle = if (isGeneral) "${getCount(dbCat.name)} stickers • Default folder" else "${getCount(dbCat.name)} stickers",
                        canRename = true,
                        canDelete = true
                    )
                )
            }
        }

        return result
    }

    var itemsList by remember(categories, categoryOrder, stickers) {
        mutableStateOf(buildItemsList())
    }

    fun deleteFallbackFor(target: String): String {
        return CategoryItem.pickDeleteFallback(categories, target)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = navBarsBottom + 32.dp
            )
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptics.performTap()
                            onBack()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_arrow_left),
                            contentDescription = "Back to Settings",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Drag or use arrows to reorder all categories and filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            haptics.performTap()
                            showAddDialog = true
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_plus),
                                contentDescription = "Add category",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item(key = "all_section_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ALL CATEGORIES & FILTERS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${itemsList.size} total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            itemsIndexed(itemsList, key = { _, item -> item.id }) { index, item ->
                val isDragged = draggedKey == item.id

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDragged) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                    },
                    tonalElevation = if (isDragged) 8.dp else 0.dp,
                    shadowElevation = if (isDragged) 8.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .animateItem()
                        .zIndex(if (isDragged) 10f else 1f)
                        .graphicsLayer {
                            if (isDragged) {
                                translationY = dragTranslateY
                                scaleX = 1.02f
                                scaleY = 1.02f
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive Drag & Drop grip handle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .pointerInput(item.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggedKey = item.id
                                            dragTranslateY = 0f
                                            haptics.performLongPress()
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragTranslateY += dragAmount.y

                                            val currentKey = draggedKey ?: return@detectDragGestures
                                            val currentIndex = itemsList.indexOfFirst { it.id == currentKey }
                                            if (currentIndex < 0) return@detectDragGestures

                                            val currentItemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull {
                                                it.key == currentKey
                                            } ?: return@detectDragGestures

                                            val draggedCenterY = currentItemInfo.offset + dragTranslateY + currentItemInfo.size / 2f

                                            val targetItemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                                                info.key != currentKey &&
                                                    info.key != "header" && info.key != "all_section_title" &&
                                                    draggedCenterY >= info.offset && draggedCenterY <= info.offset + info.size
                                            }

                                            if (targetItemInfo != null) {
                                                val targetKey = targetItemInfo.key as? String
                                                val targetIndex = itemsList.indexOfFirst { it.id == targetKey }
                                                if (targetIndex >= 0 && targetIndex != currentIndex) {
                                                    dragTranslateY += currentItemInfo.offset - targetItemInfo.offset
                                                    itemsList = StickerOrderPolicy.move(itemsList, currentIndex, targetIndex)
                                                    haptics.performTick()
                                                }
                                            }

                                            val viewportEnd = listState.layoutInfo.viewportEndOffset.toFloat()
                                            val touchY = change.position.y
                                            if (touchY < 120f) {
                                                scope.launch { listState.scrollBy(-24f) }
                                            } else if (touchY > viewportEnd - 120f) {
                                                scope.launch { listState.scrollBy(24f) }
                                            }
                                        },
                                        onDragEnd = {
                                            draggedKey = null
                                            dragTranslateY = 0f
                                            haptics.performTick()
                                            onReorderCategories(itemsList.map { it.name })
                                        },
                                        onDragCancel = {
                                            draggedKey = null
                                            dragTranslateY = 0f
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_grip_vertical),
                                contentDescription = "Drag to reorder ${item.name}",
                                tint = if (isDragged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Category Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (item.isSystem) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(item.iconRes),
                                contentDescription = null,
                                tint = if (item.isSystem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.isSystem) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "FILTER",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                } else if (item.isDefaultFolder) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "DEFAULT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Move Up Button
                        IconButton(
                            onClick = {
                                if (index > 0) {
                                    haptics.performSelection()
                                    val moved = StickerOrderPolicy.move(itemsList, index, index - 1)
                                    itemsList = moved
                                    onReorderCategories(moved.map { it.name })
                                }
                            },
                            enabled = index > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_arrow_up),
                                contentDescription = "Move up",
                                tint = if (index > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Move Down Button
                        IconButton(
                            onClick = {
                                if (index < itemsList.size - 1) {
                                    haptics.performSelection()
                                    val moved = StickerOrderPolicy.move(itemsList, index, index + 1)
                                    itemsList = moved
                                    onReorderCategories(moved.map { it.name })
                                }
                            },
                            enabled = index < itemsList.size - 1,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_arrow_down),
                                contentDescription = "Move down",
                                tint = if (index < itemsList.size - 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Rename Button (only for non-system)
                        if (item.canRename) {
                            IconButton(
                                onClick = {
                                    haptics.performTap()
                                    categoryToRename = item
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_pencil),
                                    contentDescription = "Rename ${item.name}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Delete Button (only for non-system)
                        if (item.canDelete) {
                            IconButton(
                                onClick = {
                                    haptics.performTap()
                                    categoryToDelete = item
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_trash_2),
                                    contentDescription = "Delete ${item.name}",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddDialog) {
        var newCategoryName by rememberSaveable { mutableStateOf("") }
        val validationResult = remember(newCategoryName, categories) {
            CategoryValidator.validate(newCategoryName, categories)
        }
        val isError = validationResult is CategoryValidator.Result.Error && newCategoryName.isNotBlank()
        val errorMessage = (validationResult as? CategoryValidator.Result.Error)?.message

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Create a custom category to organize stickers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { if (it.length <= CategoryValidator.MAX_LENGTH) newCategoryName = it },
                        singleLine = true,
                        isError = isError,
                        label = { Text("Category name") },
                        supportingText = {
                            if (isError && errorMessage != null) {
                                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text(
                                    text = "${newCategoryName.trim().length}/${CategoryValidator.MAX_LENGTH}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (validationResult is CategoryValidator.Result.Valid) {
                                    haptics.performTick()
                                    onAddCategory(newCategoryName.trim())
                                    showAddDialog = false
                                }
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.performTick()
                        onAddCategory(newCategoryName.trim())
                        showAddDialog = false
                    },
                    enabled = validationResult is CategoryValidator.Result.Valid,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Category Dialog
    categoryToRename?.let { targetCat ->
        var updatedName by rememberSaveable(targetCat) { mutableStateOf(targetCat.name) }
        val validationResult = remember(updatedName, categories, targetCat) {
            CategoryValidator.validate(updatedName, categories, currentName = targetCat.name)
        }
        val isError = validationResult is CategoryValidator.Result.Error && updatedName.isNotBlank()
        val errorMessage = (validationResult as? CategoryValidator.Result.Error)?.message
        val hasChanged = updatedName.trim() != targetCat.name

        AlertDialog(
            onDismissRequest = { categoryToRename = null },
            title = {
                Text(
                    text = "Rename category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Stickers in '${targetCat.name}' will be automatically updated to the new name.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = updatedName,
                        onValueChange = { if (it.length <= CategoryValidator.MAX_LENGTH) updatedName = it },
                        singleLine = true,
                        isError = isError,
                        label = { Text("New category name") },
                        supportingText = {
                            if (isError && errorMessage != null) {
                                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text(
                                    text = "${updatedName.trim().length}/${CategoryValidator.MAX_LENGTH}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (validationResult is CategoryValidator.Result.Valid && hasChanged) {
                                    haptics.performTick()
                                    onRenameCategory(targetCat.name, updatedName.trim())
                                    categoryToRename = null
                                }
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.performTick()
                        onRenameCategory(targetCat.name, updatedName.trim())
                        categoryToRename = null
                    },
                    enabled = validationResult is CategoryValidator.Result.Valid && hasChanged,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { categoryToRename = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Category Dialog
    categoryToDelete?.let { targetCat ->
        val affectedCount = targetCat.stickerCount
        val fallbackName = deleteFallbackFor(targetCat.name)
        val customCount = categories.size

        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = {
                Text(
                    text = "Delete '${targetCat.name}'?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (customCount <= 1) {
                            "This is your last custom category. A fresh empty 'General' will be created so your library always has a home."
                        } else if (affectedCount > 0) {
                            "All $affectedCount stickers in '${targetCat.name}' will be safely moved to '$fallbackName'. No sticker images or tags will be deleted."
                        } else {
                            "This category is currently empty. Deleting it will remove it from your categories list."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.performTick()
                        onDeleteCategory(targetCat.name)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { categoryToDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
