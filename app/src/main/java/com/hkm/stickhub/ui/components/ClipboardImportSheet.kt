package com.hkm.stickhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.hkm.stickhub.util.StagedClipboardItem
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics

/**
 * Review-and-pick sheet for multi-image clipboard imports: the user sees
 * every image on the clipboard, toggles selection, then imports once.
 */
@Composable
fun ClipboardImportSheet(
    stagedItems: List<StagedClipboardItem>,
    skippedCount: Int = 0,
    isStaging: Boolean = false,
    stageProgress: Pair<Int, Int> = 0 to 0,
    isImporting: Boolean = false,
    onImportSelected: (List<StagedClipboardItem.Ready>) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = rememberStickHubHaptics()
    val readyItems = stagedItems.filterIsInstance<StagedClipboardItem.Ready>()
    val failedCount = stagedItems.count { it is StagedClipboardItem.Failed }
    var selectedKeys by remember(stagedItems) {
        mutableStateOf(readyItems.map { it.candidate.stableKey }.toSet())
    }

    AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        title = {
            Text(
                text = "Import from clipboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                val status = when {
                    isStaging -> "Preparing ${stageProgress.first}/${stageProgress.second} copied images…"
                    isImporting -> "Saving selected stickers…"
                    readyItems.isNotEmpty() -> "${readyItems.size} images ready — tap to select, then import once."
                    else -> "No copied images could be prepared."
                }
                Text(text = status, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (skippedCount > 0 || failedCount > 0) {
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
                if (isStaging) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else if (readyItems.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().height(300.dp),
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptics.performTick()
                    onImportSelected(readyItems.filter { it.candidate.stableKey in selectedKeys })
                },
                enabled = !isStaging && !isImporting && selectedKeys.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isImporting) "Importing…" else "Import ${selectedKeys.size}")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        haptics.performTick()
                        selectedKeys = if (selectedKeys.size == readyItems.size) emptySet()
                        else readyItems.map { it.candidate.stableKey }.toSet()
                    },
                    enabled = !isStaging && !isImporting && readyItems.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (selectedKeys.size == readyItems.size) "Clear" else "Select all")
                }
                TextButton(
                    onClick = onDismiss,
                    enabled = !isImporting,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    )
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
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
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
                    .padding(4.dp)
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
