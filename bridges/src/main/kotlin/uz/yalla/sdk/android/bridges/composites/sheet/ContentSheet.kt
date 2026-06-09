package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ContentSheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    onClose: (() -> Unit)? = null,
    fullHeight: Boolean = false,
    sheetSwipeEnabled: Boolean = true,
    content: @Composable (padding: PaddingValues) -> Unit
) {
    Sheet(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetSwipeEnabled = sheetSwipeEnabled,
        title = title,
        onClose = onClose,
        footer = null,
        fullHeight = fullHeight,
        content = content
    )
}
