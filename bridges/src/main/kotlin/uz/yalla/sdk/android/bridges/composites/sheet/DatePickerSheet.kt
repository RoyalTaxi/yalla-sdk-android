package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.darkokoa.datetimewheelpicker.WheelDatePicker
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import kotlinx.datetime.LocalDate
import uz.yalla.components.primitives.button.DoneButton
import uz.yalla.sdk.android.design.theme.System

private val DEFAULT_MIN_DATE = LocalDate(1900, 1, 1)
private val DEFAULT_MAX_DATE = LocalDate(2100, 12, 31)

@Composable
internal fun DatePickerSheet(
    isVisible: Boolean,
    startDate: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    title: String?,
    onSelect: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    dismissEnabled: Boolean
) {
    // Reset to the incoming startDate whenever it changes (e.g. once the saved birthday loads after
    // the initial default composition).
    var snappedDate by remember(startDate) { mutableStateOf(startDate) }

    Sheet(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        dismissEnabled = dismissEnabled,
        sheetSwipeEnabled = dismissEnabled,
        title = title,
        onClose = if (dismissEnabled) onDismissRequest else null,
        headerAction = {
            DoneButton(onClick = { onSelect(snappedDate) })
        }
    ) { padding ->
        // key(startDate) rebuilds the wheel when the start date changes so it scrolls to the new date
        // instead of holding the position it captured on first composition.
        key(startDate) {
            WheelDatePicker(
                startDate = startDate,
                minDate = minDate ?: DEFAULT_MIN_DATE,
                maxDate = maxDate ?: DEFAULT_MAX_DATE,
                size = DpSize(width = 360.dp, height = 280.dp),
                rowCount = 5,
                textStyle = System.font.title.base,
                textColor = System.color.text.base,
                onSnappedDate = { snappedDate = it },
                selectorProperties = WheelPickerDefaults.selectorProperties(false),
                modifier = Modifier
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding()
                    )
                    .padding(horizontal = 16.dp)
            )
        }
    }
}
