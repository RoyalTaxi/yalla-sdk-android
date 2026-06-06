package uz.yalla.sdk.android.maps.model

import androidx.compose.runtime.Immutable

@Immutable
data class MapProperties(
    val isBuildingEnabled: Boolean = false,
    val isIndoorEnabled: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val mapType: MapType = MapType.NORMAL,
    val minZoomPreference: Float = 3.0f,
    val maxZoomPreference: Float = 21.0f
)
