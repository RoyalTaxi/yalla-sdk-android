package uz.yalla.sdk.android.maps.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import uz.yalla.maps.api.model.MapMarkerIcon

internal object MarkerIconLoader {

    private const val MAX_BITMAP_ENTRIES = 64

    private val bitmapCache = LruCache<MapMarkerIcon, Bitmap>(MAX_BITMAP_ENTRIES)

    private val descriptorCache = LruCache<MapMarkerIcon, BitmapDescriptor>(MAX_BITMAP_ENTRIES)

    fun loadBitmap(context: Context, icon: MapMarkerIcon): Bitmap? {
        bitmapCache.get(icon)?.let { return it }
        val bitmap = when (icon) {
            is MapMarkerIcon.Resource -> resourceBitmap(context, icon.name)
            is MapMarkerIcon.Bytes -> BitmapFactory.decodeByteArray(icon.data, 0, icon.data.size)
        } ?: return null
        bitmapCache.put(icon, bitmap)
        return bitmap
    }

    fun loadGmsDescriptor(context: Context, icon: MapMarkerIcon): BitmapDescriptor? {
        descriptorCache.get(icon)?.let { return it }
        val bitmap = loadBitmap(context, icon) ?: return null
        val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        descriptorCache.put(icon, descriptor)
        return descriptor
    }

    private fun resourceBitmap(context: Context, name: String): Bitmap? {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id == 0) return null
        val drawable = ContextCompat.getDrawable(context, id) ?: return null
        if (drawable is BitmapDrawable) return drawable.bitmap
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: return null
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: return null
        val bitmap = createBitmap(width, height)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}
