package com.hkm.stickhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.util.StagedClipboardItem

/**
 * Review-and-pick sheet for multi-image clipboard imports: the user sees
 * every image on the clipboard, toggles selection, then imports once.
 * Responsive Material 3 ModalBottomSheet adhering to max 85% viewport height.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardImportSheet(
    stagedItems: List<StagedClipboardItem>,
    skippedCount: Int = 0,
    isStaging: Boolean = false,
    stageProgress: Pair<Int, Int> = 0 to 0,
    isImporting: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onImportSelected: (List<StagedClipboardItem.Ready>) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = rememberStickHubHaptics()
    val readyItems = stagedItems.filterIsInstance<StagedClipboardItem.Ready>()
    val failedCount = stagedItems.count { it is StagedClipboardItem.Failed }
    var selectedKeys by remember(stagedItems) {
        mutableStateOf(readyItems.map { it.candidate.stableKey }.toSet())
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isImporting) onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxHeightCap = maxHeight * 0.85f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeightCap)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Import from clipboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val subtitle = when {
                            isStaging -> "Preparing ${stageProgress.first}/${stageProgress.second} copied images…"
                            isImporting -> "Saving selected stickers…"
                            readyItems.isNotEmpty() -> "${selectedKeys.size} of ${readyItems.size} selected"
                            else -> "No copied images could be prepared."
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isImporting
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_x),
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (skippedCount > 0 || failedCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (skippedCount > 0) append("$skippedCount unsupported. ")
                            if (failedCount > 0) append("$failedCount couldn't be read.")
                        }.trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content area
                if (isStaging) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 160.dp, max = 240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (readyItems.isNotEmpty()) {
                    // Action chip row: Select All / Deselect All
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tap image to toggle selection",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TextButton(
                            onClick = {
                                haptics.performTick()
                                selectedKeys = if (selectedKeys.size == readyItems.size) {
                                    emptySet()
                                } else {
                                    readyItems.map { it.candidate.stableKey }.toSet()
                                }
                            },
                            enabled = !isImporting
                        ) {
                            Text(
                                if (selectedKeys.size == readyItems.size) "Deselect all"
                                else "Select all"
                            )
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 88.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(readyItems, key = { it.candidate.stableKey }) { item ->
                            ClipboardPickThumb(
                                item = item,
                                selected = item.candidate.stableKey in selectedKeys,
                                onToggle = {
                                    if (!isImporting) {
                                        haptics.performTick()
                                        val key = item.candidate.stableKey
                                        selectedKeys = if (key in selectedKeys) selectedKeys - key else selectedKeys + key
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isImporting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            haptics.performConfirm()
                            onImportSelected(readyItems.filter { it.candidate.stableKey in selectedKeys })
                        },
                        enabled = !isStaging && !isImporting && selectedKeys.isNotEmpty(),
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importing…")
                        } else {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_save),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import (${selectedKeys.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipboardPickThumb(
    item: StagedClipboardItem.Ready,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .clickable(
                role = androidx.compose.ui.semantics.Role.Checkbox,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.file)
                .crossfade(true)
                .build(),
            contentDescription = "Clipboard image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .matchParentSize()
                .padding(8.dp)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_check),
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(11.dp)
                    )
            )
        }
    }
}
