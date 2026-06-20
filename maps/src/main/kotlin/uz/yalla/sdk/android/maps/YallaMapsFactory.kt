package uz.yalla.sdk.android.maps

import android.app.Application
import uz.yalla.maps.api.MapController
import uz.yalla.maps.config.MapConstants
import uz.yalla.maps.config.MapFactory
import uz.yalla.sdk.android.maps.google.AndroidGoogleMapController
import uz.yalla.sdk.android.maps.libre.AndroidLibreMapController

/**
 * Android [MapFactory] producing the two native map controllers.
 *
 * @param application used only for its application [android.content.Context]; the factory holds no
 *   activity reference.
 * @param libreStyleUrl initial MapLibre style URL for [createLibreController]; defaults to the CARTO
 *   positron (light) base style. The renderer swaps to the dark variant via
 *   [MapController.setStyle]; this is only the seed used before the first `setStyle` call.
 */
class YallaMapsFactory(
    application: Application,
    private val libreStyleUrl: String = MapConstants.LIGHT_STYLE_URL
) : MapFactory {

    private val applicationContext = application.applicationContext

    /** A controller backed by the Google Maps SDK. */
    override fun createGoogleController(): MapController =
        AndroidGoogleMapController(applicationContext)

    /** A controller backed by MapLibre GL, seeded with [libreStyleUrl]. */
    override fun createLibreController(): MapController =
        AndroidLibreMapController(applicationContext, libreStyleUrl)
}
