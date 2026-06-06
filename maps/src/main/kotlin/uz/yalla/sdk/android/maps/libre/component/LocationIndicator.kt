package uz.yalla.sdk.android.maps.libre.component

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import io.github.dellisd.spatialk.geojson.dsl.featureCollection
import io.github.dellisd.spatialk.geojson.dsl.point
import kotlinx.coroutines.flow.collectLatest
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import uz.yalla.core.geo.GeoPoint
import uz.yalla.sdk.android.maps.common.MapColors
import uz.yalla.sdk.android.maps.common.MapDimens
import uz.yalla.sdk.android.maps.common.UserLocationPainter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

@Composable
internal fun LocationIndicator(
    location: GeoPoint?,
    cameraState: CameraState,
    accuracyMeters: Double = MapDimens.DEFAULT_ACCURACY_METERS
) {
    location ?: return

    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            featureCollection {
                feature(geometry = point(longitude = location.lng, latitude = location.lat))
            }
        )
    )

    AccuracyLayer(
        source = source,
        accuracyMeters = accuracyMeters,
        latitude = location.lat,
        cameraState = cameraState
    )

    SymbolLayer(
        id = "user-location",
        source = source,
        iconImage = image(UserLocationPainter),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true)
    )
}

@Composable
private fun AccuracyLayer(
    source: org.maplibre.compose.sources.GeoJsonSource,
    accuracyMeters: Double,
    latitude: Double,
    cameraState: CameraState
) {
    val iconSize = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        snapshotFlow { cameraState.position.zoom }.collectLatest { zoom ->
            val metersPerPixel = 156543.03392 * cos(latitude * PI / 180.0) / 2.0.pow(zoom)
            val radiusPixels = accuracyMeters / metersPerPixel
            val targetSize = (radiusPixels / (MapDimens.ACCURACY_LAYER_SIZE / 2)).toFloat().coerceIn(0.1f, 20f)
            iconSize.snapTo(targetSize)
        }
    }

    SymbolLayer(
        id = "user-location-accuracy",
        source = source,
        iconImage = image(AccuracyPainter),
        iconSize = const(iconSize.value),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true)
    )
}

private object AccuracyPainter : Painter() {
    override val intrinsicSize = Size(MapDimens.ACCURACY_LAYER_SIZE, MapDimens.ACCURACY_LAYER_SIZE)

    override fun DrawScope.onDraw() {
        val center = Offset(MapDimens.ACCURACY_LAYER_SIZE / 2, MapDimens.ACCURACY_LAYER_SIZE / 2)
        val radius = MapDimens.ACCURACY_LAYER_SIZE / 2

        drawCircle(
            brush = Brush.radialGradient(
                radius = radius,
                center = center,
                colors = listOf(
                    MapColors.Primary.copy(alpha = 0.1f),
                    MapColors.Primary.copy(alpha = 0.2f)
                )
            ),
            radius = radius,
            center = center
        )
    }
}
