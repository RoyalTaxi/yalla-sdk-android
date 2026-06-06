package uz.yalla.sdk.android.bridges.primitives.toggle

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import uz.yalla.components.config.primitives.ToggleFactory
import uz.yalla.sdk.android.components.primitives.toggle.Toggle

class YallaToggleFactory : ToggleFactory {
    @Composable
    override fun Content(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        enabled: Boolean,
        checkedThumbColor: Color,
        checkedTrackColor: Color,
        checkedBorderColor: Color,
        uncheckedThumbColor: Color,
        uncheckedTrackColor: Color,
        uncheckedBorderColor: Color,
        disabledCheckedThumbColor: Color,
        disabledCheckedTrackColor: Color,
        disabledCheckedBorderColor: Color,
        disabledUncheckedThumbColor: Color,
        disabledUncheckedTrackColor: Color,
        disabledUncheckedBorderColor: Color,
        modifier: Modifier
    ) {
        Toggle(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
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
            disabledUncheckedBorderColor = disabledUncheckedBorderColor,
            modifier = modifier
        )
    }
}
