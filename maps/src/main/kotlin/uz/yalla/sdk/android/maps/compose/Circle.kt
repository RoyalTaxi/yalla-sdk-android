package uz.yalla.sdk.android.maps.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import uz.yalla.sdk.android.maps.model.LatLng
import com.google.android.gms.maps.model.Circle as GoogleCircle
import com.google.maps.android.compose.Circle as AndroidCircle

class Circle(
    val googleCircle: GoogleCircle
) {
    val center: LatLng = LatLng(googleCircle.center.latitude, googleCircle.center.longitude)
    val radius: Double = googleCircle.radius
}

@Composable
@GoogleMapComposable
fun Circle(
    center: LatLng,
    radius: Double,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float
) {
    AndroidCircle(
        center = center.toGoogleLatLng(),
        radius = radius,
        fillColor = fillColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth
    )
}
