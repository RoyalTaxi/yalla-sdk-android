package uz.yalla.sdk.android.design.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import uz.yalla.sdk.android.design.theme.LocalIsDark

@Composable
fun themedPainter(image: ThemedImage): Painter {
    val resource = if (LocalIsDark.current) image.dark else image.light
    return painterResource(resource)
}
