package uz.yalla.sdk.android.maps.common

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection

internal data class PaddingPx(val left: Int, val top: Int, val right: Int, val bottom: Int)

/**
 * Resolves [PaddingValues] to physical left/top/right/bottom pixels for the native map's
 * `setPadding`/camera padding.
 *
 * Intentionally resolved as [LayoutDirection.Ltr]: the map surface (Google `MapView` / MapLibre
 * `MapView`) has no layout direction and addresses padding in physical left/right edges, so the
 * caller's start/end is mapped to physical left/right Ltr-style. Callers that need RTL-aware framing
 * should pass already-physical left/right padding.
 */
internal fun PaddingValues.toPaddingPx(context: Context): PaddingPx {
    val density = context.resources.displayMetrics.density
    return PaddingPx(
        left = (calculateLeftPadding(LayoutDirection.Ltr).value * density).toInt(),
        top = (calculateTopPadding().value * density).toInt(),
        right = (calculateRightPadding(LayoutDirection.Ltr).value * density).toInt(),
        bottom = (calculateBottomPadding().value * density).toInt()
    )
}
