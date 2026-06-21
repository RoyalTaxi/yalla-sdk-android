package uz.yalla.sdk.android.bridges.feedback

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SnackbarSwipeDismissTest {
    private val width = 1000f
    private val velocityThreshold = 1500f

    @Test
    fun smallDragWithLowVelocityIsKept() {
        // 30% of width, well under the 40% distance threshold and under the velocity threshold.
        assertFalse(shouldDismissOnSwipe(offsetX = 300f, itemWidthPx = width, velocity = 100f, velocityThresholdPx = velocityThreshold))
    }

    @Test
    fun dragPastFortyPercentDismisses() {
        assertTrue(shouldDismissOnSwipe(offsetX = 401f, itemWidthPx = width, velocity = 0f, velocityThresholdPx = velocityThreshold))
    }

    @Test
    fun exactlyFortyPercentIsNotEnough() {
        assertFalse(shouldDismissOnSwipe(offsetX = 400f, itemWidthPx = width, velocity = 0f, velocityThresholdPx = velocityThreshold))
    }

    @Test
    fun fastFlingDismissesEvenWhenBarelyMoved() {
        assertTrue(shouldDismissOnSwipe(offsetX = 10f, itemWidthPx = width, velocity = 2000f, velocityThresholdPx = velocityThreshold))
    }

    @Test
    fun dismissTriggersInEitherDragDirection() {
        assertTrue(shouldDismissOnSwipe(offsetX = -500f, itemWidthPx = width, velocity = 0f, velocityThresholdPx = velocityThreshold))
        assertTrue(shouldDismissOnSwipe(offsetX = 0f, itemWidthPx = width, velocity = -2000f, velocityThresholdPx = velocityThreshold))
    }
}
