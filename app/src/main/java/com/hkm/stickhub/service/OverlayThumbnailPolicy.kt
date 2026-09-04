package com.hkm.stickhub.service

/** Bound thumbnail decode memory even when a source is extremely wide or tall. */
object OverlayThumbnailPolicy {
    fun sampleSize(width: Int, height: Int, targetSize: Int): Int {
        val longest = maxOf(width, height).coerceAtLeast(1)
        val target = targetSize.coerceAtLeast(1)
        var sample = 1
        while (sample <= Int.MAX_VALUE / 2 && longest / sample / 2 >= target) {
            sample *= 2
        }
        return sample
    }
}
