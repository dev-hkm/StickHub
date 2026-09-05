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
     * Reconciles with external category changes.
     * If no drag is active, adopts the external order directly.
     * If a drag is in-flight, preserves the dragged item's position and reconciles other items.
     */
    fun syncExternal(current: List<String>) {
        if (draggedKey == null) {
            order = current.toList()
            startingOrder = order
        } else {
            // Keep in-flight drag order, drop vanished items, append newly added items
            val filtered = order.filter { it in current }
            val added = current.filter { it !in order }
            order = filtered + added
            if (draggedKey !in order) {
                draggedKey = null
                startingOrder = order
            }
        }
    }

    fun cancel() {
        order = startingOrder
        draggedKey = null
    }
}
