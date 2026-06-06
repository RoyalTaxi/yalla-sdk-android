package uz.yalla.sdk.android.maps.model

data class LatLngBounds(
    val southwest: LatLng,
    val northeast: LatLng
) {
    val center: LatLng
        get() = LatLng(
            latitude = (southwest.latitude + northeast.latitude) / 2,
            longitude = (southwest.longitude + northeast.longitude) / 2
        )

    class Builder {
        private var southWestLat = Double.MAX_VALUE
        private var southWestLng = Double.MAX_VALUE
        private var northEastLat = -Double.MAX_VALUE
        private var northEastLng = -Double.MAX_VALUE

        fun include(point: LatLng): Builder {
            southWestLat = minOf(southWestLat, point.latitude)
            southWestLng = minOf(southWestLng, point.longitude)
            northEastLat = maxOf(northEastLat, point.latitude)
            northEastLng = maxOf(northEastLng, point.longitude)
            return this
        }

        fun build(): LatLngBounds {
            require(southWestLat != Double.MAX_VALUE) { "No points included" }
            return LatLngBounds(
                southwest = LatLng(southWestLat, southWestLng),
                northeast = LatLng(northEastLat, northEastLng)
            )
        }
    }
}
