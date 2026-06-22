package uz.yalla.sdk.android.bridges.primitives.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import uz.yalla.components.config.primitives.IconButtonFactory
import uz.yalla.components.primitives.button.IconButtonShape
import uz.yalla.sdk.android.components.primitives.button.IconButton

class YallaIconButtonFactory : IconButtonFactory {
    @Composable
    override fun Content(
        icon: String,
        shape: IconButtonShape,
        iconColor: Color,
        containerColor: Color,
        borderColor: Color,
        onClick: () -> Unit,
        modifier: Modifier
    ) {
        IconButton(
            icon = icon,
            onClick = onClick,
            modifier = modifier,
            circle = shape == IconButtonShape.CIRCLE,
            iconColor = iconColor,
            containerColor = containerColor,
            borderColor = borderColor
        )
    }
}
