package com.hkm.stickhub.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hkm.stickhub.data.repository.StickerOrderPolicy

/** Preview changes are local until finish; cancelling always restores the starting order. */
class CategoryDragSession(initialOrder: List<String>) {
    var order by mutableStateOf(initialOrder.toList())
        private set
    var draggedKey by mutableStateOf<String?>(null)
        private set
    private var startingOrder = order

    fun start(key: String): Boolean {
        if (key !in order) return false
        startingOrder = order
        draggedKey = key
        return true
    }

    fun moveTo(targetKey: String): Boolean {
        val from = order.indexOf(draggedKey)
        val to = order.indexOf(targetKey)
        if (from < 0 || to < 0 || from == to) return false
        order = StickerOrderPolicy.move(order, from, to)
        return true
    }

    fun finish(): List<String>? {
        val result = order.takeIf { draggedKey != null && it != startingOrder }
        draggedKey = null
        startingOrder = order
        return result
    }

    /**
     * Reconciles with the category flow (add/rename/delete from elsewhere). New names
     * append, missing names drop. A drag whose chip vanished ends silently; an active
     * drag otherwise keeps its pre-drag baseline so cancel() still rolls back.
     */
    fun syncExternal(current: List<String>) {
        order = order.filter { it in current } + current.filter { it !in order }
        if (draggedKey == null || draggedKey !in order) {
            draggedKey = null
            startingOrder = order
        }
    }

    fun cancel() {
        order = startingOrder
        draggedKey = null
    }
}
