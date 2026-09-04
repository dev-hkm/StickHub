package com.hkm.stickhub

import com.hkm.stickhub.data.cutout.CutoutModelInputPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CutoutModelInputPlanTest {
    @Test
    fun panoramicPhotoUsesPaddingAndMapsBackToTheEntireOriginalPhoto() {
        val plan = CutoutModelInputPlan.create(2048, 64)
        assertEquals(2048, plan.width)
        assertEquals(512, plan.height)
        assertEquals(224, plan.top)
        assertEquals(0f, plan.normalizedX(plan.left.toFloat()), 0.0001f)
        assertEquals(0f, plan.normalizedY(plan.top.toFloat()), 0.0001f)
        assertEquals(1f, plan.normalizedY((plan.top + plan.contentHeight).toFloat()), 0.0001f)
    }

    @Test
    fun portraitPaddingMapsItsCenterToOriginalCenter() {
        val plan = CutoutModelInputPlan.create(64, 2048)
        assertEquals(512, plan.width)
        assertEquals(2048, plan.height)
        assertEquals(224, plan.left)
        assertEquals(0.5f, plan.normalizedX(256f), 0.0001f)
        assertEquals(0.5f, plan.normalizedY(1024f), 0.0001f)
    }

    @Test
    fun extremeAndLargeInputsAlwaysFitTheBudget() {
        listOf(1 to 2048, 2048 to 1, 1 to 1, 8000 to 6000, 100 to 10).forEach { (width, height) ->
            val plan = CutoutModelInputPlan.create(width, height)
            assertTrue(plan.width in 512..2048)
            assertTrue(plan.height in 512..2048)
            assertTrue(plan.contentWidth > 0 && plan.contentHeight > 0)
            assertTrue(plan.width.toLong() * plan.height <= 2048L * 2048L)
        }
    }
}
