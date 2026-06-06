package uz.yalla.sdk.android.maps.libre.platform

import org.maplibre.compose.map.GestureOptions

fun getPlatformGestures(): GestureOptions = GestureOptions(
    isScrollEnabled = true,
    isZoomEnabled = true,
    isQuickZoomEnabled = true,
    isRotateEnabled = false,
    isTiltEnabled = false,
    isDoubleTapEnabled = true
)

fun getDisabledGestures(): GestureOptions = GestureOptions(
    isScrollEnabled = false,
    isZoomEnabled = false,
    isQuickZoomEnabled = false,
    isRotateEnabled = false,
    isTiltEnabled = false,
    isDoubleTapEnabled = false
)
