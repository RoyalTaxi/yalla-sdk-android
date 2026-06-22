package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import uz.yalla.components.composites.item.SelectableItem
import uz.yalla.components.composites.item.SelectableItemDefaults
import uz.yalla.components.composites.item.SelectableItemModel
import uz.yalla.components.resource.ComponentImage
import uz.yalla.components.resource.asImageVector
import uz.yalla.sdk.android.bridges.feedback.Haptics
import uz.yalla.sdk.android.design.theme.System

@Composable
internal fun SelectionSheet(
    isVisible: Boolean,
    title: String,
    items: List<SelectableItemModel>,
    selectedId: String?,
    onSelect: (id: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val listState = rememberLazyListState()
    val headerElevated by remember { derivedStateOf { listState.canScrollBackward } }
    val view = LocalView.current

    Sheet(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        title = title,
        onClose = onDismissRequest,
        headerElevated = headerElevated
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                SelectableItem(
                    text = item.text,
                    selected = item.id == selectedId,
                    leadingPainter = ComponentImage.from(item.icon)?.asImageVector()?.let { rememberVectorPainter(it) },
                    onClick = {
                        Haptics.selection(view)
                        onSelect(item.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SelectableItemDefaults.colors(
                        iconColor = if (item.tintIcon) System.color.icon.base else Color.Unspecified,
                        selectedIconColor = if (item.tintIcon) System.color.icon.base else Color.Unspecified
                    ),
                    dimens = SelectableItemDefaults.dimens(
                        iconSize = if (item.tintIcon) 24.dp else 34.dp
                    )
                )
            }
        }
    }
}
