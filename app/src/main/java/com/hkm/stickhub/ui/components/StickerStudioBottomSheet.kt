package com.hkm.stickhub.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.theme.StickHubMotion
import com.hkm.stickhub.util.StickerEditorUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerStudioBottomSheet(
    sticker: StickerItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveNew: (Bitmap) -> Unit,
    onOverwrite: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val haptics = rememberStickHubHaptics()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var baseTransformedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var outlineWidthInt by remember { mutableIntStateOf(0) }
    var sliderFloat by remember { mutableFloatStateOf(0f) }

    var captionText by remember { mutableStateOf("") }
    var debouncedCaption by remember { mutableStateOf("") }
    var isCaptionTop by remember { mutableStateOf(false) }

    // Load original bitmap off-main at max dimension 1024 to keep preview fast and bounded
    LaunchedEffect(sticker.filePath) {
        val file = File(sticker.filePath)
        if (file.exists()) {
            withContext(Dispatchers.IO) {
                val opts = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.absolutePath, opts)
                var sample = 1
                while (opts.outWidth / (sample * 2) >= 1024 || opts.outHeight / (sample * 2) >= 1024) {
                    sample *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bmp = BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
                withContext(Dispatchers.Main) {
                    originalBitmap = bmp
                    baseTransformedBitmap = bmp
                    previewBitmap = bmp
                }
            }
        }
    }

    // Debounce caption text input
    LaunchedEffect(captionText) {
        delay(250)
        debouncedCaption = captionText
    }

    // Recompute preview in background whenever transforms, outline, or text change
    LaunchedEffect(baseTransformedBitmap, outlineWidthInt, debouncedCaption, isCaptionTop) {
        val base = baseTransformedBitmap ?: return@LaunchedEffect
        isProcessing = true
        withContext(Dispatchers.Default) {
            var current = base

            if (outlineWidthInt > 0) {
                current = StickerEditorUtil.addDieCutOutline(
                    current,
                    strokeWidth = outlineWidthInt,
                    strokeColor = AndroidColor.WHITE
                )
            }

            if (debouncedCaption.isNotBlank()) {
                current = StickerEditorUtil.addTextCaption(
                    source = current,
                    caption = debouncedCaption,
                    isTop = isCaptionTop
                )
            }

            withContext(Dispatchers.Main) {
                previewBitmap = current
                isProcessing = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sticker Studio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_x),
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Checkered Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CheckerboardBackground(modifier = Modifier.fillMaxSize())

                Crossfade(
                    targetState = previewBitmap,
                    animationSpec = tween(StickHubMotion.DurationShort),
                    label = "preview_crossfade"
                ) { bmp ->
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Edited Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Tool Bar: Flip, Rotate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        val base = baseTransformedBitmap ?: return@FilledTonalButton
                        baseTransformedBitmap = StickerEditorUtil.flipHorizontal(base)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_flip_horizontal),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Flip")
                }

                FilledTonalButton(
                    onClick = {
                        val base = baseTransformedBitmap ?: return@FilledTonalButton
                        baseTransformedBitmap = StickerEditorUtil.rotate90(base)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_rotate_cw),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rotate 90°")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Die-Cut White Outline Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Die-Cut White Border",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${outlineWidthInt}px",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = sliderFloat,
                    onValueChange = { newVal ->
                        sliderFloat = newVal
                        outlineWidthInt = newVal.roundToInt()
                    },
                    onValueChangeFinished = {
                        haptics.performTick()
                    },
                    valueRange = 0f..20f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meme Caption Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Meme Caption",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Caption text") },
                    placeholder = { Text("WHAT IF I TOLD YOU...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (captionText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isCaptionTop,
                            onClick = { isCaptionTop = true },
                            label = { Text("Top") }
                        )
                        FilterChip(
                            selected = !isCaptionTop,
                            onClick = { isCaptionTop = false },
                            label = { Text("Bottom") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val bmp = previewBitmap ?: return@OutlinedButton
                        onOverwrite(bmp)
                        haptics.performConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Overwrite")
                }

                Button(
                    onClick = {
                        val bmp = previewBitmap ?: return@Button
                        onSaveNew(bmp)
                        haptics.performConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_save),
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save as Copy", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
