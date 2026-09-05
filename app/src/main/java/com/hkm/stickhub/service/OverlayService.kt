package com.hkm.stickhub.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.MainActivity
import com.hkm.stickhub.R
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.ui.haptics.StickHubHaptics
import com.hkm.stickhub.ui.theme.AppVisualTheme
import com.hkm.stickhub.ui.theme.OverlayPalette
import com.hkm.stickhub.ui.theme.ThemePaletteResolver
import com.hkm.stickhub.ui.theme.ThemePreferences
import com.hkm.stickhub.util.ClipboardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.res.Configuration
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

class OverlayService : Service() {

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, error ->
            // Overlay work is optional UI. A stale database snapshot, invalid
            // legacy preference, or OEM WindowManager exception must never
            // crash the host process and remove the floating bubble.
            android.util.Log.e("StickHubOverlay", "Overlay update failed", error)
        }
    )
    private lateinit var windowManager: WindowManager
    private lateinit var repository: StickerRepository

    private var bubbleView: View? = null
    private var isPanelOpen = false

    private var bubbleIcon: ImageView? = null
    private var bubbleBg: GradientDrawable? = null
    private var panelBg: GradientDrawable? = null
    private var panelHeaderView: LinearLayout? = null
    private var panelTitleView: TextView? = null
    private var panelSearchView: EditText? = null
    private var searchBg: GradientDrawable? = null
    private var categoryScrollView: HorizontalScrollView? = null
    private var closeBtnView: ImageView? = null
    private var closeOverlayAttached = false
    private var closeOverlaySizePx = 0
    private lateinit var closeOverlayParams: WindowManager.LayoutParams
    private var closeBtnBg: GradientDrawable? = null
    private var resizeBtnView: ImageView? = null
    private var resizeHandleBg: GradientDrawable? = null
    private var emptyStateTextView: TextView? = null
    private var panelSurfaceView: View? = null
    private var chromeContainer: LinearLayout? = null
    private var stickerRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var stickerAdapter: OverlayStickerAdapter? = null

    private fun effectiveSurfaceOpacity(): Float {
        val surface = OverlayPreferences.popupSurfaceOpacity(this)
        return appearanceState.opacity(
            "surface",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(surface),
            nowMs()
        )
    }

    private fun effectiveChromeOpacity(): Float {
        val chrome = OverlayPreferences.popupChromeOpacity(this)
        return appearanceState.opacity(
            "chrome",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(chrome),
            nowMs()
        )
    }

    private fun effectiveStickersOpacity(): Float {
        val stickers = OverlayPreferences.popupStickersOpacity(this)
        return appearanceState.opacity(
            "stickers",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(stickers),
            nowMs()
        )
    }

    private fun effectiveCloseOpacity(): Float {
        val close = OverlayPreferences.popupCloseOpacity(this)
        return appearanceState.opacity(
            "close",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(close),
            nowMs()
        )
    }

    private fun effectiveResizeOpacity(): Float {
        val resize = OverlayPreferences.popupResizeOpacity(this)
        return appearanceState.opacity(
            "resize",
            effectiveMasterOpacity() * OverlayOpacityPolicy.clamp(resize),
            nowMs()
        )
    }

    private fun effectiveBubbleOpacity(): Float {
        return appearanceState.opacity(
            "bubble",
            OverlayOpacityPolicy.clamp(OverlayPreferences.bubbleOpacity(this)),
            nowMs()
        )
    }

    private fun effectiveMasterOpacity(): Float {
        return appearanceState.opacity(
            "master",
            OverlayOpacityPolicy.clamp(OverlayPreferences.popupMasterOpacity(this)),
            nowMs()
        )
    }

    private fun nowMs(): Long = try {
        android.os.SystemClock.uptimeMillis()
    } catch (_: Exception) {
        0L
    }

    private fun currentPalette(): OverlayPalette {
        return ThemePaletteResolver.resolveOverlayPalette(
            context = this,
            visualTheme = ThemePreferences.getVisualTheme(this),
            isDark = isDarkMode()
        )
    }
    private var panelGeneration = 0L

    // Explicit named references for the popup layout hierarchy
    private var panelRoot: FrameLayout? = null
    private var panelContent: LinearLayout? = null
    private var headerBar: LinearLayout? = null
    private var titleView: TextView? = null
    private var closeButton: ImageView? = null
    private var searchBox: EditText? = null
    private var chipScroll: HorizontalScrollView? = null
    private var chipContainer: LinearLayout? = null
    private var resizeHandle: ImageView? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    private var selectedCategory = "All"
    private var searchQuery = ""
    private var searchDebounceJob: Job? = null
    private var openRefreshJob: Job? = null
    private var categoryUiRefreshPosted = false
    private var stickerSubmitPosted = false

    /** Transient slider previews (never persisted) + the 5s reveal deadline. */
    private val appearanceState = OverlayAppearanceState()

    companion object {
        const val CHANNEL_ID = "stickhub_overlay_channel"
        const val NOTIF_ID = 1001
        const val ACTION_REFRESH_CONFIGURATION = "com.hkm.stickhub.action.REFRESH_OVERLAY_CONFIGURATION"
        const val ACTION_UPDATE_APPEARANCE = "com.hkm.stickhub.action.UPDATE_APPEARANCE"
        const val ACTION_UPDATE_SHADOW = "com.hkm.stickhub.action.UPDATE_SHADOW"
        const val ACTION_REVEAL_CONTROLS = "com.hkm.stickhub.action.REVEAL_CONTROLS"
        const val ACTION_PREVIEW_APPEARANCE = "com.hkm.stickhub.PREVIEW_APPEARANCE"
        const val EXTRA_APPEARANCE_LAYER = "appearance_layer"
        const val EXTRA_APPEARANCE_VALUE = "appearance_value"
        var isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun hasOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            android.provider.Settings.canDrawOverlays(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Never loop restarts without permission, and never claim STICKY:
        // the overlay lives exactly as long as the user keeps it enabled.
        if (!hasOverlayPermission()) {
            isRunning = false
            stopSelf()
            return START_NOT_STICKY
        }
        if (::windowManager.isInitialized) {
            when (intent?.action) {
                ACTION_REFRESH_CONFIGURATION -> refreshOverlayConfiguration()
                ACTION_UPDATE_APPEARANCE -> {
                    appearanceState.clearPreviews()
                    updateOverlayAppearance()
                }
                ACTION_UPDATE_SHADOW -> updateStickerShadows()
                ACTION_REVEAL_CONTROLS -> revealOverlayControls()
                ACTION_PREVIEW_APPEARANCE -> {
                    val layer = intent.getStringExtra(EXTRA_APPEARANCE_LAYER)
                    val value = intent.getFloatExtra(EXTRA_APPEARANCE_VALUE, Float.NaN)
                    if (appearanceState.preview(layer, value)) {
                        updateOverlayAppearance()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        // If the overlay permission was revoked while we were dead, a sticky
        // restart must not crash on addView — bail out cleanly instead.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
            !android.provider.Settings.canDrawOverlays(this)
        ) {
            isRunning = false
            stopSelf()
            return
        }
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // Shared process-wide repository: same StateFlows, DB owner and
        // clipboard dedup mutex as the activity. Never closed here.
        repository = StickerRepository.getInstance(this)

        startForegroundServiceNotification()
        try {
            setupBubbleView()
            setupPanelView()
        } catch (e: SecurityException) {
            isRunning = false
            stopSelf()
            return
        }

        serviceScope.launch {
            repository.categoryOrderFlow.collect {
                if (isPanelOpen && chipScroll?.visibility == View.VISIBLE) {
                    scheduleCategoryUiRefresh()
                }
            }
        }

        serviceScope.launch {
            try {
                repository.refresh()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                android.util.Log.e("StickHubOverlay", "Initial overlay refresh failed", error)
            }
        }
    }

    private fun isDarkMode(): Boolean {
        return ThemePreferences.resolveIsDark(this, ThemePreferences.getThemeMode(this))
    }

    private fun withAlpha(color: Int, alpha: Int): Int = Color.argb(
        alpha.coerceIn(0, 255),
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )

    /** Per-theme popup frame geometry: most themes share the soft 16dp card,
     * while Neubrutalism goes sharp + thick, Pixel near-square, Glass rounder. */
    private data class PopupChrome(val cornerRadiusPx: Float, val strokePx: Int)

    private fun popupChrome(): PopupChrome {
        val density = resources.displayMetrics.density
        return when (currentPalette().visualTheme) {
            AppVisualTheme.NEUBRUTALISM -> PopupChrome(4f * density, (3f * density).toInt().coerceAtLeast(2))
            AppVisualTheme.PIXEL -> PopupChrome(2f * density, (2f * density).toInt().coerceAtLeast(1))
            AppVisualTheme.GLASS -> PopupChrome(24f * density, (1f * density).toInt().coerceAtLeast(1))
            else -> PopupChrome(16f * density, (1f * density).toInt().coerceAtLeast(1))
        }
    }

    private var revealJob: Job? = null

    private fun updateOverlayAppearance() {
        // Lightweight path: only applies view alphas + colors.
        // Must NEVER rebuild the sticker grid here — this is called on every
        // slider tick during drag, and a rebuild (decode + shadow re-render
        // for every sticker) is what caused settings jank + useless sliders.
        // Sticker shadow re-render has its own debounced action (ACTION_UPDATE_SHADOW).
        // Transient previews flow through effective*() without touching prefs.
        bubbleView?.alpha = effectiveBubbleOpacity()

        if (isPanelOpen) {
            panelSurfaceView?.alpha = effectiveSurfaceOpacity()
            chromeContainer?.alpha = effectiveChromeOpacity()
            stickerRecyclerView?.alpha = effectiveStickersOpacity()
            closeBtnView?.alpha = effectiveCloseOpacity()
            resizeBtnView?.alpha = effectiveResizeOpacity()
        }

        val palette = currentPalette()
        val density = resources.displayMetrics.density

        bubbleBg?.apply {
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 240 else 245))
            setStroke((1.5f * density).toInt(), palette.primaryColor)
        }
        bubbleIcon?.setColorFilter(palette.primaryColor)

        panelBg?.apply {
            val chrome = popupChrome()
            cornerRadius = chrome.cornerRadiusPx
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 244 else 250))
            setStroke(chrome.strokePx, withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
        }
        closeBtnView?.setColorFilter(palette.textColor)
        closeBtnBg?.apply {
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 200 else 220))
            setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
        }
        resizeBtnView?.setColorFilter(palette.accentColor)
        resizeHandleBg?.setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 190 else 210))
        emptyStateTextView?.setTextColor(palette.mutedTextColor)
        searchBg?.setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 140 else 200))
    }

    /**
     * Heavy path for sticker shadow strength changes only.
     * Called once when the shadow slider is released (ACTION_UPDATE_SHADOW),
     * never per-tick during drag. Re-renders cached thumbnails with the new
     * silhouette shadow strength.
     */
    private fun updateStickerShadows() {
        StickerShadowRenderer.clearCache()
        if (isPanelOpen) {
            submitStickers(force = true)
        }
    }

    private fun revealOverlayControls() {
        revealJob?.cancel()
        // Reveal is a 5s deadline inside the appearance state: every alpha
        // lookup below (including a panel opened mid-window) resolves to 1.
        appearanceState.reveal(nowMs())
        updateOverlayAppearance()

        revealJob = serviceScope.launch {
            delay(OverlayAppearanceState.REVEAL_DURATION_MS)
            updateOverlayAppearance()
        }
    }

    private fun refreshOverlayConfiguration() {
        if (bubbleView == null || panelRoot == null) {
            recreateOverlayViews()
            return
        }

        val palette = currentPalette()
        val density = resources.displayMetrics.density

        // 1. Refresh Bubble styling & dimensions
        val bubbleSizeDp = OverlayPreferences.bubbleSizeDp(this)
        val bubbleSize = (bubbleSizeDp * density).toInt()
        bubbleView?.let { bubble ->
            if (::bubbleParams.isInitialized) {
                bubbleParams.width = bubbleSize
                bubbleParams.height = bubbleSize
                bubbleBg?.apply {
                    setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 240 else 245))
                    setStroke((1.5f * density).toInt(), palette.primaryColor)
                }
                bubbleIcon?.setColorFilter(palette.primaryColor)
                if (bubble.isAttachedToWindow) {
                    windowManager.updateViewLayout(bubble, bubbleParams)
                }
            }
        }

        // 2. Refresh Panel visibility & colors
        val showTitle = OverlayPreferences.showTitle(this)
        val showSearch = OverlayPreferences.showSearch(this)
        val showCategories = OverlayPreferences.showCategories(this)

        panelHeaderView?.visibility = if (showTitle) View.VISIBLE else View.GONE
        panelTitleView?.apply {
            visibility = if (showTitle) View.VISIBLE else View.GONE
            setTextColor(palette.textColor)
        }
        panelSearchView?.apply {
            visibility = if (showSearch) View.VISIBLE else View.GONE
            setTextColor(palette.textColor)
            setHintTextColor(withAlpha(palette.mutedTextColor, 160))
        }
        searchBg?.setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 140 else 200))
        categoryScrollView?.visibility = if (showCategories) View.VISIBLE else View.GONE

        // Keep the popup compact when optional chrome changes at runtime.
        // The close control is handled independently below, so no artificial
        // blank top row is required for it.
        panelContent?.let { content ->
            val isGridOnly = !showTitle && !showSearch && !showCategories
            val outerPad = if (isGridOnly) (4 * density).toInt() else (6 * density).toInt()
            content.setPadding(outerPad, outerPad, outerPad, outerPad)
        }

        // Hidden rows must not keep filtering invisibly. Clearing the EditText
        // keeps the widget and the query in the same state.
        syncSearchRow(showSearch)
        // A hidden tag row keeps the configured start filter; only an invalid
        // one falls back (resolved at open), never a blind force to All.

        panelBg?.apply {
            val chrome = popupChrome()
            cornerRadius = chrome.cornerRadiusPx
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 244 else 250))
            setStroke(chrome.strokePx, withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
        }
        closeBtnView?.setColorFilter(palette.textColor)
        closeBtnBg?.apply {
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 200 else 220))
            setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
        }
        resizeBtnView?.setColorFilter(palette.accentColor)
        resizeHandleBg?.setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 190 else 210))
        emptyStateTextView?.setTextColor(palette.mutedTextColor)

        if (isPanelOpen) {
            scheduleCategoryUiRefresh()
        }

        panelRoot?.let { panel ->
            if (panel.isAttachedToWindow && ::panelParams.isInitialized) {
                windowManager.updateViewLayout(panel, panelParams)
            }
        }
        updateCloseOverlayLayout()
    }

    private fun recreateOverlayViews() {
        val wasPanelOpen = isPanelOpen
        panelGeneration++
        removeCloseOverlay()
        panelRoot?.let { panel ->
            if (panel.isAttachedToWindow) windowManager.removeViewImmediate(panel)
        }
        bubbleView?.let { bubble ->
            if (bubble.isAttachedToWindow) windowManager.removeViewImmediate(bubble)
        }
        panelRoot = null
        bubbleView = null
        isPanelOpen = false

        setupBubbleView()
        setupPanelView()

        if (wasPanelOpen) togglePanel()
    }

    private fun startForegroundServiceNotification() {
        // NotificationChannel exists only on API 26+; the compat builder
        // below works back to API 24 without it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "StickHub Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Floating sticker quick access"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StickHub Overlay Active")
            .setContentText("Tap the floating button to access stickers")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    private fun currentOverlayBounds(): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        return Pair(metrics.widthPixels, metrics.heightPixels)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        reflowOverlayViews()
    }

    private fun reflowOverlayViews() {
        val (screenW, screenH) = currentOverlayBounds()

        // 1. Reflow bubble position using saved normalized fractions
        bubbleView?.let { bubble ->
            if (bubble.isAttachedToWindow && ::bubbleParams.isInitialized) {
                val fracX = OverlayPreferences.bubblePositionFractionX(this)
                val fracY = OverlayPreferences.bubblePositionFractionY(this)
                val bounds = OverlayLayoutPolicy.clampBubbleBounds(
                    x = bubbleParams.x,
                    y = bubbleParams.y,
                    bubbleSize = bubbleParams.width,
                    screenWidth = screenW,
                    screenHeight = screenH
                )
                if (bubbleParams.width != bounds.size) {
                    bubbleParams.width = bounds.size
                    bubbleParams.height = bounds.size
                }
                val (newX, newY) = OverlayLayoutPolicy.denormalizePosition(
                    OverlayLayoutPolicy.NormalizedPosition(fracX, fracY),
                    bounds.maxX,
                    bounds.maxY
                )
                bubbleParams.x = newX
                bubbleParams.y = newY
                windowManager.updateViewLayout(bubble, bubbleParams)
            }
        }

        // 2. Reflow panel geometry from one shared snapshot. The saved size is
        // clamped even while the panel is closed/detached, so reopening after
        // a rotation never restores a viewport-busting rectangle.
        if (::panelParams.isInitialized) {
            val density = resources.displayMetrics.density
            val showTitle = OverlayPreferences.showTitle(this)
            val showSearch = OverlayPreferences.showSearch(this)
            val showCategories = OverlayPreferences.showCategories(this)

            val minW = OverlayLayoutPolicy.minPanelWidthPx(density)
            val minH = OverlayLayoutPolicy.minPanelHeightPx(
                density = density,
                showTitle = showTitle,
                showSearch = showSearch,
                showCategories = showCategories
            )
            val maxW = (screenW * 0.94f).toInt()
            val maxH = (screenH * 0.85f).toInt()

            val clamped = OverlayLayoutPolicy.clampPanelBounds(
                x = panelParams.x,
                y = panelParams.y,
                width = panelParams.width,
                height = panelParams.height,
                screenWidth = screenW,
                screenHeight = screenH,
                minWidth = minW,
                minHeight = minH,
                maxWidth = maxW,
                maxHeight = maxH
            )
            panelParams.x = clamped.x
            panelParams.y = clamped.y
            panelParams.width = clamped.width
            panelParams.height = clamped.height

            OverlayPreferences.setPanelPosition(this, clamped.x, clamped.y)
            OverlayPreferences.setPanelWidthPx(this, clamped.width)
            OverlayPreferences.setPanelHeightPx(this, clamped.height)

            panelRoot?.let { panel ->
                if (panel.isAttachedToWindow) {
                    windowManager.updateViewLayout(panel, panelParams)
                }
            }
            updateCloseOverlayLayout()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubbleView() {
        val density = resources.displayMetrics.density
        val bubbleSizeDp = OverlayPreferences.bubbleSizeDp(this)
        val bubbleSize = (bubbleSizeDp * density).toInt()
        val (screenW, screenH) = currentOverlayBounds()

        val fracX = OverlayPreferences.bubblePositionFractionX(this)
        val fracY = OverlayPreferences.bubblePositionFractionY(this)
        val bounds = OverlayLayoutPolicy.clampBubbleBounds(
            x = 0,
            y = 0,
            bubbleSize = bubbleSize,
            screenWidth = screenW,
            screenHeight = screenH
        )
        val (initialX, initialY) = OverlayLayoutPolicy.denormalizePosition(
            OverlayLayoutPolicy.NormalizedPosition(fracX, fracY),
            bounds.maxX,
            bounds.maxY
        )

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            bubbleSize,
            bubbleSize,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }

        val palette = currentPalette()
        val bubble = FrameLayout(this).apply {
            alpha = OverlayPreferences.bubbleOpacity(this@OverlayService)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 240 else 245))
                setStroke((1.5f * density).toInt(), palette.primaryColor)
            }
            bubbleBg = bg
            background = bg
            elevation = 16f

            // Lucide Sticker Vector Icon scaled proportionally to bubble size
            val icon = ImageView(this@OverlayService).apply {
                setImageDrawable(ContextCompat.getDrawable(this@OverlayService, LucideR.drawable.lucide_ic_sticker))
                setColorFilter(palette.primaryColor)
                val pad = (bubbleSize * 0.22f).toInt()
                setPadding(pad, pad, pad, pad)
            }
            bubbleIcon = icon
            addView(icon)
        }

        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var dragStartX = 0
        var dragStartY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        bubble.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dragStartX = bubbleParams.x
                    dragStartY = bubbleParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (hypot(dx.toDouble(), dy.toDouble()) > touchSlop) {
                        isClick = false
                    }
                    val (currentW, currentH) = currentOverlayBounds()
                    val clamped = OverlayLayoutPolicy.clampBubbleBounds(
                        x = dragStartX + dx,
                        y = dragStartY + dy,
                        bubbleSize = bubbleParams.width,
                        screenWidth = currentW,
                        screenHeight = currentH
                    )
                    bubbleParams.x = clamped.x
                    bubbleParams.y = clamped.y
                    windowManager.updateViewLayout(bubble, bubbleParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        // Route through click for TalkBack/switch access parity.
                        v.performClick()
                    } else {
                        val (currentW, currentH) = currentOverlayBounds()
                        val clamped = OverlayLayoutPolicy.clampBubbleBounds(
                            x = bubbleParams.x,
                            y = bubbleParams.y,
                            bubbleSize = bubbleParams.width,
                            screenWidth = currentW,
                            screenHeight = currentH
                        )
                        val normalized = OverlayLayoutPolicy.normalizePosition(
                            x = clamped.x,
                            y = clamped.y,
                            maxX = clamped.maxX,
                            maxY = clamped.maxY
                        )
                        OverlayPreferences.setBubblePositionFraction(
                            this@OverlayService,
                            normalized.fractionX,
                            normalized.fractionY
                        )
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    // Gesture aborted (e.g. rotation mid-drag): persist where
                    // the bubble actually is instead of dropping the move.
                    val (currentW, currentH) = currentOverlayBounds()
                    val clamped = OverlayLayoutPolicy.clampBubbleBounds(
                        x = bubbleParams.x,
                        y = bubbleParams.y,
                        bubbleSize = bubbleParams.width,
                        screenWidth = currentW,
                        screenHeight = currentH
                    )
                    val normalized = OverlayLayoutPolicy.normalizePosition(
                        x = clamped.x,
                        y = clamped.y,
                        maxX = clamped.maxX,
                        maxY = clamped.maxY
                    )
                    OverlayPreferences.setBubblePositionFraction(
                        this@OverlayService,
                        normalized.fractionX,
                        normalized.fractionY
                    )
                    true
                }
                else -> false
            }
        }

        bubble.setOnClickListener {
            StickHubHaptics.performTap(it)
            togglePanel()
        }

        bubbleView = bubble
        windowManager.addView(bubble, bubbleParams)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPanelView() {
        val density = resources.displayMetrics.density
        val (screenW, screenH) = currentOverlayBounds()

        // Read preferences for optional chrome visibility
        val showTitle = OverlayPreferences.showTitle(this)
        val showSearch = OverlayPreferences.showSearch(this)
        val showCategories = OverlayPreferences.showCategories(this)
        val isGridOnly = !showTitle && !showSearch && !showCategories

        // If search is disabled, no hidden query may persist. Category
        // selection stays as-is; the open-time start-filter policy decides.
        if (!showSearch) searchQuery = ""

        // Dynamic minimum bounds calculated based on enabled chrome
        val minPanelWidth = OverlayLayoutPolicy.minPanelWidthPx(density)
        val minPanelHeight = OverlayLayoutPolicy.minPanelHeightPx(
            density = density,
            showTitle = showTitle,
            showSearch = showSearch,
            showCategories = showCategories
        )
        val maxPanelWidth = (screenW * 0.94f).toInt()
        val maxPanelHeight = (screenH * 0.85f).toInt()

        // Restore saved dimensions and position, or fall back to defaults
        val savedWidth = OverlayPreferences.panelWidthPx(this)
        val savedHeight = OverlayPreferences.panelHeightPx(this)
        val savedX = OverlayPreferences.panelPositionX(this)
        val savedY = OverlayPreferences.panelPositionY(this)

        val initialW = if (savedWidth > 0) savedWidth else (OverlayLayoutPolicy.DEFAULT_PANEL_WIDTH_DP * density).toInt()
        val initialH = if (savedHeight > 0) savedHeight else (OverlayLayoutPolicy.DEFAULT_PANEL_HEIGHT_DP * density).toInt()
        val initialX = if (savedX >= 0) savedX else (screenW - initialW) / 2
        val initialY = if (savedY >= 0) savedY else (screenH - initialH) / 3

        val clampedBounds = OverlayLayoutPolicy.clampPanelBounds(
            x = initialX,
            y = initialY,
            width = initialW,
            height = initialH,
            screenWidth = screenW,
            screenHeight = screenH,
            minWidth = minPanelWidth,
            minHeight = minPanelHeight,
            maxWidth = maxPanelWidth,
            maxHeight = maxPanelHeight
        )

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        panelParams = WindowManager.LayoutParams(
            clampedBounds.width,
            clampedBounds.height,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = clampedBounds.x
            y = clampedBounds.y
        }

        val palette = currentPalette()

        // 1. Root popup FrameLayout: translucent container holding independent layers
        val root = FrameLayout(this).apply {
            // The close control intentionally overhangs the visual surface at
            // its top-right corner; do not clip that decorative overhang.
            clipChildren = false
            clipToPadding = false
        }

        // 2. Layer 1: Dedicated Background / Surface View (has its own opacity).
        // NOTE: elevation must stay on the SAME plane as content + floating
        // buttons (12f). Siblings with higher elevation paint ON TOP —
        // a higher surface elevation covered the stickers whenever the
        // background opacity was raised. Same-plane ties fall back to
        // insertion order: surface -> content -> close -> resize.
        val surfaceLayer = View(this).apply {
            val chrome = popupChrome()
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = chrome.cornerRadiusPx
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 244 else 250))
                setStroke(chrome.strokePx, withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
            }
            panelBg = bg
            background = bg
            elevation = 12f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                // Leave a slim transparent corner dock so the X reads as an
                // external control instead of consuming popup content space.
                setMargins(0, (6 * density).toInt(), (6 * density).toInt(), 0)
            }
            alpha = effectiveSurfaceOpacity()
        }
        panelSurfaceView = surfaceLayer
        root.addView(surfaceLayer)

        // 3. Layer 2: Panel Content LinearLayout.
        // Same elevation plane as the surface (12f): ties fall back to
        // insertion order, so content always paints ABOVE the background.
        // Transparent container emits no shadow of its own.
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            elevation = 12f
            val outerPad = if (isGridOnly) (4 * density).toInt() else (6 * density).toInt()
            setPadding(outerPad, outerPad, outerPad, outerPad)
        }

        // Chrome Container (Header, Search, Categories) with its own opacity.
        val chrome = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            alpha = effectiveChromeOpacity()
        }
        chromeContainer = chrome

        // Draggable listener for moving the entire popup window
        var initialPanelX = 0
        var initialPanelY = 0
        var initialTouchPanelX = 0f
        var initialTouchPanelY = 0f
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var draggingPanel = false

        val panelDragListener = View.OnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialPanelX = panelParams.x
                    initialPanelY = panelParams.y
                    initialTouchPanelX = event.rawX
                    initialTouchPanelY = event.rawY
                    draggingPanel = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchPanelX).toInt()
                    val dy = (event.rawY - initialTouchPanelY).toInt()
                    if (!draggingPanel && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        draggingPanel = true
                    }
                    if (draggingPanel) {
                        val (currentW, currentH) = currentOverlayBounds()
                        panelParams.x = (initialPanelX + dx).coerceIn(0, max(0, currentW - panelParams.width))
                        panelParams.y = (initialPanelY + dy).coerceIn(0, max(0, currentH - panelParams.height))
                        windowManager.updateViewLayout(root, panelParams)
                        updateCloseOverlayLayout()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (draggingPanel) {
                        OverlayPreferences.setPanelPosition(this@OverlayService, panelParams.x, panelParams.y)
                    }
                    true
                }
                else -> false
            }
        }

        // A. Header Bar (Shown only when showTitle is true)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((6 * density).toInt(), (2 * density).toInt(), (34 * density).toInt(), (4 * density).toInt())
            visibility = if (showTitle) View.VISIBLE else View.GONE
            setOnTouchListener(panelDragListener)
        }
        panelHeaderView = header

        val title = TextView(this).apply {
            text = "Quick Stickers"
            setTextColor(palette.textColor)
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = if (showTitle) View.VISIBLE else View.GONE
        }
        panelTitleView = title
        header.addView(title)
        chrome.addView(header)

        // B. Search Box (Compact ~40dp, shown only when showSearch is true)
        val search = EditText(this).apply {
            hint = "Search stickers..."
            setHintTextColor(withAlpha(palette.mutedTextColor, 160))
            setTextColor(palette.textColor)
            textSize = 13f
            setSingleLine(true)
            val sBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12 * density
                setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 140 else 200))
            }
            searchBg = sBg
            background = sBg
            val pH = (10 * density).toInt()
            val pV = (6 * density).toInt()
            setPadding(pH, pV, pH, pV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (40 * density).toInt()
            ).apply {
                setMargins(0, 0, 0, (6 * density).toInt())
            }
            visibility = if (showSearch) View.VISIBLE else View.GONE

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchDebounceJob?.cancel()
                    searchDebounceJob = serviceScope.launch {
                        delay(200)
                        searchQuery = s?.toString()?.trim()?.lowercase().orEmpty()
                        submitStickers()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        panelSearchView = search
        chrome.addView(search)

        // C. Category Chips HorizontalScrollView (shown only when showCategories is true)
        val chipsScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (6 * density).toInt())
            }
            visibility = if (showCategories) View.VISIBLE else View.GONE
        }
        categoryScrollView = chipsScroll
        val chipsGroup = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        chipsScroll.addView(chipsGroup)
        chrome.addView(chipsScroll)
        content.addView(chrome)

        // D. Sticker list: RecyclerView + GridLayoutManager with its own
        // opacity. Only attached cells decode thumbnails (see adapter).
        val recycler = androidx.recyclerview.widget.RecyclerView(this).apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(
                this@OverlayService,
                OverlayLayoutPolicy.GRID_COLUMNS
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            clipToPadding = false
            alpha = effectiveStickersOpacity()
        }
        stickerRecyclerView = recycler
        stickerAdapter = OverlayStickerAdapter(serviceScope) { view, sticker ->
            onStickerSelected(view, sticker)
        }.also { recycler.adapter = it }
        content.addView(recycler)

        root.addView(content)

        // Empty-library placeholder lives above the surface, below controls.
        val emptyView = TextView(this).apply {
            text = "No stickers found"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(withAlpha(if (isDarkMode()) Color.WHITE else Color.BLACK, 130))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            elevation = 12f
            visibility = View.GONE
        }
        emptyStateTextView = emptyView
        root.addView(emptyView)

        // If title is hidden, allow dragging from the top rim of the panel
        if (!showTitle) {
            root.setOnTouchListener { _, event ->
                val topRimHeight = (22 * density).toInt()
                if (event.y <= topRimHeight && event.x < (root.width - (34 * density).toInt())) {
                    panelDragListener.onTouch(root, event)
                } else {
                    false
                }
            }
        }

        // 3. Layer 2: Floating Controls overlaying the popup edges

        // Top-End Close Button. Tap closes; a deliberate hold turns it into the popup drag handle.
        // The 36dp hit box exceeds the 30dp artwork so the control stays
        // finger-friendly without visually crowding the popup corner.
        val closeBtn = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@OverlayService, LucideR.drawable.lucide_ic_x))
            setColorFilter(palette.textColor)
            val btnSize = (30 * density).toInt()
            val pad = (7 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 200 else 220))
                setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
            }
            closeBtnBg = btnBg
            background = btnBg
            contentDescription = "Close popup. Hold and drag to move."
            elevation = 12f
            closeOverlaySizePx = btnSize
        }
        closeOverlayParams = WindowManager.LayoutParams(
            closeOverlaySizePx, closeOverlaySizePx, layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }
        var closeInitialPanelX = 0
        var closeInitialPanelY = 0
        var closeInitialTouchX = 0f
        var closeInitialTouchY = 0f
        var movingFromClose = false
        var closeGestureCancelled = false
        var closePressActive = false
        val closeLongPress = Runnable {
            if (closePressActive && !closeGestureCancelled) {
                movingFromClose = true
                StickHubHaptics.performLongPress(closeBtn)
                closeBtn.animate().scaleX(0.9f).scaleY(0.9f).alpha(0.8f).setDuration(120).start()
            }
        }
        closeBtn.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    closeInitialPanelX = panelParams.x
                    closeInitialPanelY = panelParams.y
                    closeInitialTouchX = event.rawX
                    closeInitialTouchY = event.rawY
                    movingFromClose = false
                    closeGestureCancelled = false
                    closePressActive = true
                    view.postDelayed(closeLongPress, ViewConfiguration.getLongPressTimeout().toLong())
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - closeInitialTouchX).toInt()
                    val dy = (event.rawY - closeInitialTouchY).toInt()
                    if (!movingFromClose && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        closeGestureCancelled = true
                        view.removeCallbacks(closeLongPress)
                    }
                    if (movingFromClose) {
                        val (currentW, currentH) = currentOverlayBounds()
                        panelParams.x = (closeInitialPanelX + dx).coerceIn(0, max(0, currentW - panelParams.width))
                        panelParams.y = (closeInitialPanelY + dy).coerceIn(0, max(0, currentH - panelParams.height))
                        windowManager.updateViewLayout(root, panelParams)
                        updateCloseOverlayLayout()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.removeCallbacks(closeLongPress)
                    closePressActive = false
                    closeBtn.animate().scaleX(1f).scaleY(1f).alpha(effectiveCloseOpacity()).setDuration(160).start()
                    if (movingFromClose) {
                        OverlayPreferences.setPanelPosition(this@OverlayService, panelParams.x, panelParams.y)
                    } else if (!closeGestureCancelled && event.actionMasked == MotionEvent.ACTION_UP) {
                        // Route through click for TalkBack/switch access parity.
                        view.performClick()
                    }
                    movingFromClose = false
                    true
                }
                else -> true
            }
        }
        closeBtn.setOnClickListener { view ->
            StickHubHaptics.performTap(view)
            togglePanel()
        }
        closeBtn.alpha = effectiveCloseOpacity()
        closeBtnView = closeBtn

        // Bottom-End Resize Handle (Lucide move_diagonal_2). Same 36dp
        // reasoning as the close button: bigger target, same artwork.
        val resizeBtn = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@OverlayService, LucideR.drawable.lucide_ic_move_diagonal_2))
            setColorFilter(palette.accentColor)
            val btnSize = (36 * density).toInt()
            val pad = (10 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val handleBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(
                    8 * density, 8 * density,
                    4 * density, 4 * density,
                    14 * density, 14 * density,
                    4 * density, 4 * density
                )
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 190 else 210))
            }
            resizeHandleBg = handleBg
            background = handleBg
            contentDescription = "Resize quick stickers"
            elevation = 12f
            layoutParams = FrameLayout.LayoutParams(btnSize, btnSize).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(0, 0, (2 * density).toInt(), (2 * density).toInt())
            }
        }

        var initialPanelWidth = 0
        var initialPanelHeight = 0
        var initialResizeTouchX = 0f
        var initialResizeTouchY = 0f

        resizeBtn.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialPanelWidth = panelParams.width
                    initialPanelHeight = panelParams.height
                    initialResizeTouchX = event.rawX
                    initialResizeTouchY = event.rawY
                    StickHubHaptics.performTick(view)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val targetW = initialPanelWidth + (event.rawX - initialResizeTouchX).toInt()
                    val targetH = initialPanelHeight + (event.rawY - initialResizeTouchY).toInt()

                    val (currentW, currentH) = currentOverlayBounds()
                    val currentMaxW = (currentW * 0.94f).toInt()
                    val currentMaxH = (currentH * 0.85f).toInt()
                    // Minimums recomputed live: chrome toggles during the
                    // panel's life must not leave a stale floor behind.
                    val liveMinW = OverlayLayoutPolicy.minPanelWidthPx(density)
                    val liveMinH = OverlayLayoutPolicy.minPanelHeightPx(
                        density = density,
                        showTitle = OverlayPreferences.showTitle(this@OverlayService),
                        showSearch = OverlayPreferences.showSearch(this@OverlayService),
                        showCategories = OverlayPreferences.showCategories(this@OverlayService)
                    )

                    val clamped = OverlayLayoutPolicy.clampPanelBounds(
                        x = panelParams.x,
                        y = panelParams.y,
                        width = targetW,
                        height = targetH,
                        screenWidth = currentW,
                        screenHeight = currentH,
                        minWidth = liveMinW,
                        minHeight = liveMinH,
                        maxWidth = currentMaxW,
                        maxHeight = currentMaxH
                    )

                    panelParams.width = clamped.width
                    panelParams.height = clamped.height
                    panelParams.x = clamped.x
                    panelParams.y = clamped.y
                    windowManager.updateViewLayout(root, panelParams)
                    updateCloseOverlayLayout()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    OverlayPreferences.setPanelWidthPx(this@OverlayService, panelParams.width)
                    OverlayPreferences.setPanelHeightPx(this@OverlayService, panelParams.height)
                    OverlayPreferences.setPanelPosition(this@OverlayService, panelParams.x, panelParams.y)
                    submitStickers()
                    true
                }
                else -> true
            }
        }
        resizeBtnView = resizeBtn
        root.addView(resizeBtn)

        // Store explicit references
        panelRoot = root
        panelContent = content
        headerBar = header
        titleView = title
        closeButton = closeBtn
        searchBox = search
        chipScroll = chipsScroll
        chipContainer = chipsGroup
        resizeHandle = resizeBtn
    }

    private fun togglePanel() {
        val panel = panelRoot ?: return
        val currentToken = ++panelGeneration
        openRefreshJob?.cancel()
        openRefreshJob = null

        if (isPanelOpen) {
            if (searchBox?.hasFocus() == true) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                imm?.hideSoftInputFromWindow(searchBox?.windowToken, 0)
                searchBox?.clearFocus()
            }
            panel.animate().cancel()
            panelSurfaceView?.animate()?.alpha(0f)?.setDuration(200)?.start()
            chromeContainer?.animate()?.alpha(0f)?.setDuration(200)?.start()
            stickerRecyclerView?.animate()?.alpha(0f)?.setDuration(200)?.start()
            closeBtnView?.animate()?.alpha(0f)?.setDuration(200)?.start()
            resizeBtnView?.animate()?.alpha(0f)?.setDuration(200)?.start()

            panel.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .translationY(10f)
                .setDuration(220)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    if (panelGeneration == currentToken && panel.isAttachedToWindow) {
                        removeCloseOverlay()
                        windowManager.removeView(panel)
                    }
                }
                .start()
            isPanelOpen = false
        } else {
            panel.animate().cancel()
            // A superseded open must never publish: capture the generation and
            // bail if another toggle stole the panel meanwhile.
            val openToken = currentToken
            openRefreshJob?.cancel()
            // Single source of truth at open time: re-sync chrome visibility
            // from prefs on EVERY open (not just at view creation), so a
            // missed REFRESH intent or stale view can never show hidden rows.
            val openShowTitle = OverlayPreferences.showTitle(this)
            val openShowSearch = OverlayPreferences.showSearch(this)
            val openShowCategories = OverlayPreferences.showCategories(this)
            panelHeaderView?.visibility = if (openShowTitle) View.VISIBLE else View.GONE
            panelTitleView?.visibility = if (openShowTitle) View.VISIBLE else View.GONE
            panelSearchView?.visibility = if (openShowSearch) View.VISIBLE else View.GONE
            categoryScrollView?.visibility = if (openShowCategories) View.VISIBLE else View.GONE
            syncSearchRow(openShowSearch)
            // A hidden tag row keeps the configured start filter (default /
            // last-used / custom with safe fallback) instead of forcing All.
            openRefreshJob = serviceScope.launch {
                try {
                    repository.refresh()
                    if (panelGeneration != openToken) return@launch
                    // Resolve start filter from policy
                    val categories = repository.categoriesFlow.value.map { it.name }
                    val startMode = OverlayPreferences.startFilterMode(this@OverlayService)
                    val customCat = OverlayPreferences.startCustomCategory(this@OverlayService)
                    val lastUsed = OverlayPreferences.lastUsedFilter(this@OverlayService)
                    selectedCategory = OverlayStartFilterPolicy.resolveActiveFilter(
                        mode = startMode,
                        customCategory = customCat,
                        lastUsedFilter = lastUsed,
                        availableCategories = categories
                    )
                    if (panelGeneration != openToken) return@launch
                    scheduleCategoryUiRefresh()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    // Opening the optional overlay must remain usable even when
                    // a legacy library row or refresh fails. Render the latest
                    // in-memory snapshot instead of allowing the child job to
                    // tear down the service and remove the bubble.
                    android.util.Log.e("StickHubOverlay", "Panel refresh failed", error)
                    if (panelGeneration == openToken) {
                        selectedCategory = OverlayCategoryPolicy.resolveSelection(
                            selectedCategory,
                            repository.categoriesFlow.value.map { it.name }
                        )
                        scheduleCategoryUiRefresh()
                    }
                }
            }

            panelSurfaceView?.alpha = 0f
            chromeContainer?.alpha = 0f
            stickerRecyclerView?.alpha = 0f
            closeBtnView?.alpha = 0f
            resizeBtnView?.alpha = 0f

            panel.scaleX = 0.96f
            panel.scaleY = 0.96f
            panel.translationY = 10f

            if (!panel.isAttachedToWindow) {
                windowManager.addView(panel, panelParams)
            }
            attachCloseOverlay()

            panelSurfaceView?.animate()?.alpha(effectiveSurfaceOpacity())?.setDuration(260)?.start()
            chromeContainer?.animate()?.alpha(effectiveChromeOpacity())?.setDuration(260)?.start()
            stickerRecyclerView?.animate()?.alpha(effectiveStickersOpacity())?.setDuration(260)?.start()
            closeBtnView?.animate()?.alpha(effectiveCloseOpacity())?.setDuration(260)?.start()
            resizeBtnView?.animate()?.alpha(effectiveResizeOpacity())?.setDuration(260)?.start()

            panel.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(260)
                .setInterpolator(DecelerateInterpolator())
                .start()
            isPanelOpen = true
        }
    }

    /**
     * Keeps the search widget and the backing query in the same state. Hiding
     * the row cancels a pending debounce and clears both the EditText and the
     * query, so no invisible filter survives.
     */
    private fun syncSearchRow(showSearch: Boolean) {
        if (showSearch) return
        searchDebounceJob?.cancel()
        searchDebounceJob = null
        searchQuery = ""
        val box = panelSearchView
        if (box != null && box.text.isNotEmpty()) {
            box.setText("")
        }
    }

    /**
     * Coalesces category updates onto a later UI turn. Rebuilding a ViewGroup
     * and notifying the RecyclerView from inside a chip's click dispatch can
     * race ViewRoot/RecyclerView layout on some Android versions; the overlay
     * then disappears even though the service is still alive.
     */
    private fun scheduleCategoryUiRefresh() {
        val group = chipContainer ?: return
        if (categoryUiRefreshPosted) return
        categoryUiRefreshPosted = true
        group.post {
            categoryUiRefreshPosted = false
            if (!isPanelOpen || chipScroll?.visibility != View.VISIBLE) return@post
            try {
                setupCategoryChips()
                submitStickers()
            } catch (error: Exception) {
                android.util.Log.e("StickHubOverlay", "Category UI refresh failed", error)
            }
        }
    }

    private fun updateCloseOverlayLayout() {
        val close = closeBtnView ?: return
        if (!::panelParams.isInitialized || !::closeOverlayParams.isInitialized) return
        val (screenW, screenH) = currentOverlayBounds()
        val position = OverlayLayoutPolicy.closeOverlayPosition(
            panelX = panelParams.x,
            panelY = panelParams.y,
            panelWidth = panelParams.width,
            closeSize = closeOverlaySizePx,
            screenWidth = screenW,
            screenHeight = screenH
        )
        closeOverlayParams.x = position.x
        closeOverlayParams.y = position.y
        if (closeOverlayAttached && close.isAttachedToWindow) {
            try { windowManager.updateViewLayout(close, closeOverlayParams) } catch (_: Exception) { closeOverlayAttached = false }
        }
    }

    private fun attachCloseOverlay() {
        val close = closeBtnView ?: return
        if (!::closeOverlayParams.isInitialized) return
        updateCloseOverlayLayout()
        if (!close.isAttachedToWindow) {
            try {
                windowManager.addView(close, closeOverlayParams)
                closeOverlayAttached = true
                close.isClickable = true
            } catch (error: Exception) {
                closeOverlayAttached = false
                close.isClickable = false
                android.util.Log.e("StickHubOverlay", "Close overlay attach failed", error)
            }
        }
    }

    private fun removeCloseOverlay() {
        val close = closeBtnView ?: return
        close.isClickable = false
        if (close.isAttachedToWindow) {
            try { windowManager.removeViewImmediate(close) } catch (_: Exception) { }
        }
        closeOverlayAttached = false
    }

    private fun setupCategoryChips() {
        val group = chipContainer ?: return
        group.removeAllViews()

        if (chipScroll?.visibility != View.VISIBLE) return

        val palette = currentPalette()
        val density = resources.displayMetrics.density
        val availableCategories = repository.categoriesFlow.value.map { it.name }
        val categories = OverlayCategoryPolicy.normalize(
            orderedNames = repository.categoryOrderFlow.value,
            availableNames = availableCategories
        )
        selectedCategory = OverlayCategoryPolicy.resolveSelection(selectedCategory, availableCategories)

        for (cat in categories) {
            val isSelected = (selectedCategory == cat)
            val chip = TextView(this).apply {
                text = cat
                textSize = 11f
                typeface = if (isSelected) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
                setTextColor(
                    if (isSelected) {
                        palette.selectedChipContentColor
                    } else {
                        palette.mutedTextColor
                    }
                )
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 10 * density
                    if (isSelected) {
                        setColor(palette.selectedChipContainerColor)
                        palette.selectedChipStrokeColor?.let { strokeCol ->
                            setStroke((1 * density).toInt(), strokeCol)
                        }
                    } else {
                        setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 140 else 200))
                    }
                }
                background = bg
                val padH = (9 * density).toInt()
                val padV = (4 * density).toInt()
                setPadding(padH, padV, padH, padV)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, (5 * density).toInt(), 0)
                }

                setOnClickListener { source ->
                    StickHubHaptics.performTick(source)
                    selectedCategory = OverlayCategoryPolicy.resolveSelection(cat, availableCategories)
                    // Persist for START_FILTER=LAST_USED; without this the
                    // mode could never observe anything but the default.
                    try {
                        OverlayPreferences.setLastUsedFilter(this@OverlayService, selectedCategory)
                    } catch (error: Exception) {
                        android.util.Log.e("StickHubOverlay", "Could not persist category selection", error)
                    }
                    // Do not mutate the chip hierarchy or RecyclerView while
                    // this click is being dispatched. The posted refresh also
                    // coalesces rapid taps and StateFlow emissions.
                    scheduleCategoryUiRefresh()
                }
            }
            group.addView(chip)
        }
    }

    private fun submitStickers(force: Boolean = false) {
        val adapter = stickerAdapter ?: return
        val recycler = stickerRecyclerView
        if (recycler?.isComputingLayout == true) {
            if (!stickerSubmitPosted) {
                stickerSubmitPosted = true
                recycler.post {
                    stickerSubmitPosted = false
                    submitStickers(force)
                }
            }
            return
        }
        val density = resources.displayMetrics.density
        val contentPadding = if (OverlayPreferences.showTitle(this)) (6 * density).toInt() else (4 * density).toInt()
        val cellMargin = (3 * density).toInt()
        val itemSize = OverlayLayoutPolicy.gridCellSize(
            panelWidthPx = panelParams.width,
            horizontalContentPaddingPx = contentPadding,
            cellMarginPx = cellMargin
        )

        val filtered = OverlayStickerFilter.filter(
            stickers = repository.stickersFlow.value,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery
        )
        val options = OverlayStickerAdapter.RenderOptions(
            cellSize = itemSize,
            cellMargin = cellMargin,
            shadowStrength = OverlayPreferences.stickerShadowStrength(this),
            isDark = isDarkMode(),
            density = density
        )
        try {
            adapter.submit(filtered, options, force)
        } catch (error: IllegalStateException) {
            // RecyclerView can still enter a layout pass between the guard and
            // notifyDataSetChanged(). Retry on the next frame instead of
            // propagating an exception through the overlay click coroutine.
            if (!stickerSubmitPosted) {
                stickerSubmitPosted = true
                recycler?.post {
                    stickerSubmitPosted = false
                    submitStickers(force)
                }
            } else {
                android.util.Log.e("StickHubOverlay", "Sticker list update failed", error)
            }
            return
        } catch (error: Exception) {
            android.util.Log.e("StickHubOverlay", "Sticker list update failed", error)
            return
        }
        emptyStateTextView?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onStickerSelected(view: View, sticker: StickerItem) {
        val success = ClipboardHelper.copyStickerToClipboard(this, sticker)
        if (success) {
            StickHubHaptics.performConfirm(view)
            serviceScope.launch {
                repository.recordUsage(sticker.id)
            }
            val afterCopy = OverlayPreferences.afterCopyAction(this)
            if (afterCopy == OverlayAfterCopyAction.KEEP_OPEN) {
                showCopiedFeedbackOnItem(view as? ViewGroup)
            } else {
                if (ClipboardHelper.shouldShowCopiedConfirmation()) {
                    Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
                togglePanel()
            }
        } else {
            StickHubHaptics.performReject(view)
        }
    }

    private fun showCopiedFeedbackOnItem(frame: ViewGroup?) {
        frame ?: return
        val existing = frame.findViewWithTag<View>("copied_feedback_badge")
        if (existing != null) {
            frame.removeView(existing)
        }
        val density = resources.displayMetrics.density
        val palette = currentPalette()
        val badge = TextView(this).apply {
            tag = "copied_feedback_badge"
            text = "Copied!"
            textSize = 10f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 6 * density
                setColor(palette.primaryColor)
            }
            val pH = (6 * density).toInt()
            val pV = (2 * density).toInt()
            setPadding(pH, pV, pH, pV)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }
        frame.addView(badge)
        badge.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(120)
            .withEndAction {
                badge.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setStartDelay(500)
                    .withEndAction {
                        frame.removeView(badge)
                    }
                    .start()
            }
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        isPanelOpen = false
        categoryUiRefreshPosted = false
        stickerSubmitPosted = false
        revealJob?.cancel()
        revealJob = null
        searchDebounceJob?.cancel()
        searchDebounceJob = null
        openRefreshJob?.cancel()
        openRefreshJob = null
        serviceScope.cancel()
        stickerAdapter?.cancelRequests()
        stickerAdapter = null
        removeCloseOverlay()
        // Detach exactly once per attached view; WindowManager throws if a
        // view was already removed, so every removal is individually guarded.
        bubbleView?.let { bubble ->
            try {
                bubble.animate().cancel()
            } catch (_: Exception) {
            }
            if (bubble.isAttachedToWindow) {
                try {
                    windowManager.removeViewImmediate(bubble)
                } catch (_: Exception) {
                }
            }
        }
        bubbleView = null
        panelRoot?.let { panel ->
            try {
                panel.animate().cancel()
            } catch (_: Exception) {
            }
            if (panel.isAttachedToWindow) {
                try {
                    windowManager.removeViewImmediate(panel)
                } catch (_: Exception) {
                }
            }
        }
        panelRoot = null
        panelSurfaceView = null
        chromeContainer = null
        stickerRecyclerView = null
        bubbleIcon = null
        StickerShadowRenderer.clearCache()
    }
}
