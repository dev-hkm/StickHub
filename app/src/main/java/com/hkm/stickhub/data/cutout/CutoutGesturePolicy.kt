package com.hkm.stickhub.data.cutout

/** Keeps manual subject selection intentionally long-press-only. */
object CutoutGesturePolicy {
    fun shouldSelectOnTap(mode: CutoutInteractionMode): Boolean {
        return mode == CutoutInteractionMode.Auto
    }

    fun shouldSelectOnLongPress(mode: CutoutInteractionMode): Boolean {
        return mode == CutoutInteractionMode.Auto || mode == CutoutInteractionMode.Manual
    }
}
