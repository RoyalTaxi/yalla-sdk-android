package uz.yalla.sdk.android.maps.common

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection

internal data class PaddingPx(val left: Int, val top: Int, val right: Int, val bottom: Int)

internal fun PaddingValues.toPaddingPx(context: Context): PaddingPx {
    val density = context.resources.displayMetrics.density
    return PaddingPx(
        left = (calculateLeftPadding(LayoutDirection.Ltr).value * density).toInt(),
        top = (calculateTopPadding().value * density).toInt(),
        right = (calculateRightPadding(LayoutDirection.Ltr).value * density).toInt(),
        bottom = (calculateBottomPadding().value * density).toInt()
    )
}
