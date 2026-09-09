package com.hkm.stickhub.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.hkm.stickhub.data.model.StickerItem
import java.io.File

/** Builds one stable, bounded Coil request per sticker instead of recreating it every frame. */
fun stickerImageRequest(
    context: Context,
    sticker: StickerItem,
    targetPx: Int = 256
): ImageRequest {
    val file = File(sticker.filePath)
    // Repository edits use a new immutable file path. No disk stat calls on the UI thread.
    val cacheKey = "stickhub-sticker-${sticker.filePath}-$targetPx"
    return ImageRequest.Builder(context)
        .data(file)
        .size(targetPx, targetPx)
        .memoryCacheKey(cacheKey)
        .diskCacheKey(cacheKey)
        .crossfade(false)
        .build()
}

@Composable
fun rememberStickerImageRequest(sticker: StickerItem, targetPx: Int = 256): ImageRequest {
    val context = LocalContext.current
    return remember(context, sticker.filePath, targetPx) {
        stickerImageRequest(context, sticker, targetPx)
    }
}
