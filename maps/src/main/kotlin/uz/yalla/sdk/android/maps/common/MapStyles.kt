package uz.yalla.sdk.android.maps.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal object MapColors {
    val Primary = Color(0xFF562DF8)

    val FinishMarker = Color(0xFFFF3B30)

    val IntermediateMarker = Color(0xFFAEAEB2)

    val BadgeBackground = Color(0xFF1C1C1E)

    val BadgeText = Color.White

    val MarkerFill = Color.White
}

internal object MapDimens {
    val MarkerSize = 22.dp

    val UserLocationSize = 16.dp

    val MarkerBorderWidth = 6.dp

    val BadgeHeight = 28.dp

    val BadgePadding = 12.dp

    val RouteWidth = 4.dp

    const val DEFAULT_ACCURACY_METERS = 50.0

    const val ACCURACY_LAYER_SIZE = 256f
}

internal enum class LocationType {
    START,

    POINT,

    FINISH
}
