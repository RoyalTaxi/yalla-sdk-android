package uz.yalla.sdk.android.bridges.media

import kotlin.test.Test
import kotlin.test.assertEquals

class PickModeTest {
    @Test
    fun limitOfOneIsSinglePick() {
        assertEquals(PickMode.Single, resolvePickMode(1))
    }

    @Test
    fun limitAboveOneIsMultiPickWithThatMax() {
        assertEquals(PickMode.Multiple(5), resolvePickMode(5))
    }

    @Test
    fun zeroLimitIsSinglePickNotUnboundedMulti() {
        // Regression: a limit of 0 previously fell through to an unbounded PickMultipleVisualMedia().
        assertEquals(PickMode.Single, resolvePickMode(0))
    }

    @Test
    fun negativeLimitIsSinglePickNotUnboundedMulti() {
        assertEquals(PickMode.Single, resolvePickMode(-3))
    }

    @Test
    fun limitOfTwoIsTheSmallestMultiPick() {
        assertEquals(PickMode.Multiple(2), resolvePickMode(2))
    }
}
