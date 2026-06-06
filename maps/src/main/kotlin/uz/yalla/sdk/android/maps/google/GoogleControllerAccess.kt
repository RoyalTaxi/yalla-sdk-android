package uz.yalla.sdk.android.maps.google

import uz.yalla.maps.api.MapController
import uz.yalla.maps.provider.SwitchingMapController

internal fun MapController.requireGoogleController(): GoogleMapController = when (this) {
    is GoogleMapController -> this
    is SwitchingMapController -> googleController as GoogleMapController
    else -> error("Google map requires GoogleMapController")
}
