package com.hkm.stickhub

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.service.OverlayStickerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OverlayStickerAdapterTest {
    @Test
    fun largeLibraryKeepsEveryItemReachableWithoutCreatingEveryView() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val adapter = OverlayStickerAdapter(scope) { _, _ -> }
        try {
            val grid = RecyclerView(activity.get()).apply {
                layoutManager = GridLayoutManager(context, 3)
                this.adapter = adapter
                itemAnimator = null
            }
            activity.get().setContentView(grid)
            adapter.submit(
                (1L..1000L).map { StickerItem(id = it, filePath = "/missing/$it.png") },
                OverlayStickerAdapter.RenderOptions(180, 4, 0f, false, 1f)
            )
            val exact = View.MeasureSpec.EXACTLY
            grid.measure(View.MeasureSpec.makeMeasureSpec(600, exact), View.MeasureSpec.makeMeasureSpec(400, exact))
            grid.layout(0, 0, 600, 400)
            assertEquals(1000, adapter.itemCount)
            assertTrue("Only the visible rows should own views", grid.childCount in 1..18)
            grid.scrollToPosition(999)
            grid.measure(View.MeasureSpec.makeMeasureSpec(600, exact), View.MeasureSpec.makeMeasureSpec(400, exact))
            grid.layout(0, 0, 600, 400)
            assertNotNull("Last sticker must remain reachable", grid.findViewHolderForAdapterPosition(999))
            assertTrue(grid.childCount <= 18)
        } finally {
            adapter.cancelRequests()
            scope.cancel()
            activity.pause().stop().destroy()
        }
    }
}
