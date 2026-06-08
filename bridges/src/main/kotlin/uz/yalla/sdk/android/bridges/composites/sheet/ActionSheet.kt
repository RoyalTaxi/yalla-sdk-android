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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import uz.yalla.components.composites.item.ActionableItem
import uz.yalla.components.composites.item.ActionableItemDefaults
import uz.yalla.components.composites.item.ActionableItemModel
import uz.yalla.components.resource.asImageVector

@Composable
internal fun ActionSheet(
    isVisible: Boolean,
    title: String,
    items: List<ActionableItemModel>,
    onAction: (id: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val listState = rememberLazyListState()
    val headerElevated by remember { derivedStateOf { listState.canScrollBackward } }

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ActionableItem(
                    text = item.text,
                    painter = item.icon.asImageVector()?.let { rememberVectorPainter(it) },
                    trailingPainter = item.trailingIcon?.asImageVector()?.let { rememberVectorPainter(it) },
                    onClick = { onAction(item.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ActionableItemDefaults.colorsFor(item)
                )
            }
        }
    }
}
