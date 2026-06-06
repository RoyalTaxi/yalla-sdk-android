package uz.yalla.sdk.android.maps.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory

internal fun ImageBitmap.toBitmapDescriptor(): BitmapDescriptor = BitmapDescriptor(GoogleBitmapDescriptorFactory.fromBitmap(asAndroidBitmap()))
