package uz.yalla.sdk.android.components.primitives.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uz.yalla.sdk.android.design.theme.System

@Composable
fun IconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    circle: Boolean = true,
    iconColor: Color = System.color.icon.base,
    containerColor: Color = System.color.background.secondary,
    borderColor: Color = Color.Unspecified
) {
    val context = LocalContext.current
    val iconResId = remember(icon) {
        context.resources.getIdentifier(icon, "drawable", context.packageName)
    }

    Surface(
        shape = if (circle) CircleShape else RoundedCornerShape(12.dp),
        color = containerColor,
        onClick = onClick,
        border = if (borderColor.isSpecified) BorderStroke(1.dp, borderColor) else null,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .padding(10.dp)
                .size(24.dp)
        )
    }
}
