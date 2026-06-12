package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3Api::class)
class SheetDismissPolicyTest {
    @Test
    fun hideIsBlockedWhileVisibleAndNotDismissable() {
        assertTrue(shouldBlockHide(SheetValue.Hidden, dismissEnabled = false, isVisible = true))
    }

    @Test
    fun hideIsAllowedForDismissableSheets() {
        assertFalse(shouldBlockHide(SheetValue.Hidden, dismissEnabled = true, isVisible = true))
    }

    @Test
    fun hideIsAllowedOnceNoLongerVisible() {
        assertFalse(shouldBlockHide(SheetValue.Hidden, dismissEnabled = false, isVisible = false))
    }

    @Test
    fun expandIsNeverBlocked() {
        assertFalse(shouldBlockHide(SheetValue.Expanded, dismissEnabled = false, isVisible = true))
    }

    @Test
    fun partialExpansionIsNeverBlocked() {
        assertFalse(shouldBlockHide(SheetValue.PartiallyExpanded, dismissEnabled = false, isVisible = true))
    }
}
