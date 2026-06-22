package uz.yalla.sdk.android.maps

import android.app.Application
import uz.yalla.maps.api.MapController
import uz.yalla.maps.api.model.MapStyle
import uz.yalla.maps.config.MapFactory
import uz.yalla.sdk.android.maps.google.AndroidGoogleMapController
import uz.yalla.sdk.android.maps.libre.AndroidLibreMapController

class YallaMapsFactory(
    application: Application,
    private val libreStyleUrl: String = MapStyle.CARTO.lightUrl
) : MapFactory {

    private val applicationContext = application.applicationContext

    override fun createGoogleController(): MapController =
        AndroidGoogleMapController(applicationContext)

    override fun createLibreController(): MapController =
        AndroidLibreMapController(applicationContext, libreStyleUrl)
}
