package uz.yalla.sdk.android.bridges.primitives.indicator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import uz.yalla.components.config.primitives.LoadingIndicatorFactory
import uz.yalla.sdk.android.components.primitives.indicator.LoadingIndicator

/** Android implementation of the common `LoadingIndicatorFactory`, delegating to the design `LoadingIndicator`. */
class YallaLoadingIndicatorFactory : LoadingIndicatorFactory {
    @Composable
    override fun Content(
        color: Color,
        modifier: Modifier
    ) {
        LoadingIndicator(
            modifier = modifier,
            color = color
        )
    }
}
