package uz.yalla.sdk.android.bridges.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.abs
import kotlinx.coroutines.delay
import uz.yalla.sdk.android.design.theme.System
import uz.yalla.sdk.android.design.theme.YallaTheme

private val SwipeVelocityThreshold = 500.dp
private const val SwipeDismissFraction = 0.4f

private val SnackbarPositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset((windowSize.width - popupContentSize.width) / 2, 0)
}

@Composable
fun YallaSnackbarHost(modifier: Modifier = Modifier) {
    val items = SnackbarController.items
    if (items.isNotEmpty()) {
        Popup(
            popupPositionProvider = SnackbarPositionProvider,
            properties = PopupProperties(focusable = false, clippingEnabled = false)
        ) {
            SnackbarOverlay(items = items, modifier = modifier)
        }
    }
}

@Composable
internal fun SnackbarOverlay(
    items: SnapshotStateList<SnackbarItem>,
    modifier: Modifier = Modifier
) {
    YallaTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { item ->
                key(item.id) {
                    SnackbarItemRow(
                        item = item,
                        onDismiss = { items.remove(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SnackbarItemRow(
    item: SnackbarItem,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val view = LocalView.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    var itemWidthPx by remember { mutableFloatStateOf(1f) }
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    LaunchedEffect(item.id) {
        if (item.isError) Haptics.error(view) else Haptics.success(view)
        delay(if (item.isError) 5_000L else 3_000L)
        visibleState.targetState = false
    }

    LaunchedEffect(visibleState.currentState, visibleState.targetState) {
        if (!visibleState.targetState && !visibleState.currentState) {
            onDismiss()
        }
    }

    val requestDismiss = { visibleState.targetState = false }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite }
                .onSizeChanged { itemWidthPx = it.width.toFloat() }
                .graphicsLayer { translationX = offsetX }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta -> offsetX += delta },
                    onDragStopped = { velocity ->
                        val velocityThreshold = with(density) { SwipeVelocityThreshold.toPx() }
                        if (shouldDismissOnSwipe(offsetX, itemWidthPx, velocity, velocityThreshold)) {
                            requestDismiss()
                        } else {
                            offsetX = 0f
                        }
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (item.isError) System.color.border.error
                    else System.color.background.brand
                )
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.message,
                color = System.color.text.white,
                style = System.font.body.small.medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = requestDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = System.color.icon.white,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

internal fun shouldDismissOnSwipe(
    offsetX: Float,
    itemWidthPx: Float,
    velocity: Float,
    velocityThresholdPx: Float
): Boolean {
    val distanceThreshold = itemWidthPx * SwipeDismissFraction
    return abs(offsetX) > distanceThreshold || abs(velocity) > velocityThresholdPx
}
