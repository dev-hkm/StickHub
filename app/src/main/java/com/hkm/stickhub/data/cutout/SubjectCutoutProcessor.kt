package com.hkm.stickhub.data.cutout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Rect as ComposeRect
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.Subject
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.hkm.stickhub.util.BitmapDecodeUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min

/**
 * Owns the complete on-device subject-cutout lifecycle.
 *
 * `installModules()` only acknowledges a request. Inference starts exclusively after the model
 * is already present or the install listener reports STATE_COMPLETED.
 */
class SubjectCutoutProcessor(private val context: Context) {

    private val _state = MutableStateFlow<CutoutState>(CutoutState.Idle)
    val state: StateFlow<CutoutState> = _state.asStateFlow()

    /**
     * Single gate for request identity: reset and every state publication
     * share it, so a stale callback can never paint into a newer session.
     */
    private val requestGate = CutoutRequestGate()
    private val activeSegmenterLock = Any()
    private var activeSegmenter: ActiveSegmenter? = null

    private data class ActiveSegmenter(
        val requestId: Long,
        val segmenter: SubjectSegmenter
    )

    fun reset() {
        requestGate.begin { _state.value = CutoutState.Idle }
        closeActiveSegmenter()
    }

    /**
     * Decode is deliberately owned here, so a bad or expired picker URI ends in Failed rather
     * than leaving the sheet at Idle/"Preparing".
     */
    suspend fun processUri(uri: Uri) = withContext(Dispatchers.Default) {
        val requestId = requestGate.begin { _state.value = CutoutState.Decoding }

        val decoded = BitmapDecodeUtil.decodeBoundedBitmap(context, uri, maxDimension = MAX_INPUT_DIMENSION)
        if (!requestGate.isCurrent(requestId)) return@withContext

        when (val decision = CutoutWorkflowPolicy.afterDecode(decoded != null)) {
            is CutoutStartDecision.DecodeFailed ->
                requestGate.publish(requestId) { _state.value = CutoutState.Failed(decision.message) }
            CutoutStartDecision.StartProcessing -> {
                val image = decoded ?: return@withContext
                processDecodedImage(image.bitmap, image.isAlreadyTransparent, requestId)
            }
        }
    }

    /** Kept for callers that already own a decoded bitmap. */
    suspend fun processImage(sourceBitmap: Bitmap, isAlreadyTransparent: Boolean) = withContext(Dispatchers.Default) {
        val requestId = requestGate.begin { _state.value = CutoutState.Decoding }
        processDecodedImage(sourceBitmap, isAlreadyTransparent, requestId)
    }

    private suspend fun CoroutineScope.processDecodedImage(
        sourceBitmap: Bitmap,
        isAlreadyTransparent: Boolean,
        requestId: Long
    ) {
        if (!requestGate.isCurrent(requestId)) return

        if (isAlreadyTransparent) {
            requestGate.publish(requestId) { _state.value = CutoutState.TransparentDetected(sourceBitmap) }
            return
        }

        val gmsAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (gmsAvailable != ConnectionResult.SUCCESS) {
            requestGate.publish(requestId) {
                _state.value = CutoutState.GooglePlayServicesUnavailable(
                    "Google Play services is required for on-device subject cutout."
                )
            }
            return
        }

        val inputPlan = try {
            CutoutModelInputPlan.create(sourceBitmap.width, sourceBitmap.height)
        } catch (_: Exception) {
            null
        }
        val workingBitmap = scaleForSubjectModel(sourceBitmap, inputPlan)
        // Candidate display boxes are mapped back through this plan so they line up
        // with the original image no matter how the ML input was scaled/padded.
        val mappingPlan = inputPlan ?: run {
            try {
                CutoutModelInputPlan.create(workingBitmap.width, workingBitmap.height)
            } catch (_: Exception) {
                null
            }
        }
        val options = SubjectSegmenterOptions.Builder()
            .enableMultipleSubjects(
                SubjectSegmenterOptions.SubjectResultOptions.Builder()
                    .enableConfidenceMask()
                    .enableSubjectBitmap()
                    .build()
            )
            .build()
        val segmenter = SubjectSegmentation.getClient(options)
        registerActiveSegmenter(requestId, segmenter)

        try {
            when (val readiness = ensureSubjectModelInstalled(segmenter, requestId)) {
                is CutoutInstallDecision.Failed -> {
                    requestGate.publish(requestId) { _state.value = CutoutState.Failed(readiness.message) }
                    return
                }
                CutoutInstallDecision.WaitForCompletion -> {
                    requestGate.publish(requestId) {
                        _state.value = CutoutState.Failed(
                            "The on-device cutter model is still unavailable. Please retry."
                        )
                    }
                    return
                }
                CutoutInstallDecision.ProceedToAnalysis -> Unit
            }

            if (!requestGate.publish(requestId) { _state.value = CutoutState.Analyzing }) return

            val result = withTimeoutOrNull(INFERENCE_TIMEOUT_MS) {
                segmenter.process(InputImage.fromBitmap(workingBitmap, 0)).awaitTask()
            }
            if (result == null) {
                requestGate.publish(requestId) {
                    _state.value = CutoutState.Failed(
                        "Subject detection took too long. Please try this photo again."
                    )
                }
                return
            }

            if (!requestGate.isCurrent(requestId)) return
            val subjects = result.subjects
            if (subjects.isNullOrEmpty()) {
                requestGate.publish(requestId) { _state.value = CutoutState.NoSubjectFound(sourceBitmap) }
                return
            }

            val candidates = mutableListOf<CutoutCandidate>()
            for ((index, subject) in subjects.withIndex()) {
                ensureActive()
                extractCutoutCandidate(
                    index = index,
                    subject = subject,
                    sourceBitmap = workingBitmap,
                    srcWidth = workingBitmap.width,
                    srcHeight = workingBitmap.height,
                    mappingPlan = mappingPlan,
                    originalWidth = sourceBitmap.width,
                    originalHeight = sourceBitmap.height
                )?.let { candidates.add(it) }
            }

            // Display bitmaps live in the emitted state; the sheet owns them from here
            // (disposal on new generation / unmount) and the processor must not recycle them.
            val finalState = if (candidates.isEmpty()) {
                CutoutState.NoSubjectFound(sourceBitmap)
            } else {
                CutoutState.CandidatesReady(sourceBitmap, candidates)
            }
            requestGate.publish(requestId) { _state.value = finalState }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e(TAG, "Subject cutout failed", error)
            requestGate.publish(requestId) {
                _state.value = CutoutState.Failed(
                    error.localizedMessage ?: "Subject detection failed. Please try again."
                )
            }
        } finally {
            closeSegmenterIfActive(segmenter)
            if (workingBitmap !== sourceBitmap && !workingBitmap.isRecycled) {
                workingBitmap.recycle()
            }
        }
    }

    private suspend fun ensureSubjectModelInstalled(
        segmenter: SubjectSegmenter,
        requestId: Long
    ): CutoutInstallDecision {
        if (!requestGate.isCurrent(requestId)) return CutoutInstallDecision.Failed("Request was replaced.")

        val installClient = ModuleInstall.getClient(context)
        requestGate.publish(requestId) { _state.value = CutoutState.CheckingModel }
        val availability = withTimeoutOrNull(MODEL_CHECK_TIMEOUT_MS) {
            installClient.areModulesAvailable(segmenter).awaitTask()
        } ?: return CutoutInstallDecision.Failed(
            "Couldn't check the on-device cutter model. Please retry."
        )

        if (availability.areModulesAvailable()) {
            return CutoutInstallDecision.ProceedToAnalysis
        }

        if (!requestGate.publish(requestId) { _state.value = CutoutState.DownloadingModel() }) {
            return CutoutInstallDecision.Failed("Request was replaced.")
        }
        val installDecision = withTimeoutOrNull(MODEL_INSTALL_TIMEOUT_MS) {
            awaitModelInstallCompletion(installClient, segmenter, requestId)
        } ?: CutoutInstallDecision.Failed(
            "The model download timed out. Check your connection and retry."
        )

        if (installDecision !is CutoutInstallDecision.ProceedToAnalysis || !requestGate.isCurrent(requestId)) {
            return installDecision
        }

        val installed = withTimeoutOrNull(MODEL_CHECK_TIMEOUT_MS) {
            installClient.areModulesAvailable(segmenter).awaitTask().areModulesAvailable()
        } ?: false

        return if (installed) {
            CutoutInstallDecision.ProceedToAnalysis
        } else {
            CutoutInstallDecision.Failed(
                "The on-device cutter model did not become available. Please retry."
            )
        }
    }

    /**
     * Bridges the callback-based ModuleInstall API. A request acknowledgement is intentionally
     * not treated as completion; the listener controls the terminal transition.
     */
    private suspend fun awaitModelInstallCompletion(
        installClient: ModuleInstallClient,
        segmenter: SubjectSegmenter,
        requestId: Long
    ): CutoutInstallDecision = suspendCancellableCoroutine { continuation ->
        val completed = AtomicBoolean(false)
        lateinit var listener: InstallStatusListener

        fun finish(decision: CutoutInstallDecision) {
            if (!completed.compareAndSet(false, true)) return
            try {
                installClient.unregisterListener(listener)
            } catch (_: Exception) {
                // The listener may not be registered if Play services rejects the request.
            }
            if (continuation.isActive) {
                continuation.resume(decision)
            }
        }

        listener = InstallStatusListener { update ->
            if (!requestGate.isCurrent(requestId)) {
                finish(CutoutInstallDecision.Failed("Request was replaced."))
                return@InstallStatusListener
            }

            val status = update.toCutoutInstallStatus()
            when (val decision = CutoutWorkflowPolicy.afterInstallStatus(status)) {
                CutoutInstallDecision.WaitForCompletion -> updateDownloadProgress(update, status, requestId)
                CutoutInstallDecision.ProceedToAnalysis,
                is CutoutInstallDecision.Failed -> finish(decision)
            }
        }

        val request = ModuleInstallRequest.newBuilder()
            .addApi(segmenter)
            .setListener(listener)
            .build()

        installClient.installModules(request)
            .addOnSuccessListener { response ->
                if (!requestGate.isCurrent(requestId)) {
                    finish(CutoutInstallDecision.Failed("Request was replaced."))
                } else {
                    when (
                        CutoutWorkflowPolicy.afterInstallRequestAcknowledged(
                            response.areModulesAlreadyInstalled()
                        )
                    ) {
                        CutoutInstallDecision.ProceedToAnalysis -> finish(CutoutInstallDecision.ProceedToAnalysis)
                        CutoutInstallDecision.WaitForCompletion -> Unit
                        is CutoutInstallDecision.Failed -> Unit
                    }
                }
            }
            .addOnFailureListener { error ->
                finish(
                    CutoutInstallDecision.Failed(
                        error.localizedMessage ?: "Couldn't start the on-device model download."
                    )
                )
            }
            .addOnCanceledListener {
                finish(CutoutInstallDecision.Failed("The on-device model download was canceled."))
            }

        continuation.invokeOnCancellation {
            if (completed.compareAndSet(false, true)) {
                try {
                    installClient.unregisterListener(listener)
                } catch (_: Exception) {
                    // Best-effort cleanup only.
                }
            }
        }
    }

    private fun updateDownloadProgress(
        update: ModuleInstallStatusUpdate,
        status: CutoutInstallStatus,
        requestId: Long
    ) {
        if (!requestGate.isCurrent(requestId)) return

        val progress = update.progressInfo
        val percent = if (progress != null && progress.totalBytesToDownload > 0) {
            ((progress.bytesDownloaded * 100) / progress.totalBytesToDownload)
                .toInt()
                .coerceIn(0, 100)
        } else {
            0
        }

        val progressState = when (status) {
            CutoutInstallStatus.Pending -> CutoutState.DownloadingModel(
                progressPercent = 0,
                statusText = "Waiting for the on-device model..."
            )
            CutoutInstallStatus.Downloading -> CutoutState.DownloadingModel(
                progressPercent = percent,
                statusText = "Downloading on-device model..."
            )
            CutoutInstallStatus.DownloadPaused -> CutoutState.DownloadingModel(
                progressPercent = percent,
                statusText = "Model download paused. Waiting for a connection..."
            )
            CutoutInstallStatus.Installing -> CutoutState.DownloadingModel(
                progressPercent = maxOf(percent, 95),
                statusText = "Installing on-device model..."
            )
            else -> CutoutState.DownloadingModel()
        }
        requestGate.publish(requestId) { _state.value = progressState }
    }

    private fun ModuleInstallStatusUpdate.toCutoutInstallStatus(): CutoutInstallStatus {
        return when (installState) {
            ModuleInstallStatusUpdate.InstallState.STATE_PENDING -> CutoutInstallStatus.Pending
            ModuleInstallStatusUpdate.InstallState.STATE_DOWNLOADING -> CutoutInstallStatus.Downloading
            ModuleInstallStatusUpdate.InstallState.STATE_DOWNLOAD_PAUSED -> CutoutInstallStatus.DownloadPaused
            ModuleInstallStatusUpdate.InstallState.STATE_INSTALLING -> CutoutInstallStatus.Installing
            ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED -> CutoutInstallStatus.Completed
            ModuleInstallStatusUpdate.InstallState.STATE_FAILED -> CutoutInstallStatus.Failed
            ModuleInstallStatusUpdate.InstallState.STATE_CANCELED -> CutoutInstallStatus.Canceled
            else -> CutoutInstallStatus.Unknown
        }
    }

    /**
     * Scales/pads through [CutoutModelInputPlan] so the ML input matches the
     * geometry the box overlay maps back through — a fraction in working space
     * is no longer a fraction in the original image once padding exists.
     */
    private fun scaleForSubjectModel(sourceBitmap: Bitmap, plan: CutoutModelInputPlan?): Bitmap {
        if (plan == null) return sourceBitmap
        if (plan.left == 0 && plan.top == 0 &&
            plan.contentWidth == sourceBitmap.width &&
            plan.contentHeight == sourceBitmap.height
        ) {
            return sourceBitmap
        }
        return try {
            val canvas = Bitmap.createBitmap(plan.width, plan.height, Bitmap.Config.ARGB_8888)
            android.graphics.Canvas(canvas).drawBitmap(
                sourceBitmap,
                null,
                android.graphics.Rect(
                    plan.left,
                    plan.top,
                    plan.left + plan.contentWidth,
                    plan.top + plan.contentHeight
                ),
                null
            )
            canvas
        } catch (_: OutOfMemoryError) {
            sourceBitmap
        } catch (_: Exception) {
            sourceBitmap
        }
    }

    private fun registerActiveSegmenter(requestId: Long, segmenter: SubjectSegmenter) {
        val previous = synchronized(activeSegmenterLock) {
            val current = activeSegmenter
            activeSegmenter = ActiveSegmenter(requestId, segmenter)
            current
        }
        previous?.segmenter?.closeSafely()
    }

    private fun closeActiveSegmenter() {
        val segmenter = synchronized(activeSegmenterLock) {
            activeSegmenter?.segmenter.also { activeSegmenter = null }
        }
        segmenter?.closeSafely()
    }

    private fun closeSegmenterIfActive(segmenter: SubjectSegmenter) {
        synchronized(activeSegmenterLock) {
            if (activeSegmenter?.segmenter === segmenter) {
                activeSegmenter = null
            }
        }
        segmenter.closeSafely()
    }

    private fun SubjectSegmenter.closeSafely() {
        try {
            close()
        } catch (_: Exception) {
            // ML Kit close is best-effort during cancellation/reselection.
        }
    }

    private suspend fun CoroutineScope.extractCutoutCandidate(
        index: Int,
        subject: Subject,
        sourceBitmap: Bitmap,
        srcWidth: Int,
        srcHeight: Int,
        mappingPlan: CutoutModelInputPlan?,
        originalWidth: Int,
        originalHeight: Int
    ): CutoutCandidate? {
        val startX = subject.startX
        val startY = subject.startY
        val width = subject.width
        val height = subject.height
        if (width <= MIN_SUBJECT_DIMENSION || height <= MIN_SUBJECT_DIMENSION) return null

        val confidenceMaskBuffer: FloatBuffer = subject.confidenceMask ?: return null
        confidenceMaskBuffer.rewind()

        // ML coordinates live in padded-canvas space; map them back to original
        // pixels so boxes line up with the photo shown in the sheet.
        fun toOriginalX(canvasX: Float): Float =
            (mappingPlan?.normalizedX(canvasX) ?: (canvasX / srcWidth)).coerceIn(0f, 1f)
        fun toOriginalY(canvasY: Float): Float =
            (mappingPlan?.normalizedY(canvasY) ?: (canvasY / srcHeight)).coerceIn(0f, 1f)
        val normalizedBounds = ComposeRect(
            toOriginalX(startX.toFloat()),
            toOriginalY(startY.toFloat()),
            toOriginalX((startX + width).toFloat()),
            toOriginalY((startY + height).toFloat())
        )

        val cropLeft = max(0, startX - OUTPUT_PADDING)
        val cropTop = max(0, startY - OUTPUT_PADDING)
        val cropRight = min(srcWidth, startX + width + OUTPUT_PADDING)
        val cropBottom = min(srcHeight, startY + height + OUTPUT_PADDING)
        val cropW = cropRight - cropLeft
        val cropH = cropBottom - cropTop
        if (cropW <= 0 || cropH <= 0) return null

        val maskFloats = FloatArray(width * height)
        confidenceMaskBuffer.get(maskFloats)
        val pixels = IntArray(cropW * cropH)

        for (cy in 0 until cropH) {
            if ((cy and 63) == 0) ensureActive()
            val srcY = cropTop + cy
            for (cx in 0 until cropW) {
                val srcX = cropLeft + cx
                val maskX = srcX - startX
                val maskY = srcY - startY
                val alphaFloat = if (maskX in 0 until width && maskY in 0 until height) {
                    val confidence = maskFloats[maskY * width + maskX]
                    when {
                        confidence < ALPHA_LOW_THRESHOLD -> 0f
                        confidence > ALPHA_HIGH_THRESHOLD -> 1f
                        else -> (confidence - ALPHA_LOW_THRESHOLD) /
                            (ALPHA_HIGH_THRESHOLD - ALPHA_LOW_THRESHOLD)
                    }
                } else {
                    0f
                }

                pixels[cy * cropW + cx] = if (alphaFloat > 0f) {
                    val srcPixel = sourceBitmap.getPixel(srcX, srcY)
                    Color.argb(
                        (alphaFloat * 255f).toInt().coerceIn(0, 255),
                        Color.red(srcPixel),
                        Color.green(srcPixel),
                        Color.blue(srcPixel)
                    )
                } else {
                    Color.TRANSPARENT
                }
            }
        }

        // Prefer ML Kit's own masked bitmap when available: it is generated
        // from the same subject result and carries the model's refined edge
        // alpha. The original-pixels + confidence-mask path remains the
        // defensive fallback for older Play services builds.
        val croppedCutout = try {
            val modelBitmap = try { subject.bitmap } catch (_: Exception) { null }
            if (modelBitmap != null &&
                modelBitmap.width == width &&
                modelBitmap.height == height &&
                modelBitmap.hasAlpha()
            ) {
                modelBitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888).apply {
                    setPixels(pixels, 0, cropW, 0, 0, cropW, cropH)
                }
            }
        } catch (_: Exception) {
            Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, cropW, 0, 0, cropW, cropH)
            }
        }
        val normalizedCutout = try {
            StickerCanvasNormalizer.normalize(croppedCutout)
        } finally {
            croppedCutout.recycle()
        }

        val outlineSegments = MaskContourExtractor.extract(
            mask = maskFloats,
            width = width,
            height = height,
            startX = startX,
            startY = startY,
            toNormalizedX = ::toOriginalX,
            toNormalizedY = ::toOriginalY
        )

        return CutoutCandidate(
            id = index,
            bounds = Rect(
                (toOriginalX(startX.toFloat()) * originalWidth).toInt(),
                (toOriginalY(startY.toFloat()) * originalHeight).toInt(),
                (toOriginalX((startX + width).toFloat()) * originalWidth).toInt(),
                (toOriginalY((startY + height).toFloat()) * originalHeight).toInt()
            ),
            normalizedBounds = normalizedBounds,
            cutoutBitmap = normalizedCutout,
            maskWidth = width,
            maskHeight = height,
            confidenceMask = maskFloats,
            outlineSegments = outlineSegments
        )
    }

    private companion object {
        const val TAG = "SubjectCutout"
        const val MAX_INPUT_DIMENSION = 2048
        const val MIN_MODEL_DIMENSION = 512
        const val MIN_SUBJECT_DIMENSION = 10
        const val OUTPUT_PADDING = 6
        const val ALPHA_LOW_THRESHOLD = 0.15f
        const val ALPHA_HIGH_THRESHOLD = 0.85f
        const val MODEL_CHECK_TIMEOUT_MS = 10_000L
        const val MODEL_INSTALL_TIMEOUT_MS = 90_000L
        const val INFERENCE_TIMEOUT_MS = 30_000L
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { value ->
        if (continuation.isActive) continuation.resume(value)
    }
    addOnFailureListener { error ->
        if (continuation.isActive) continuation.resumeWithException(error)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}
