package com.hkm.stickhub.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Decodes media selected by the Android Photo Picker and regular content providers.
 *
 * Picker grants are intentionally short lived. On Android 13+, picker URIs are opened through
 * [MediaStore.openFileDescriptor] first; raw streams are a compatibility fallback only.
 */
object BitmapDecodeUtil {

    private const val TAG = "BitmapDecodeUtil"

    data class DecodedImageResult(
        val bitmap: Bitmap,
        val isAlreadyTransparent: Boolean,
        val width: Int,
        val height: Int
    )

    suspend fun decodeBoundedBitmap(
        context: Context,
        uri: Uri,
        maxDimension: Int = 2048
    ): DecodedImageResult? = withContext(Dispatchers.IO) {
        try {
            val bounds = readBounds(context, uri) ?: return@withContext null
            val origWidth = bounds.outWidth
            val origHeight = bounds.outHeight
            if (origWidth <= 0 || origHeight <= 0) {
                Log.w(TAG, "Image has invalid bounds for $uri: ${origWidth}x${origHeight}")
                return@withContext null
            }

            var sampleSize = 1
            val maxOriginal = max(origWidth, origHeight)
            while (maxOriginal / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }

            val rawBitmap = readBitmap(
                context = context,
                uri = uri,
                options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
            ) ?: run {
                Log.w(TAG, "Bitmap decode returned no image for $uri")
                return@withContext null
            }

            val rotatedBitmap = applyExifRotation(context, uri, rawBitmap)
            DecodedImageResult(
                bitmap = rotatedBitmap,
                isAlreadyTransparent = checkRealTransparency(rotatedBitmap),
                width = rotatedBitmap.width,
                height = rotatedBitmap.height
            )
        } catch (error: Exception) {
            Log.e(TAG, "Unable to decode selected image: $uri", error)
            null
        }
    }

    private fun readBounds(context: Context, uri: Uri): BitmapFactory.Options? {
        val descriptorOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        if (withReadableFileDescriptor(context, uri) { descriptor ->
                BitmapFactory.decodeFileDescriptor(descriptor, null, descriptorOptions)
            } && descriptorOptions.outWidth > 0 && descriptorOptions.outHeight > 0
        ) {
            return descriptorOptions
        }

        val streamOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val decoded = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, streamOptions)
            }
            streamOptions.outWidth > 0 && streamOptions.outHeight > 0
        } catch (error: Exception) {
            Log.w(TAG, "Stream bounds fallback failed for $uri", error)
            false
        }
        return streamOptions.takeIf { decoded }
    }

    private fun readBitmap(
        context: Context,
        uri: Uri,
        options: BitmapFactory.Options
    ): Bitmap? {
        var descriptorBitmap: Bitmap? = null
        if (withReadableFileDescriptor(context, uri) { descriptor ->
                descriptorBitmap = BitmapFactory.decodeFileDescriptor(descriptor, null, options)
            } && descriptorBitmap != null
        ) {
            return descriptorBitmap
        }

        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (error: Exception) {
            Log.w(TAG, "Stream bitmap fallback failed for $uri", error)
            null
        }
    }

    private inline fun withReadableFileDescriptor(
        context: Context,
        uri: Uri,
        action: (java.io.FileDescriptor) -> Unit
    ): Boolean {
        val descriptor = openReadableFileDescriptor(context, uri) ?: return false
        descriptor.use { action(it.fileDescriptor) }
        return true
    }

    // The 4-arg MediaStore overload needs R-extensions v15, which the TIRAMISU
    // gate alone does not guarantee: any linkage failure (including Error)
    // falls through to the generic descriptor, which works everywhere.
    @SuppressLint("NewApi")
    private fun openReadableFileDescriptor(context: Context, uri: Uri): ParcelFileDescriptor? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                MediaStore.openFileDescriptor(context.contentResolver, uri, "r", null)?.let { return it }
            } catch (error: Throwable) {
                // Third-party providers are not owned by MediaStore; try the generic descriptor.
                Log.d(TAG, "MediaStore descriptor unavailable for $uri", error)
            }
        }

        try {
            return context.contentResolver.openFileDescriptor(uri, "r")
        } catch (error: Exception) {
            Log.w(TAG, "Content descriptor unavailable for $uri", error)
            return null
        }
    }

    private fun applyExifRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = readExifOrientation(context, uri) ?: return bitmap
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (rotationDegrees == 0f) return bitmap

        return try {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also { rotated ->
                if (rotated != bitmap) bitmap.recycle()
            }
        } catch (error: Exception) {
            Log.w(TAG, "Could not apply EXIF rotation for $uri", error)
            bitmap
        }
    }

    private fun readExifOrientation(context: Context, uri: Uri): Int? {
        var descriptorOrientation: Int? = null
        if (withReadableFileDescriptor(context, uri) { descriptor ->
                descriptorOrientation = ExifInterface(descriptor).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        ) {
            return descriptorOrientation
        }

        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (error: Exception) {
            Log.d(TAG, "EXIF stream fallback unavailable for $uri", error)
            null
        }
    }

    private fun checkRealTransparency(bitmap: Bitmap): Boolean {
        if (!bitmap.hasAlpha()) return false

        val width = bitmap.width
        val height = bitmap.height
        val stepX = max(1, width / 20)
        val stepY = max(1, height / 20)

        var transparentPixelCount = 0
        val threshold = 15

        for (x in 0 until width step stepX) {
            for (y in 0 until height step stepY) {
                val alpha = (bitmap.getPixel(x, y) ushr 24) and 0xFF
                if (alpha < 240 && ++transparentPixelCount >= threshold) return true
            }
        }
        return false
    }
}
