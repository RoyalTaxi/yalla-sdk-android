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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uz.yalla.components.primitives.button.PrimaryButton
import uz.yalla.design.image.ThemedImage
import uz.yalla.design.image.themedPainter
import uz.yalla.sdk.android.design.theme.System

private val ContentHorizontalPadding = 36.dp
private val ContentTopSpacing = 32.dp
private val ImageToTitleSpacing = 36.dp
private val TitleToDescriptionSpacing = 12.dp
private val ContentBottomSpacing = 32.dp

@Composable
internal fun ConfirmationSheet(
    isVisible: Boolean,
    image: ThemedImage,
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit,
    onDismissRequest: () -> Unit,
    dismissEnabled: Boolean,
    header: String? = null
) {
    Sheet(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        dismissEnabled = dismissEnabled,
        sheetSwipeEnabled = dismissEnabled,
        title = header,
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
                .padding(horizontal = ContentHorizontalPadding)
        ) {
            Spacer(Modifier.height(ContentTopSpacing))

            Image(
                painter = themedPainter(image),
                contentDescription = null,
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
            )

            Spacer(Modifier.height(ImageToTitleSpacing))

            Text(
                text = title,
                style = System.font.title.base,
                color = System.color.text.base,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(TitleToDescriptionSpacing))

            Text(
                text = description,
                style = System.font.body.base.medium,
                color = System.color.text.subtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(ContentBottomSpacing))
        }
    }
}
