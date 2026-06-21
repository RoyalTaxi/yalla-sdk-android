package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import uz.yalla.components.composites.item.ActionableItemModel
import uz.yalla.components.composites.item.SelectableItemModel
import uz.yalla.components.config.composites.SheetFactory
import uz.yalla.design.image.ThemedImage

/**
 * Android implementation of the common `SheetFactory`. Each `*Content` method renders one of the
 * native bottom-sheet variants (shell, content, confirmation, selection, action, date picker,
 * verification) backed by Material 3 `ModalBottomSheet`.
 */
class YallaSheetFactory : SheetFactory {
    @Composable
    override fun ShellContent(
        isVisible: Boolean,
        onDismissRequest: () -> Unit,
        modifier: Modifier,
        fullHeight: Boolean,
        sheetSwipeEnabled: Boolean,
        content: @Composable (padding: PaddingValues) -> Unit
    ) {
        Sheet(
            isVisible = isVisible,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            sheetSwipeEnabled = sheetSwipeEnabled,
            title = null,
            onClose = null,
            footer = null,
            fullHeight = fullHeight,
            imeAsContentPadding = true,
            content = content
        )
    }

    @Composable
    override fun ContentContent(
        isVisible: Boolean,
        onDismissRequest: () -> Unit,
        modifier: Modifier,
        title: String?,
        onClose: (() -> Unit)?,
        fullHeight: Boolean,
        sheetSwipeEnabled: Boolean,
        onFullyExpanded: (() -> Unit)?,
        content: @Composable (padding: PaddingValues) -> Unit
    ) {
        ContentSheet(
            isVisible = isVisible,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            title = title,
            onClose = onClose,
            fullHeight = fullHeight,
            sheetSwipeEnabled = sheetSwipeEnabled,
            onFullyExpanded = onFullyExpanded,
            content = content
        )
    }

    @Composable
    override fun ConfirmationContent(
        isVisible: Boolean,
        image: ThemedImage,
        title: String,
        description: String,
        actionText: String,
        onAction: () -> Unit,
        onDismissRequest: () -> Unit,
        dismissEnabled: Boolean,
        header: String?
    ) {
        ConfirmationSheet(
            isVisible = isVisible,
            image = image,
            title = title,
            description = description,
            actionText = actionText,
            onAction = onAction,
            onDismissRequest = onDismissRequest,
            dismissEnabled = dismissEnabled,
            header = header
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
        alphanumeric: Boolean,
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
            alphanumeric = alphanumeric,
            onCodeComplete = onCodeComplete,
            onDismissRequest = onDismissRequest,
            dismissEnabled = dismissEnabled
        )
    }
}
