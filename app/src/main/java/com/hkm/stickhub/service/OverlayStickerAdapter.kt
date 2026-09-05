package com.hkm.stickhub.service

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.StickerItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File

/** Only attached cells request images. Recycling cancels requests and drops bitmap references. */
internal class OverlayStickerAdapter(
    private val scope: CoroutineScope,
    private val onSelected: (View, StickerItem) -> Unit
) : RecyclerView.Adapter<OverlayStickerAdapter.Holder>() {
    data class RenderOptions(
        val cellSize: Int,
        val cellMargin: Int,
        val shadowStrength: Float,
        val isDark: Boolean,
        val density: Float
    )

    private var stickers = emptyList<StickerItem>()
    private var options = RenderOptions(1, 0, 0f, false, 1f)
    private val decodeSlots = Semaphore(2)
    private val holders = mutableSetOf<Holder>()

    init { setHasStableIds(true) }

    class Holder(val frame: FrameLayout, val image: ImageView) : RecyclerView.ViewHolder(frame) {
        var sticker: StickerItem? = null
        var job: Job? = null
        var generation = 0L
    }

    override fun getItemCount(): Int = stickers.size
    override fun getItemId(position: Int): Long =
        stickers.getOrNull(position)?.id ?: RecyclerView.NO_ID

    fun submit(items: List<StickerItem>, renderOptions: RenderOptions, force: Boolean = false) {
        if (!force && items == stickers && renderOptions == options) return
        stickers = items
        options = renderOptions
        notifyDataSetChanged()
    }

    fun cancelRequests() {
        holders.forEach { holder ->
            holder.generation++
            holder.job?.cancel()
            holder.job = null
            holder.image.setImageDrawable(null)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val context = parent.context
        val frame = FrameLayout(context).apply {
            isClickable = true
            isFocusable = true
            val value = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, value, true)
            if (value.resourceId != 0) setBackgroundResource(value.resourceId)
        }
        val image = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        frame.addView(image, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        val holder = Holder(frame, image)
        frame.setOnClickListener { holder.sticker?.let { onSelected(frame, it) } }
        frame.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }
            false
        }
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.job?.cancel()
        holder.generation++
        val sticker = stickers.getOrNull(position)
        if (sticker == null) {
            holder.sticker = null
            holder.image.setImageDrawable(null)
            return
        }
        holder.sticker = sticker
        holder.frame.findViewWithTag<View>("copied_feedback_badge")?.let(holder.frame::removeView)
        holder.frame.animate().cancel()
        holder.frame.scaleX = 1f
        holder.frame.scaleY = 1f
        holder.frame.contentDescription = "Copy ${sticker.title.ifBlank { "sticker" }}"
        holder.frame.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            options.cellSize + options.cellMargin * 2
        )
        val padding = options.cellMargin + (3 * options.density).toInt()
        holder.frame.setPadding(padding, padding, padding, padding)
        holder.image.setImageDrawable(null)
        holders.add(holder)
        if (holder.itemView.isAttachedToWindow) load(holder)
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        super.onViewAttachedToWindow(holder)
        load(holder)
    }

    override fun onViewDetachedFromWindow(holder: Holder) {
        holder.generation++
        holder.job?.cancel()
        holder.job = null
        holder.image.setImageDrawable(null)
        super.onViewDetachedFromWindow(holder)
    }

    override fun onViewRecycled(holder: Holder) {
        holder.generation++
        holder.job?.cancel()
        holder.job = null
        holder.sticker = null
        holder.image.setImageDrawable(null)
        holders.remove(holder)
        super.onViewRecycled(holder)
    }

    private fun load(holder: Holder) {
        val sticker = holder.sticker ?: return
        val render = options
        holder.job?.cancel()
        val token = ++holder.generation
        holder.job = scope.launch {
            val bitmap = try {
                withContext(Dispatchers.IO) {
                    decodeSlots.withPermit {
                        ensureActive()
                        val file = File(sticker.filePath)
                        if (!file.isFile) return@withPermit null
                        val key = StickerShadowPolicy.buildCacheKey(
                            sticker.filePath, file.lastModified(), render.cellSize,
                            render.shadowStrength, render.isDark
                        ) + ":${render.density}:${file.length()}"
                        StickerShadowRenderer.getCached(key)?.let { return@withPermit it }
                        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(file.absolutePath, bounds)
                        ensureActive()
                        // Corrupt/truncated files can report unknown bounds. Never
                        // continue with a full-resolution decode in that case:
                        // an overlay click must not take down the whole app process.
                        if (!OverlayThumbnailPolicy.hasValidBounds(bounds.outWidth, bounds.outHeight)) {
                            return@withPermit null
                        }
                        val decodeOptions = BitmapFactory.Options().apply {
                            inSampleSize = OverlayThumbnailPolicy.sampleSize(bounds.outWidth, bounds.outHeight, render.cellSize)
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                        val source = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return@withPermit null
                        var result: Bitmap? = null
                        try {
                            ensureActive()
                            result = StickerShadowRenderer.renderWithShadow(source, render.shadowStrength, render.isDark, render.density)
                            ensureActive()
                            StickerShadowRenderer.putCache(key, result)
                            result
                        } finally {
                            if (result !== source) source.recycle()
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                // A single malformed or unusually large asset must degrade to
                // the normal unavailable-image placeholder, never crash the
                // overlay service and remove the floating bubble.
                null
            } catch (_: Exception) {
                null
            }
            if (holder.generation != token || !holder.itemView.isAttachedToWindow) return@launch
            if (bitmap != null) {
                holder.image.setImageBitmap(bitmap)
            } else {
                holder.image.setImageDrawable(ContextCompat.getDrawable(holder.image.context, LucideR.drawable.lucide_ic_image))
                holder.frame.contentDescription = "${sticker.title}. Image unavailable"
            }
        }
    }
}
