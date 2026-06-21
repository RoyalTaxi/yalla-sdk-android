package uz.yalla.sdk.android.design.font

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

@Immutable
data class FontScheme(
    val title: Title,
    val body: Body,
    val custom: Custom
) {
    @Immutable
    data class Title(
        val xLarge: TextStyle,
        val large: TextStyle,
        val base: TextStyle
    )

    @Immutable
    data class Body(
        val caption: TextStyle,
        val large: Weights,
        val base: Weights,
        val small: Weights
    ) {
        @Immutable
        data class Weights(
            val regular: TextStyle,
            val medium: TextStyle,
            val bold: TextStyle
        )
    }

    @Immutable
    data class Custom(
        val carNumber: TextStyle
    )
}

val LocalFontScheme =
    staticCompositionLocalOf<FontScheme> {
        error("No FontScheme provided. Wrap your content with YallaTheme or provide a FontScheme via LocalFontScheme.")
    }
