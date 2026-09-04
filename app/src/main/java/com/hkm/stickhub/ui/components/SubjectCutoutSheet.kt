package com.hkm.stickhub.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.cutout.CutoutCandidate
import com.hkm.stickhub.data.cutout.CutoutSaveSession
import com.hkm.stickhub.data.cutout.CutoutSaveState
import com.hkm.stickhub.data.cutout.CutoutState
import com.hkm.stickhub.data.cutout.SubjectCutoutProcessor
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.theme.StickHubMotion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectCutoutSheet(
    imageUri: Uri,
    sheetState: SheetState,
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSaveSticker: suspend (bitmap: Bitmap, title: String, category: String, tags: String) -> Boolean,
    onChangeImage: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberStickHubHaptics()

    val processor = remember { SubjectCutoutProcessor(context) }
    val cutoutState by processor.state.collectAsState()
    val saveSession = remember { CutoutSaveSession() }
    val saveState by saveSession.state.collectAsState()
    val isSaving = saveState == CutoutSaveState.Saving
    val readyState = cutoutState as? CutoutState.CandidatesReady

    var selectedCandidate by remember(imageUri) { mutableStateOf<CutoutCandidate?>(null) }
    var transparentBitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    val selectedCutoutBitmap = selectedCandidate?.cutoutBitmap ?: transparentBitmap

    // Metadata inputs
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var tags by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    // A new CandidatesReady object is a new generation (including retry of the same
    // image): adopt its first subject so the form can never save a bitmap from a
    // superseded generation.
    LaunchedEffect(readyState) {
        transparentBitmap = null
        selectedCandidate = readyState?.candidates?.firstOrNull()
    }

    DisposableEffect(imageUri) {
        onDispose {
            // Recycle before reset: reset() parks the state at Idle, which owns nothing.
            processor.state.value.recycleOwnedBitmaps()
            processor.reset()
        }
    }

    LaunchedEffect(imageUri) {
        // Reset first so a stale generation can never paint into the new image.
        processor.reset()
        selectedCandidate = null
        transparentBitmap = null
        title = "Sticker ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}"
        processor.processUri(imageUri)
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
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
                    text = if (selectedCutoutBitmap != null) "Sticker Details" else "Create Sticker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss, enabled = !isSaving) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_x),
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Area with clear phase transitions
            AnimatedContent(
                // The output form is a session state, not another ML phase. Keeping its key to
                // a Boolean prevents late inference emissions from replacing or dismissing it.
                targetState = selectedCutoutBitmap != null,
                transitionSpec = {
                    (fadeIn(tween(StickHubMotion.DurationShort)) togetherWith
                            fadeOut(tween(StickHubMotion.DurationShort)))
                },
                label = "cutout_content"
            ) { hasSelectedCutout ->
                if (hasSelectedCutout && selectedCutoutBitmap != null) {
                    // Phase: Sticker metadata/save
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CheckerboardBackground(modifier = Modifier.fillMaxSize())

                            Image(
                                bitmap = selectedCutoutBitmap!!.asImageBitmap(),
                                contentDescription = "Cutout Sticker",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Metadata Fields
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            enabled = !isSaving,
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                enabled = !isSaving,
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategory = cat.name
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            enabled = !isSaving,
                            label = { Text("Tags (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedCandidate = null
                                    transparentBitmap = null
                                },
                                enabled = !isSaving,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Back")
                            }

                            if (saveState == CutoutSaveState.Failed) {
                                Text(
                                    text = "Couldn't save this sticker. Check storage and try again.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    val bmp = selectedCutoutBitmap
                                    if (bmp == null || bmp.isRecycled || isSaving) {
                                        haptics.performReject()
                                        return@Button
                                    }
                                    scope.launch {
                                        // Serialized by the session (double-tap safe) and
                                        // resolvable once; the sheet closes on success only.
                                        val saved = saveSession.save {
                                            onSaveSticker(bmp, title, selectedCategory, tags)
                                        }
                                        if (saved) onDismiss() else haptics.performReject()
                                    }
                                },
                                enabled = selectedCutoutBitmap != null && !isSaving,
                                modifier = Modifier.weight(2f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(LucideR.drawable.lucide_ic_save),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isSaving) "Saving…" else "Save to Hub",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    when (val state = cutoutState) {
                        // Phase: Decoding
                        CutoutState.Decoding -> {
                            AnalyzingSkeletonView(statusText = "Decoding image...")
                        }
                        // Phase: Checking model
                        CutoutState.Idle, CutoutState.CheckingModel -> {
                            AnalyzingSkeletonView(statusText = "Checking AI cutout model...")
                        }
                        // Phase: Waiting/downloading/paused/installing model
                        is CutoutState.DownloadingModel -> {
                            AnalyzingSkeletonView(
                                statusText = state.statusText,
                                progressPercent = state.progressPercent
                            )
                        }
                        // Phase: Finding subjects
                        CutoutState.Analyzing -> {
                            AnalyzingSkeletonView(statusText = "Finding subjects...")
                        }
                        is CutoutState.TransparentDetected -> {
                            LaunchedEffect(state.bitmap) {
                                selectedCandidate = null
                                transparentBitmap = state.bitmap
                            }
                        }
                        // Phase: Candidate selection
                        is CutoutState.CandidatesReady -> {
                            CandidatesSelectionView(
                                sourceBitmap = state.sourceBitmap,
                                candidates = state.candidates,
                                selectedCandidate = selectedCandidate,
                                selectionEnabled = !isSaving,
                                onSelectCandidate = { candidate ->
                                    haptics.performTick()
                                    selectedCandidate = candidate
                                },
                                onWrongTap = {
                                    haptics.performReject()
                                }
                            )
                        }
                        // Phase: No subject
                        is CutoutState.NoSubjectFound -> {
                            FallbackStateView(
                                iconRes = LucideR.drawable.lucide_ic_image,
                                message = "No distinct subject found",
                                subtitle = "Try picking a photo with a clearly defined person, pet, or object in focus.",
                                actionText = "Choose another photo",
                                actionIcon = LucideR.drawable.lucide_ic_image,
                                onAction = onChangeImage
                            )
                        }
                        // Phase: Google Play Services needed
                        is CutoutState.GooglePlayServicesUnavailable -> {
                            FallbackStateView(
                                iconRes = LucideR.drawable.lucide_ic_info,
                                message = "Google Play Services needed",
                                subtitle = state.message,
                                actionText = "Retry",
                                actionIcon = LucideR.drawable.lucide_ic_refresh_cw,
                                onAction = {
                                    scope.launch {
                                        processor.processUri(imageUri)
                                    }
                                },
                                secondaryActionText = "Choose another photo",
                                secondaryActionIcon = LucideR.drawable.lucide_ic_image,
                                onSecondaryAction = onChangeImage
                            )
                        }
                        // Phase: Failed + Retry
                        is CutoutState.Failed -> {
                            FallbackStateView(
                                iconRes = LucideR.drawable.lucide_ic_info,
                                message = "Unable to process photo",
                                subtitle = state.reason,
                                actionText = "Retry",
                                actionIcon = LucideR.drawable.lucide_ic_refresh_cw,
                                onAction = {
                                    scope.launch {
                                        processor.processUri(imageUri)
                                    }
                                },
                                secondaryActionText = "Choose another photo",
                                secondaryActionIcon = LucideR.drawable.lucide_ic_image,
                                onSecondaryAction = onChangeImage
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyzingSkeletonView(
    statusText: String,
    progressPercent: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 2.5.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (progressPercent in 1..99) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CandidatesSelectionView(
    sourceBitmap: Bitmap,
    candidates: List<CutoutCandidate>,
    selectedCandidate: CutoutCandidate?,
    selectionEnabled: Boolean,
    onSelectCandidate: (CutoutCandidate) -> Unit,
    onWrongTap: () -> Unit
) {
    var isPressedOnCandidate by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressedOnCandidate) StickHubMotion.CandidatePressScale else 1f,
        animationSpec = tween(
            durationMillis = StickHubMotion.DurationMicro,
            easing = StickHubMotion.EasingEmphasizedDecelerate
        ),
        label = "candidate_press_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Clear, elegant hint pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_scissors),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Tap or hold a subject to isolate as sticker",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (candidates.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                candidates.forEachIndexed { index, candidate ->
                    AssistChip(
                        onClick = { if (selectionEnabled) onSelectCandidate(candidate) },
                        enabled = selectionEnabled,
                        label = { Text("Subject ${index + 1}") },
                        leadingIcon = if (candidate == selectedCandidate) {
                            {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .scale(pressScale)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            val containerWidth = constraints.maxWidth.toFloat()
            val containerHeight = constraints.maxHeight.toFloat()

            val imgW = sourceBitmap.width.toFloat()
            val imgH = sourceBitmap.height.toFloat()

            val scale = min(containerWidth / imgW, containerHeight / imgH)
            val renderW = imgW * scale
            val renderH = imgH * scale
            val offsetX = (containerWidth - renderW) / 2f
            val offsetY = (containerHeight - renderH) / 2f

            // Image and interactive overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candidates, renderW, renderH, offsetX, offsetY) {
                        detectTapGestures(
                            onPress = { touchOffset ->
                                val normX = (touchOffset.x - offsetX) / renderW
                                val normY = (touchOffset.y - offsetY) / renderH
                                val isCandidate = (normX in 0f..1f && normY in 0f..1f) &&
                                        candidates.any { it.containsNormalizedPoint(normX, normY) }
                                if (isCandidate) {
                                    isPressedOnCandidate = true
                                    tryAwaitRelease()
                                    isPressedOnCandidate = false
                                }
                            },
                            onLongPress = { touchOffset ->
                                if (!selectionEnabled) return@detectTapGestures
                                val normX = (touchOffset.x - offsetX) / renderW
                                val normY = (touchOffset.y - offsetY) / renderH
                                if (normX in 0f..1f && normY in 0f..1f) {
                                    val matched = candidates.find { it.containsNormalizedPoint(normX, normY) }
                                    if (matched != null) {
                                        onSelectCandidate(matched)
                                    } else {
                                        onWrongTap()
                                    }
                                } else {
                                    onWrongTap()
                                }
                            },
                            onTap = { touchOffset ->
                                if (!selectionEnabled) return@detectTapGestures
                                val normX = (touchOffset.x - offsetX) / renderW
                                val normY = (touchOffset.y - offsetY) / renderH
                                if (normX in 0f..1f && normY in 0f..1f) {
                                    val matched = candidates.find { it.containsNormalizedPoint(normX, normY) }
                                    if (matched != null) {
                                        onSelectCandidate(matched)
                                    } else {
                                        onWrongTap()
                                    }
                                } else {
                                    onWrongTap()
                                }
                            }
                        )
                    }
            ) {
                // Background source image
                Image(
                    bitmap = sourceBitmap.asImageBitmap(),
                    contentDescription = "Source Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Refined subtle shimmer outline over candidates
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (candidate in candidates) {
                        val nb = candidate.normalizedBounds
                        val left = offsetX + nb.left * renderW
                        val top = offsetY + nb.top * renderH
                        val width = nb.width * renderW
                        val height = nb.height * renderH

                        // Draw subtle rounded outline
                        drawRoundRect(
                            color = Color.White.copy(alpha = shimmerAlpha),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FallbackStateView(
    iconRes: Int,
    message: String,
    subtitle: String,
    actionText: String,
    actionIcon: Int? = null,
    onAction: () -> Unit,
    secondaryActionText: String? = null,
    secondaryActionIcon: Int? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAction,
            shape = RoundedCornerShape(14.dp)
        ) {
            if (actionIcon != null) {
                Icon(
                    painter = painterResource(actionIcon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(actionText, fontWeight = FontWeight.SemiBold)
        }
        if (secondaryActionText != null && onSecondaryAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSecondaryAction,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (secondaryActionIcon != null) {
                    Icon(
                        painter = painterResource(secondaryActionIcon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(secondaryActionText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Recycles every display bitmap owned by a terminal cutout state. Guarded against
 * double-recycle. All decoded bitmaps here are processor-owned and fresh per request
 * (processImage has no production callers), so no caller-owned pixels are touched. */
private fun CutoutState.recycleOwnedBitmaps() {
    when (this) {
        is CutoutState.CandidatesReady -> {
            sourceBitmap.recycleSafely()
            candidates.forEach { it.cutoutBitmap?.recycleSafely() }
        }
        is CutoutState.TransparentDetected -> bitmap.recycleSafely()
        is CutoutState.NoSubjectFound -> sourceBitmap.recycleSafely()
        else -> Unit
    }
}

private fun Bitmap.recycleSafely() {
    try {
        if (!isRecycled) recycle()
    } catch (_: Exception) {
        // A frame already in flight may still reference the pixels; they become
        // unreachable after this regardless.
    }
}

@Composable
fun CheckerboardBackground(
    modifier: Modifier = Modifier,
    squareSizeDp: Float = 10f,
    lightColor: Color = Color(0xFFF4F4F4),
    darkColor: Color = Color(0xFFE2E2E2)
) {
    Canvas(modifier = modifier) {
        val squarePx = squareSizeDp * density
        val cols = ceil(size.width / squarePx).toInt()
        val rows = ceil(size.height / squarePx).toInt()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val color = if ((r + c) % 2 == 0) lightColor else darkColor
                drawRect(
                    color = color,
                    topLeft = Offset(c * squarePx, r * squarePx),
                    size = Size(squarePx, squarePx)
                )
            }
        }
    }
}
