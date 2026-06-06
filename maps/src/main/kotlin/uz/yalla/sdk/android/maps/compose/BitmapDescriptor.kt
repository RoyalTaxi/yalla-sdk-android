package uz.yalla.sdk.android.maps.compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.nio.ByteBuffer
import com.google.android.gms.maps.model.BitmapDescriptor as GoogleBitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory

class BitmapDescriptor(
    val googleBitmapDescriptor: GoogleBitmapDescriptor
)

object BitmapDescriptorFactory {
    fun fromBytes(
        bytes: ByteArray,
        width: Int,
        height: Int
    ): BitmapDescriptor {
        require(bytes.size == width * height * 4) { "Invalid byte array size" }
        val rgbaBytes = ByteArray(bytes.size)
        for (i in bytes.indices step 4) {
            rgbaBytes[i] = bytes[i + 1]
            rgbaBytes[i + 1] = bytes[i + 2]
            rgbaBytes[i + 2] = bytes[i + 3]
            rgbaBytes[i + 3] = bytes[i]
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgbaBytes))
        return BitmapDescriptor(GoogleBitmapDescriptorFactory.fromBitmap(bitmap))
    }

    fun fromEncodedImage(data: ByteArray): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size) ?: error("Failed to decode image")
        return BitmapDescriptor(GoogleBitmapDescriptorFactory.fromBitmap(bitmap))
    }
}
