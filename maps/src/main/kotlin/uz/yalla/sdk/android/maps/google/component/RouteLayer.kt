package uz.yalla.sdk.android.maps.google.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import uz.yalla.core.geo.GeoPoint
import uz.yalla.sdk.android.maps.compose.Polyline
import uz.yalla.sdk.android.maps.model.Cap
import uz.yalla.sdk.android.maps.model.JointType
import uz.yalla.sdk.android.maps.common.MapColors
import uz.yalla.sdk.android.maps.common.MapDimens
import uz.yalla.sdk.android.maps.google.toLatLng

@Composable
internal fun RouteLayer(route: List<GeoPoint>) {
    if (route.size < 2) return

    val widthPx = with(LocalDensity.current) { MapDimens.RouteWidth.toPx() }

    val points = remember(route) { route.map { it.toLatLng() } }
    Polyline(
        points = points,
        color = MapColors.Primary,
        width = widthPx,
        jointType = JointType.Round,
        startCap = Cap.Round,
        endCap = Cap.Round
    )
}
