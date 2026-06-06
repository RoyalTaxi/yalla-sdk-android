package uz.yalla.sdk.android.components.primitives.indicator

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.tooling.preview.Preview
import uz.yalla.sdk.android.design.theme.YallaTheme

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    if (color.isSpecified) {
        CircularProgressIndicator(modifier = modifier, color = color)
    } else {
        CircularProgressIndicator(modifier = modifier)
    }
}

@Preview
@Composable
private fun Preview() = YallaTheme {
    LoadingIndicator()
}
