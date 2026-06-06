package uz.yalla.sdk.android.maps.model

data class LatLng(
    val latitude: Double,
    val longitude: Double
) {
    fun isValid(): Boolean = latitude in -90.0..90.0 && longitude in -180.0..180.0
}
