package uz.yalla.sdk.android.maps.libre.platform

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import org.maplibre.compose.map.OrnamentOptions

fun getPlatformOrnamentOptions(padding: PaddingValues = PaddingValues()): OrnamentOptions = OrnamentOptions(
    padding = padding,
    isLogoEnabled = false,
    logoAlignment = Alignment.BottomStart,
    isAttributionEnabled = false,
    isCompassEnabled = false,
    isScaleBarEnabled = false
)
