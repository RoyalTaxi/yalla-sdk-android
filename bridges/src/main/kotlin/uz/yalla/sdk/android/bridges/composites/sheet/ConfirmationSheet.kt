package uz.yalla.sdk.android.bridges.composites.sheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import uz.yalla.components.primitives.button.PrimaryButton
import uz.yalla.sdk.android.design.theme.System

@Composable
internal fun ConfirmationSheet(
    isVisible: Boolean,
    imageResource: String,
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit,
    onDismissRequest: () -> Unit,
    dismissEnabled: Boolean
) {
    val context = LocalContext.current
    val imageResId = remember(imageResource) {
        context.resources.getIdentifier(imageResource, "drawable", context.packageName)
    }

    Sheet(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        dismissEnabled = dismissEnabled,
        onClose = if (dismissEnabled) onDismissRequest else null,
        footer = {
            PrimaryButton(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth()
            ) { _, _, _ ->
                Text(actionText)
            }
        }
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
                .padding(horizontal = 36.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            if (imageResId != 0) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageResId),
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1f)
                )
                Spacer(Modifier.height(36.dp))
            }

            Text(
                text = title,
                style = System.font.title.base,
                color = System.color.text.base,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = description,
                style = System.font.body.base.medium,
                color = System.color.text.subtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
