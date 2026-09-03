package com.hkm.stickhub.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.res.Configuration
import android.util.LruCache
import java.io.File
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

class OverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
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
    private var closeBtnBg: GradientDrawable? = null
    private var resizeBtnView: ImageView? = null
    private var resizeHandleBg: GradientDrawable? = null
    private var emptyStateTextView: TextView? = null

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
    private var gridScrollView: androidx.core.widget.NestedScrollView? = null
    private var stickerGrid: GridLayout? = null
    private var resizeHandle: ImageView? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    private var selectedCategory = "All"
    private var searchQuery = ""
    private var searchDebounceJob: Job? = null

    // In-memory downsampled thumbnail cache for smooth overlay scrolling
    private val thumbnailCache = object : LruCache<String, Bitmap>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    companion object {
        const val CHANNEL_ID = "stickhub_overlay_channel"
        const val NOTIF_ID = 1001
        const val ACTION_REFRESH_CONFIGURATION = "com.hkm.stickhub.action.REFRESH_OVERLAY_CONFIGURATION"
        var isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_REFRESH_CONFIGURATION && ::windowManager.isInitialized) {
            refreshOverlayConfiguration()
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        repository = StickerRepository(this)

        startForegroundServiceNotification()
        setupBubbleView()
        setupPanelView()

        serviceScope.launch {
            repository.refresh()
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

        panelBg?.apply {
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 244 else 250))
            setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
        }
        closeBtnView?.setColorFilter(palette.textColor)
        closeBtnBg?.apply {
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 200 else 220))
            setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
        }
        resizeBtnView?.setColorFilter(palette.primaryColor)
        resizeHandleBg?.setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 190 else 210))
        emptyStateTextView?.setTextColor(palette.mutedTextColor)

        setupCategoryChips()
        refreshPanelStickers()

        panelRoot?.let { panel ->
            if (panel.isAttachedToWindow && ::panelParams.isInitialized) {
                windowManager.updateViewLayout(panel, panelParams)
            }
        }
    }

    private fun recreateOverlayViews() {
        val wasPanelOpen = isPanelOpen
        panelGeneration++
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
        val channel = NotificationChannel(
            CHANNEL_ID,
            "StickHub Overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Floating sticker quick access"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)

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

        // 2. Reflow panel position and dimensions if panel is attached/open
        panelRoot?.let { panel ->
            if (panel.isAttachedToWindow && ::panelParams.isInitialized && isPanelOpen) {
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
                windowManager.updateViewLayout(panel, panelParams)

                OverlayPreferences.setPanelPosition(this, clamped.x, clamped.y)
                OverlayPreferences.setPanelWidthPx(this, clamped.width)
                OverlayPreferences.setPanelHeightPx(this, clamped.height)
            }
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
            when (event.action) {
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
                        StickHubHaptics.performTap(v)
                        togglePanel()
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
                else -> false
            }
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

        // If search/categories are disabled, reset query/filters so no hidden filters persist
        if (!showSearch) searchQuery = ""
        if (!showCategories) selectedCategory = "All"

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

        // 1. Root popup FrameLayout: combines content layer and floating controls layer
        val root = FrameLayout(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16 * density
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 244 else 250))
                setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
            }
            panelBg = bg
            background = bg
            elevation = 14f
            val outerPad = if (isGridOnly) (4 * density).toInt() else (6 * density).toInt()
            setPadding(outerPad, outerPad, outerPad, outerPad)
        }

        // 2. Layer 1: Panel Content LinearLayout
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Draggable listener for moving the entire popup window
        var initialPanelX = 0
        var initialPanelY = 0
        var initialTouchPanelX = 0f
        var initialTouchPanelY = 0f
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var draggingPanel = false

        val panelDragListener = View.OnTouchListener { _, event ->
            when (event.action) {
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
        content.addView(header)

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
                        refreshPanelStickers()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        panelSearchView = search
        content.addView(search)

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
        content.addView(chipsScroll)

        // D. Sticker Grid inside ScrollView: fills all remaining space, no empty footer!
        val scroll = androidx.core.widget.NestedScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            isFillViewport = true
        }

        val grid = GridLayout(this).apply {
            columnCount = OverlayLayoutPolicy.GRID_COLUMNS
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = false
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        scroll.addView(grid)
        content.addView(scroll)

        root.addView(content)

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
        val closeBtn = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@OverlayService, LucideR.drawable.lucide_ic_x))
            setColorFilter(palette.textColor)
            val btnSize = (30 * density).toInt()
            val pad = (6 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 200 else 220))
                setStroke((1 * density).toInt(), withAlpha(palette.outlineColor, if (palette.isDark) 60 else 70))
            }
            closeBtnBg = btnBg
            background = btnBg
            contentDescription = "Close popup. Hold and drag to move."
            layoutParams = FrameLayout.LayoutParams(btnSize, btnSize).apply {
                gravity = Gravity.TOP or Gravity.END
                setMargins(0, (2 * density).toInt(), (2 * density).toInt(), 0)
            }
        }
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
                StickHubHaptics.performTap(closeBtn)
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
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.removeCallbacks(closeLongPress)
                    closePressActive = false
                    closeBtn.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(160).start()
                    if (movingFromClose) {
                        OverlayPreferences.setPanelPosition(this@OverlayService, panelParams.x, panelParams.y)
                    } else if (!closeGestureCancelled && event.actionMasked == MotionEvent.ACTION_UP) {
                        StickHubHaptics.performTap(view)
                        togglePanel()
                    }
                    movingFromClose = false
                    true
                }
                else -> true
            }
        }
        closeBtnView = closeBtn
        root.addView(closeBtn)

        // Bottom-End Resize Handle (Lucide move_diagonal_2)
        val resizeBtn = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@OverlayService, LucideR.drawable.lucide_ic_move_diagonal_2))
            setColorFilter(palette.primaryColor)
            val btnSize = (30 * density).toInt()
            val pad = (7 * density).toInt()
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
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialPanelWidth = panelParams.width
                    initialPanelHeight = panelParams.height
                    initialResizeTouchX = event.rawX
                    initialResizeTouchY = event.rawY
                    StickHubHaptics.performTap(view)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val targetW = initialPanelWidth + (event.rawX - initialResizeTouchX).toInt()
                    val targetH = initialPanelHeight + (event.rawY - initialResizeTouchY).toInt()

                    val (currentW, currentH) = currentOverlayBounds()
                    val currentMaxW = (currentW * 0.94f).toInt()
                    val currentMaxH = (currentH * 0.85f).toInt()

                    val clamped = OverlayLayoutPolicy.clampPanelBounds(
                        x = panelParams.x,
                        y = panelParams.y,
                        width = targetW,
                        height = targetH,
                        screenWidth = currentW,
                        screenHeight = currentH,
                        minWidth = minPanelWidth,
                        minHeight = minPanelHeight,
                        maxWidth = currentMaxW,
                        maxHeight = currentMaxH
                    )

                    panelParams.width = clamped.width
                    panelParams.height = clamped.height
                    panelParams.x = clamped.x
                    panelParams.y = clamped.y
                    windowManager.updateViewLayout(root, panelParams)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    OverlayPreferences.setPanelWidthPx(this@OverlayService, panelParams.width)
                    OverlayPreferences.setPanelHeightPx(this@OverlayService, panelParams.height)
                    OverlayPreferences.setPanelPosition(this@OverlayService, panelParams.x, panelParams.y)
                    refreshPanelStickers()
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
        gridScrollView = scroll
        stickerGrid = grid
        resizeHandle = resizeBtn
    }

    private fun togglePanel() {
        val panel = panelRoot ?: return
        val currentToken = ++panelGeneration

        if (isPanelOpen) {
            panel.animate().cancel()
            panel.animate()
                .alpha(0f)
                .scaleX(0.96f)
                .scaleY(0.96f)
                .translationY(10f)
                .setDuration(220)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    if (panelGeneration == currentToken && panel.isAttachedToWindow) {
                        windowManager.removeView(panel)
                    }
                }
                .start()
            isPanelOpen = false
        } else {
            panel.animate().cancel()
            serviceScope.launch {
                repository.refresh()
                setupCategoryChips()
                refreshPanelStickers()
            }

            panel.alpha = 0f
            panel.scaleX = 0.96f
            panel.scaleY = 0.96f
            panel.translationY = 10f

            if (!panel.isAttachedToWindow) {
                windowManager.addView(panel, panelParams)
            }

            panel.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(260)
                .setInterpolator(DecelerateInterpolator())
                .start()
            isPanelOpen = true
        }
    }

    private fun setupCategoryChips() {
        val group = chipContainer ?: return
        group.removeAllViews()

        if (chipScroll?.visibility != View.VISIBLE) return

        val palette = currentPalette()
        val density = resources.displayMetrics.density
        val categories = listOf("All", "Favorites", "Frequent") + repository.categoriesFlow.value.map { it.name }

        for (cat in categories) {
            val isSelected = (selectedCategory == cat)
            val chip = TextView(this).apply {
                text = cat
                textSize = 11f
                typeface = if (isSelected) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
                setTextColor(
                    if (isSelected) {
                        if (palette.visualTheme == AppVisualTheme.HERBARIUM) palette.onPrimaryContainerColor
                        else Color.WHITE
                    } else {
                        palette.mutedTextColor
                    }
                )
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 10 * density
                    if (isSelected) {
                        setColor(
                            if (palette.visualTheme == AppVisualTheme.HERBARIUM) palette.primaryContainerColor
                            else palette.primaryColor
                        )
                        if (palette.visualTheme == AppVisualTheme.HERBARIUM) {
                            setStroke((1 * density).toInt(), palette.primaryColor)
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

                setOnClickListener {
                    StickHubHaptics.performTap(it)
                    selectedCategory = cat
                    setupCategoryChips()
                    refreshPanelStickers()
                }
            }
            group.addView(chip)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun refreshPanelStickers() {
        val grid = stickerGrid ?: return
        grid.removeAllViews()

        val dark = isDarkMode()
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

        if (filtered.isEmpty()) {
            val emptyFrame = FrameLayout(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = GridLayout.LayoutParams.MATCH_PARENT
                    height = (140 * density).toInt()
                    columnSpec = GridLayout.spec(0, OverlayLayoutPolicy.GRID_COLUMNS)
                }
                val emptyTv = TextView(this@OverlayService).apply {
                    text = "No stickers found"
                    setTextColor(withAlpha(if (dark) Color.WHITE else Color.BLACK, 130))
                    textSize = 13f
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                addView(emptyTv)
            }
            grid.addView(emptyFrame)
            return
        }

        for (sticker in filtered) {
            val itemFrame = FrameLayout(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = itemSize
                    height = itemSize
                    setMargins(cellMargin, cellMargin, cellMargin, cellMargin)
                }

                val outValue = TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                setBackgroundResource(outValue.resourceId)

                val img = ImageView(this@OverlayService).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        val p = (3 * density).toInt()
                        setMargins(p, p, p, p)
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                addView(img)

                loadThumbnailAsync(sticker.filePath, itemSize, img)

                // Brief scale-down press effect for tactile feedback
                var downX = 0f
                var downY = 0f
                var isPress = false
                setOnTouchListener { v, ev ->
                    when (ev.action) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = ev.x
                            downY = ev.y
                            isPress = true
                            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start()
                            false
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                            false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (isPress && (abs(ev.x - downX) > 20 || abs(ev.y - downY) > 20)) {
                                isPress = false
                                v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                            }
                            false
                        }
                        else -> false
                    }
                }

                setOnClickListener { view ->
                    onStickerSelected(view, sticker)
                }
            }
            grid.addView(itemFrame)
        }
    }

    private fun loadThumbnailAsync(filePath: String, targetSize: Int, imageView: ImageView) {
        val cached = thumbnailCache.get(filePath)
        if (cached != null) {
            imageView.setImageBitmap(cached)
            return
        }

        serviceScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val file = File(filePath)
                    if (!file.exists()) return@withContext null

                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, options)

                    var sample = 1
                    while (options.outWidth / (sample * 2) >= targetSize && options.outHeight / (sample * 2) >= targetSize) {
                        sample *= 2
                    }

                    val decodeOpts = BitmapFactory.Options().apply {
                        inSampleSize = sample
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val bmp = BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
                    if (bmp != null) {
                thumbnailCache.put(filePath, bmp)
                    }
                    bmp
                } catch (_: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun onStickerSelected(view: View, sticker: StickerItem) {
        val success = ClipboardHelper.copyStickerToClipboard(this, sticker)
        if (success) {
            StickHubHaptics.performConfirm(view)
            serviceScope.launch {
                repository.recordUsage(sticker.id)
            }
            if (ClipboardHelper.shouldShowCopiedConfirmation()) {
                Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
            togglePanel()
        } else {
            StickHubHaptics.performReject(view)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
        thumbnailCache.evictAll()
        bubbleView?.let {
            if (it.isAttachedToWindow) windowManager.removeView(it)
        }
        panelRoot?.let {
            if (it.isAttachedToWindow) windowManager.removeView(it)
        }
    }
}
