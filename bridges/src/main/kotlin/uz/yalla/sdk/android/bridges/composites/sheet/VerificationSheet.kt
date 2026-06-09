package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import uz.yalla.components.primitives.button.GhostButton
import uz.yalla.components.primitives.button.PrimaryButton
import uz.yalla.components.primitives.field.PinField
import uz.yalla.sdk.android.design.theme.System

private const val AutoFocusDelayMillis = 250L

@Composable
internal fun VerificationSheet(
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
    title: String?,
    isError: Boolean,
    isLoading: Boolean,
    resendEnabled: Boolean,
    onCodeComplete: (String) -> Unit,
    onDismissRequest: () -> Unit,
    dismissEnabled: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val complete = code.length == codeLength

    LaunchedEffect(Unit) {
        delay(AutoFocusDelayMillis)
        runCatching { focusRequester.requestFocus() }
    }

    Sheet(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        dismissEnabled = dismissEnabled,
        sheetSwipeEnabled = dismissEnabled,
        title = title,
        onClose = if (dismissEnabled) onDismissRequest else null,
        footer = {
            PrimaryButton(
                enabled = complete && !isError,
                loading = isLoading,
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            ) { _, _, styles ->
                Text(text = confirmText, style = styles.textStyle)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
        ) {
            Text(
                text = headline,
                style = System.font.title.xLarge,
                color = System.color.text.base,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = description,
                style = System.font.body.small.regular,
                color = System.color.text.subtle,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(32.dp))

            PinField(
                value = code,
                onValueChange = { new ->
                    onCodeChange(new)
                    if (new.length == codeLength) onCodeComplete(new)
                },
                length = codeLength,
                error = isError,
                focusRequester = focusRequester,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            GhostButton(
                text = resendText,
                onClick = onResend,
                enabled = resendEnabled && !isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.weight(1f))
        }
    }
}
