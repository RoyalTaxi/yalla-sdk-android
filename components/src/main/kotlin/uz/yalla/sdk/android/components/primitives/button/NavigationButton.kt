package uz.yalla.sdk.android.components.primitives.button

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.yalla.sdk.android.design.theme.System
import uz.yalla.sdk.android.design.theme.YallaTheme

@Composable
fun NavigationButton(
    onClick: () -> Unit
) {
    IconButton(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = System.color.icon.base,
            containerColor = System.color.background.secondary,
            disabledContentColor = System.color.icon.subtle,
            disabledContainerColor = System.color.background.base
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview
@Composable
private fun Preview() = YallaTheme {
    NavigationButton {
    }
}
