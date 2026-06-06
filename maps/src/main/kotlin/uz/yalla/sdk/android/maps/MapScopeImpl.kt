package uz.yalla.sdk.android.maps

import uz.yalla.maps.api.MapProviderKind
import uz.yalla.maps.api.MapScope
import uz.yalla.sdk.android.maps.compose.CameraPositionState

class MapScopeImpl(
    val cameraState: CameraPositionState,
    override val provider: MapProviderKind
) : MapScope
