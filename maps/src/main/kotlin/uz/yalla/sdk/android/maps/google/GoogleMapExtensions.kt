package uz.yalla.sdk.android.maps.google

import uz.yalla.core.geo.GeoPoint
import uz.yalla.sdk.android.maps.model.LatLng
import uz.yalla.sdk.android.maps.model.LatLngBounds

internal fun GeoPoint.toLatLng(): LatLng = LatLng(latitude = lat, longitude = lng)

internal fun LatLng.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

internal fun List<GeoPoint>.toLatLngBounds(): LatLngBounds? {
    if (isEmpty()) return null
    val builder = LatLngBounds.Builder()
    forEach { builder.include(it.toLatLng()) }
    return builder.build()
}
