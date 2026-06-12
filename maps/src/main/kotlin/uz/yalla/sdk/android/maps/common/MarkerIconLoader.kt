package uz.yalla.sdk.android.maps.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import uz.yalla.maps.api.model.MapMarkerIcon

internal object MarkerIconLoader {

    private const val MAX_BITMAP_ENTRIES = 64
    private const val USER_LOCATION_SIZE_DP = 16f
    private const val USER_LOCATION_STROKE_PX = 2f

    private val userLocationGradientStart = 0xFF3400FF.toInt()
    private val userLocationGradientEnd = 0xFF886BFF.toInt()

    private val bitmapCache = LruCache<MapMarkerIcon, Bitmap>(MAX_BITMAP_ENTRIES)

    private val descriptorCache = LruCache<MapMarkerIcon, BitmapDescriptor>(MAX_BITMAP_ENTRIES)

    private var userLocationBitmap: Bitmap? = null

    private var userLocationDescriptor: BitmapDescriptor? = null

    fun loadBitmap(context: Context, icon: MapMarkerIcon): Bitmap? {
        bitmapCache.get(icon)?.let { return it }
        val bitmap = when (icon) {
            is MapMarkerIcon.Resource -> resourceBitmap(context, icon.name)
            is MapMarkerIcon.Bytes -> BitmapFactory.decodeByteArray(icon.data, 0, icon.data.size)
            is MapMarkerIcon.Pin -> pinBitmap(context, icon)
            is MapMarkerIcon.Dot -> dotBitmap(context, icon)
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

    fun loadUserLocationBitmap(context: Context): Bitmap {
        userLocationBitmap?.let { return it }
        val density = context.resources.displayMetrics.density
        val size = (USER_LOCATION_SIZE_DP * density).toInt().coerceAtLeast(1)
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        val radius = (size - USER_LOCATION_STROKE_PX) / 2f
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, size.toFloat(), userLocationGradientStart, userLocationGradientEnd, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(center, center, radius, fillPaint)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = USER_LOCATION_STROKE_PX
            shader = LinearGradient(0f, 0f, 0f, size.toFloat(), userLocationGradientEnd, userLocationGradientStart, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(center, center, radius, strokePaint)
        userLocationBitmap = bitmap
        return bitmap
    }

    fun loadUserLocationDescriptor(context: Context): BitmapDescriptor {
        userLocationDescriptor?.let { return it }
        val descriptor = BitmapDescriptorFactory.fromBitmap(loadUserLocationBitmap(context))
        userLocationDescriptor = descriptor
        return descriptor
    }

    private fun dotBitmap(context: Context, icon: MapMarkerIcon.Dot): Bitmap {
        val density = context.resources.displayMetrics.density
        val strokePx = icon.strokeWidthDp * density
        val size = ((icon.diameterDp + icon.strokeWidthDp) * density).toInt().coerceAtLeast(1)
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val center = size / 2f
        val radius = (size - strokePx) / 2f
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = icon.fillColorArgb }
        canvas.drawCircle(center, center, radius, fillPaint)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokePx
            color = icon.strokeColorArgb
        }
        canvas.drawCircle(center, center, radius, strokePaint)
        return bitmap
    }

    private fun pinBitmap(context: Context, icon: MapMarkerIcon.Pin): Bitmap {
        val metrics = context.resources.displayMetrics
        val density = metrics.density
        val markerSize = 22f * density
        val ringWidth = 6f * density
        val badgeHeight = 28f * density
        val badgePadding = 12f * density
        val label = icon.label?.takeIf { it.isNotBlank() }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 14f * metrics.scaledDensity
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val badgeWidth = if (label != null) textPaint.measureText(label) + badgePadding * 2 else 0f
        val width = maxOf(markerSize, badgeWidth).toInt().coerceAtLeast(1)
        val height = (if (label != null) badgeHeight * 2 + markerSize else markerSize).toInt().coerceAtLeast(1)

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        val centerX = width / 2f
        val circleTop = if (label != null) badgeHeight else 0f
        val circleCenterY = circleTop + markerSize / 2f
        val radius = markerSize / 2f

        if (label != null) {
            val badgeLeft = centerX - badgeWidth / 2f
            val badgeRect = RectF(badgeLeft, 0f, badgeLeft + badgeWidth, badgeHeight)
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1C1C1E.toInt() }
            canvas.drawRoundRect(badgeRect, badgeHeight / 2f, badgeHeight / 2f, badgePaint)
            val textY = badgeRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, centerX, textY, textPaint)
        }

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = icon.colorArgb }
        canvas.drawCircle(centerX, circleCenterY, radius, ringPaint)
        val innerRadius = (radius - ringWidth).coerceAtLeast(0f)
        if (innerRadius > 0f) {
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
            canvas.drawCircle(centerX, circleCenterY, innerRadius, fillPaint)
        }

        return bitmap
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
