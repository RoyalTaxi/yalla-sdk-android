package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import uz.yalla.sdk.android.bridges.feedback.YallaSnackbarHost
import uz.yalla.components.primitives.button.CloseButton
import uz.yalla.design.theme.System as CommonSystem
import uz.yalla.sdk.android.components.primitives.button.DragButton
import uz.yalla.sdk.android.design.theme.System
import uz.yalla.sdk.android.design.theme.YallaTheme

private val SheetCornerRadius = 38.dp
private val SheetHeaderShadowElevation = 3.dp
private val SheetFooterShadowElevation = 6.dp
private val SheetTonalElevation = 2.dp
private val SheetContentSpacing = 10.dp
private val SheetHeaderPadding = 16.dp
private val SheetFooterVerticalPadding = 8.dp
private val SheetFooterHorizontalPadding = 20.dp
private val SheetCloseSlotWidth = 44.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Sheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissEnabled: Boolean = true,
    sheetSwipeEnabled: Boolean = true,
    title: String? = null,
    onClose: (() -> Unit)? = null,
    headerAction: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    headerElevated: Boolean = false,
    footerElevated: Boolean = false,
    fullHeight: Boolean = false,
    imeAsContentPadding: Boolean = false,
    onFullyExpanded: (() -> Unit)? = null,
    content: @Composable (padding: PaddingValues) -> Unit
) {
    val isDark = CommonSystem.isDark
    val scope = rememberCoroutineScope()
    var shouldShow by remember { mutableStateOf(false) }

    val currentIsVisible = rememberUpdatedState(isVisible)
    val currentDismissEnabled = rememberUpdatedState(dismissEnabled)
    val confirmValueChange = remember {
        { value: SheetValue -> !shouldBlockHide(value, currentDismissEnabled.value, currentIsVisible.value) }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = confirmValueChange
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            if (shouldShow) sheetState.show() else shouldShow = true
        } else if (shouldShow) {
            sheetState.hide()
            shouldShow = false
        }
    }

    LaunchedEffect(sheetState, onFullyExpanded) {
        if (onFullyExpanded == null) return@LaunchedEffect
        snapshotFlow { sheetState.currentValue to sheetState.targetValue }
            .distinctUntilChanged()
            .filter { (current, target) -> current == SheetValue.Expanded && current == target }
            .collect { onFullyExpanded() }
    }

    val hasHeader = title != null || onClose != null || headerAction != null

    if (shouldShow) {
        YallaTheme(isDark = isDark) {
            ModalBottomSheet(
                modifier = modifier.statusBarsPadding(),
                contentWindowInsets = {
                    if (imeAsContentPadding) WindowInsets(0, 0, 0, 0)
                    else WindowInsets.navigationBars.union(WindowInsets.ime)
                },
                onDismissRequest = {
                    if (dismissEnabled) {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            shouldShow = false
                            onDismissRequest()
                        }
                    }
                },
                sheetState = sheetState,
                sheetGesturesEnabled = sheetSwipeEnabled,
                properties = ModalBottomSheetProperties(shouldDismissOnBackPress = dismissEnabled),
                shape = RoundedCornerShape(
                    topStart = SheetCornerRadius,
                    topEnd = SheetCornerRadius
                ),
                containerColor = System.color.background.base,
                dragHandle = null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (fullHeight) Modifier.fillMaxHeight() else Modifier)
                        .clip(
                            RoundedCornerShape(
                                topStart = SheetCornerRadius,
                                topEnd = SheetCornerRadius
                            )
                        )
                ) {
                    val density = LocalDensity.current
                    var dragHandleHeight by remember { mutableStateOf(0.dp) }
                    var headerHeight by remember { mutableStateOf(0.dp) }
                    var footerHeight by remember { mutableStateOf(0.dp) }

                    val insetBottom = if (imeAsContentPadding) {
                        maxOf(
                            WindowInsets.ime.asPaddingValues().calculateBottomPadding(),
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        )
                    } else 0.dp

                    content(
                        PaddingValues(
                            top = dragHandleHeight + headerHeight,
                            bottom = footerHeight + insetBottom
                        )
                    )

                    DragButton(
                        onClick = {
                            if (dismissEnabled) {
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .onSizeChanged { dragHandleHeight = with(density) { it.height.toDp() } }
                    )

                    if (hasHeader) {
                        SheetHeader(
                            title = title,
                            onClose = onClose,
                            action = headerAction,
                            elevated = headerElevated,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = dragHandleHeight)
                                .onSizeChanged { headerHeight = with(density) { it.height.toDp() } }
                        )
                    }

                    if (footer != null) {
                        SheetFooter(
                            elevated = footerElevated,
                            content = footer,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .onSizeChanged { footerHeight = with(density) { it.height.toDp() } }
                        )
                    }

                    YallaSnackbarHost()
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(
    title: String?,
    onClose: (() -> Unit)?,
    action: (@Composable () -> Unit)?,
    elevated: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = System.color.background.base,
        tonalElevation = if (elevated) SheetTonalElevation else 0.dp,
        shadowElevation = if (elevated) SheetHeaderShadowElevation else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SheetHeaderPadding)
        ) {
            if (onClose != null) {
                CloseButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            if (title != null) {
                Text(
                    text = title,
                    color = System.color.text.base,
                    style = System.font.body.large.medium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = SheetCloseSlotWidth)
                )
            }

            if (action != null) {
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    action()
                }
            }
        }
    }
}

@Composable
private fun SheetFooter(
    elevated: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = SheetCornerRadius,
            topEnd = SheetCornerRadius
        ),
        color = System.color.background.base,
        shadowElevation = if (elevated) SheetFooterShadowElevation else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = SheetFooterHorizontalPadding,
                    end = SheetFooterHorizontalPadding,
                    top = SheetFooterHorizontalPadding,
                    bottom = SheetFooterVerticalPadding
                )
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
internal fun shouldBlockHide(
    value: SheetValue,
    dismissEnabled: Boolean,
    isVisible: Boolean
): Boolean {
    return value == SheetValue.Hidden && !dismissEnabled && isVisible
}
