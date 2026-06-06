package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import uz.yalla.components.composites.item.ActionableItemModel
import uz.yalla.components.composites.item.SelectableItemModel
import uz.yalla.components.config.composites.SheetFactory

class YallaSheetFactory : SheetFactory {
    @Composable
    override fun ConfirmationContent(
        isVisible: Boolean,
        imageResource: String,
        title: String,
        description: String,
        actionText: String,
        onAction: () -> Unit,
        onDismissRequest: () -> Unit,
        dismissEnabled: Boolean
    ) {
        ConfirmationSheet(
            isVisible = isVisible,
            imageResource = imageResource,
            title = title,
            description = description,
            actionText = actionText,
            onAction = onAction,
            onDismissRequest = onDismissRequest,
            dismissEnabled = dismissEnabled
        )
    }

    @Composable
    override fun SelectionContent(
        isVisible: Boolean,
        title: String,
        items: List<SelectableItemModel>,
        selectedId: String?,
        onSelect: (id: String) -> Unit,
        onDismissRequest: () -> Unit
    ) {
        SelectionSheet(
            isVisible = isVisible,
            title = title,
            items = items,
            selectedId = selectedId,
            onSelect = onSelect,
            onDismissRequest = onDismissRequest
        )
    }

    @Composable
    override fun ActionContent(
        isVisible: Boolean,
        title: String,
        items: List<ActionableItemModel>,
        onAction: (id: String) -> Unit,
        onDismissRequest: () -> Unit
    ) {
        ActionSheet(
            isVisible = isVisible,
            title = title,
            items = items,
            onAction = onAction,
            onDismissRequest = onDismissRequest
        )
    }

    @Composable
    override fun DatePickerContent(
        isVisible: Boolean,
        startDate: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?,
        title: String?,
        onSelect: (LocalDate) -> Unit,
        onDismissRequest: () -> Unit,
        dismissEnabled: Boolean
    ) {
        DatePickerSheet(
            isVisible = isVisible,
            startDate = startDate,
            minDate = minDate,
            maxDate = maxDate,
            title = title,
            onSelect = onSelect,
            onDismissRequest = onDismissRequest,
            dismissEnabled = dismissEnabled
        )
    }

    @Composable
    override fun VerificationContent(
        isVisible: Boolean,
        code: String,
        onCodeChange: (String) -> Unit,
        codeLength: Int,
        headline: String,
        description: String,
        confirmText: String,
        onConfirm: () -> Unit,
        resendText: String,
        onResend: () -> Unit,
        onDismissRequest: () -> Unit,
        title: String?,
        isError: Boolean,
        isLoading: Boolean,
        resendEnabled: Boolean,
        onCodeComplete: (String) -> Unit,
        dismissEnabled: Boolean
    ) {
        VerificationSheet(
            isVisible = isVisible,
            code = code,
            onCodeChange = onCodeChange,
            codeLength = codeLength,
            headline = headline,
            description = description,
            confirmText = confirmText,
            onConfirm = onConfirm,
            resendText = resendText,
            onResend = onResend,
            title = title,
            isError = isError,
            isLoading = isLoading,
            resendEnabled = resendEnabled,
            onCodeComplete = onCodeComplete,
            onDismissRequest = onDismissRequest,
            dismissEnabled = dismissEnabled
        )
    }
}
