package uz.yalla.sdk.android.components.primitives.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.yalla.sdk.android.design.theme.System
import uz.yalla.sdk.android.design.theme.YallaTheme

@Composable
fun DragButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .size(
                width = 36.dp,
                height = 5.dp
            )
            .background(
                color = System.color.text.subtle,
                shape = CircleShape
            )
            .clickable(
                enabled = true,
                indication = null,
                interactionSource = null,
                onClickLabel = null,
                role = Role.Button,
                onClick = onClick
            )
    )
}

@Preview
@Composable
private fun Preview() = YallaTheme {
    DragButton {
    }
}
