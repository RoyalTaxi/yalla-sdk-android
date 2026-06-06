package uz.yalla.sdk.android.maps.model

data class CameraPosition(
    val target: LatLng,
    val zoom: Float = 10f,
    val bearing: Float = 0f,
    val tilt: Float = 0f
) {
    companion object {
        fun fromLatLngZoom(
            target: LatLng,
            zoom: Float
        ): CameraPosition = CameraPosition(target = target, zoom = zoom)
    }
}
