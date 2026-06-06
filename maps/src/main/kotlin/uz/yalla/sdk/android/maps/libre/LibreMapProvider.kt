package uz.yalla.sdk.android.maps.libre

import uz.yalla.core.settings.MapKind
import uz.yalla.maps.api.ExtendedMap
import uz.yalla.maps.api.LiteMap
import uz.yalla.maps.api.MapController
import uz.yalla.maps.api.MapProvider
import uz.yalla.maps.api.StaticMap
import uz.yalla.maps.api.model.MapCapabilities
import uz.yalla.maps.api.model.MapStyle

class LibreMapProvider : MapProvider {
    override val type = MapKind.Libre
    override val capabilities = MapCapabilities.LIBRE
    override val style = MapStyle.CARTO

    override fun createLiteMap(): LiteMap = LibreLiteMap()

    override fun createExtendedMap(): ExtendedMap = LibreExtendedMap()

    override fun createStaticMap(): StaticMap = LibreStaticMap()

    override fun createController(): MapController = LibreMapController()
}
