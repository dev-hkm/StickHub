package com.hkm.stickhub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.CategoryValidator
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerOrderPolicy
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryManagementScreen(
    categories: List<CategoryItem>,
    stickers: List<StickerItem>,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (oldName: String, newName: String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onReorderCategories: (List<String>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberStickHubHaptics()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<CategoryItem?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryItem?>(null) }

    // Unified list: every category is renamable, reorderable and deletable.
    val allCategories = categories.sortedBy { it.displayOrder }

    fun getCount(catName: String): Int {
        return stickers.count { it.category.equals(catName, ignoreCase = true) }
    }

    /** Where stickers land when [target] is deleted (mirrors repository rule). */
    fun deleteFallbackFor(target: String): String {
        return CategoryItem.pickDeleteFallback(allCategories, target)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = navBarsBottom + 32.dp
            )
        ) {
            // Header item with status bars padding, scrolling together with content
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
                            text = "Organize and prioritize your sticker library",
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

            // All Categories Section (unified: rename, reorder and delete everything)
            item(key = "all_section_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ALL CATEGORIES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${allCategories.size} total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allCategories.isEmpty()) {
                item(key = "empty_custom") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_folder),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No categories yet",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap + at the top to create one",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(allCategories, key = { _, cat -> cat.id }) { index, cat ->
                    val stickerCount = getCount(cat.name)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptics.performLongPress()
                                    categoryToRename = cat
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Drag / Reorder handle
                            IconButton(
                                onClick = {
                                    haptics.performTick()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_grip_vertical),
                                    contentDescription = "Reorder",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_folder),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$stickerCount stickers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Move Up Button
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        haptics.performSelection()
                                        val moved = StickerOrderPolicy.move(allCategories, index, index - 1)
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
                                    if (index < allCategories.size - 1) {
                                        haptics.performSelection()
                                        val moved = StickerOrderPolicy.move(allCategories, index, index + 1)
                                        onReorderCategories(moved.map { it.name })
                                    }
                                },
                                enabled = index < allCategories.size - 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_arrow_down),
                                    contentDescription = "Move down",
                                    tint = if (index < allCategories.size - 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Rename Button
                            IconButton(
                                onClick = {
                                    haptics.performTap()
                                    categoryToRename = cat
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_pencil),
                                    contentDescription = "Rename ${cat.name}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete Button
                            IconButton(
                                onClick = {
                                    haptics.performTap()
                                    categoryToDelete = cat
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_trash_2),
                                    contentDescription = "Delete ${cat.name}",
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
        val affectedCount = getCount(targetCat.name)
        val fallbackName = deleteFallbackFor(targetCat.name)
        val isLastOne = allCategories.size <= 1

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
                        text = if (isLastOne) {
                            "This is your last category. A fresh empty 'General' will be created so your library always has a home."
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
