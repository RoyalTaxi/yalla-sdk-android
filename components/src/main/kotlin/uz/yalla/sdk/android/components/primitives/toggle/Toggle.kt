package uz.yalla.sdk.android.components.primitives.toggle

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import uz.yalla.sdk.android.design.theme.System
import uz.yalla.sdk.android.design.theme.YallaTheme

@Composable
fun Toggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkedThumbColor: Color = System.color.icon.white,
    checkedTrackColor: Color = System.color.background.brand,
    checkedBorderColor: Color = System.color.background.brand,
    uncheckedThumbColor: Color = System.color.icon.white,
    uncheckedTrackColor: Color = System.color.icon.subtle,
    uncheckedBorderColor: Color = System.color.icon.subtle,
    disabledCheckedThumbColor: Color = System.color.icon.white.copy(alpha = 0.7f),
    disabledCheckedTrackColor: Color = System.color.background.brand.copy(alpha = 0.5f),
    disabledCheckedBorderColor: Color = System.color.background.brand.copy(alpha = 0.5f),
    disabledUncheckedThumbColor: Color = System.color.icon.white.copy(alpha = 0.7f),
    disabledUncheckedTrackColor: Color = System.color.icon.subtle.copy(alpha = 0.5f),
    disabledUncheckedBorderColor: Color = System.color.icon.subtle.copy(alpha = 0.5f)
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = checkedThumbColor,
            checkedTrackColor = checkedTrackColor,
            checkedBorderColor = checkedBorderColor,
            uncheckedThumbColor = uncheckedThumbColor,
            uncheckedTrackColor = uncheckedTrackColor,
            uncheckedBorderColor = uncheckedBorderColor,
            disabledCheckedThumbColor = disabledCheckedThumbColor,
            disabledCheckedTrackColor = disabledCheckedTrackColor,
            disabledCheckedBorderColor = disabledCheckedBorderColor,
            disabledUncheckedThumbColor = disabledUncheckedThumbColor,
            disabledUncheckedTrackColor = disabledUncheckedTrackColor,
            disabledUncheckedBorderColor = disabledUncheckedBorderColor
        )
    )
}

@Preview
@Composable
private fun Preview() = YallaTheme {
    Toggle(checked = true, onCheckedChange = {})
}
