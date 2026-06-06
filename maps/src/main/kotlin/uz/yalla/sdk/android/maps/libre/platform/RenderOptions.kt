package uz.yalla.sdk.android.maps.libre.platform

import org.maplibre.compose.map.RenderOptions

fun getPlatformRenderOptions(): RenderOptions = RenderOptions(
    renderMode = RenderOptions.RenderMode.SurfaceView
)
