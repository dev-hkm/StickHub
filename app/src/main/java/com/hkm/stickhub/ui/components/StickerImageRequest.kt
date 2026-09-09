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
    val cacheKey = "stickhub-sticker-${sticker.id}-${sticker.filePath}-${file.lastModified()}-${file.length()}-$targetPx"
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
    val file = remember(sticker.id, sticker.filePath) { File(sticker.filePath) }
    val modifiedAt = file.lastModified()
    val fileLength = file.length()
    return remember(sticker.id, sticker.filePath, modifiedAt, fileLength, targetPx) {
        stickerImageRequest(context, sticker, targetPx)
    }
}
