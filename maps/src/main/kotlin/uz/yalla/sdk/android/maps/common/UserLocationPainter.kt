package uz.yalla.sdk.android.maps.common

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter

internal object UserLocationPainter : Painter() {
    private const val SIZE = 48f

    private const val STROKE_WIDTH = 2f

    private val gradientStart = Color(0xFF3400FF)
    private val gradientEnd = Color(0xFF886BFF)

    override val intrinsicSize = Size(SIZE, SIZE)

    override fun DrawScope.onDraw() {
        val drawCenter = this.center
        val drawSize = minOf(size.width, size.height)
        val radius = (drawSize - STROKE_WIDTH) / 2

        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(gradientStart, gradientEnd),
                startY = 0f,
                endY = size.height
            ),
            radius = radius,
            center = drawCenter
        )

        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(gradientEnd, gradientStart),
                startY = 0f,
                endY = size.height
            ),
            radius = radius,
            center = drawCenter,
            style = Stroke(width = STROKE_WIDTH)
        )
    }
}
