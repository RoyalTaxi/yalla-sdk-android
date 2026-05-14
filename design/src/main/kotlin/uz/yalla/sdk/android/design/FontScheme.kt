package uz.yalla.sdk.android.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import uz.yalla.sdk.android.design.R

@Immutable
public data class FontScheme(
    public val title: Title,
    public val body: Body,
    public val custom: Custom
) {
    @Immutable
    public data class Title(
        public val xLarge: TextStyle,
        public val large: TextStyle,
        public val base: TextStyle
    )

    @Immutable
    public data class Body(
        public val caption: TextStyle,
        public val large: Weighty,
        public val base: Weighty,
        public val small: Weighty
    ) {
        @Immutable
        public data class Weighty(
            public val regular: TextStyle,
            public val medium: TextStyle,
            public val bold: TextStyle
        )
    }

    @Immutable
    public data class Custom(
        public val carNumber: TextStyle
    )
}

public val LocalFontScheme = staticCompositionLocalOf<FontScheme> {
    error("No FontScheme provided")
}

@Composable
public fun rememberFontScheme(): FontScheme {
    val normalFont = FontFamily(Font(R.font.roboto_normal))
    val mediumFont = FontFamily(Font(R.font.roboto_medium))
    val boldFont = FontFamily(Font(R.font.roboto_bold))
    val carNumberFont = FontFamily(Font(R.font.nummernschild))

    return FontScheme(
        title = FontScheme.Title(
            xLarge = TextStyle(fontFamily = boldFont, fontSize = 30.sp, lineHeight = 30.sp),
            large = TextStyle(fontFamily = boldFont, fontSize = 22.sp, lineHeight = 22.sp),
            base = TextStyle(fontFamily = boldFont, fontSize = 20.sp, lineHeight = 20.sp)
        ),
        body = FontScheme.Body(
            caption = TextStyle(fontFamily = mediumFont, fontSize = 13.sp, lineHeight = 15.6.sp),
            large = FontScheme.Body.Weighty(
                regular = TextStyle(fontFamily = normalFont, fontSize = 18.sp, lineHeight = 21.6.sp),
                medium = TextStyle(fontFamily = mediumFont, fontSize = 18.sp, lineHeight = 21.6.sp),
                bold = TextStyle(fontFamily = boldFont, fontSize = 18.sp, lineHeight = 21.6.sp)
            ),
            base = FontScheme.Body.Weighty(
                regular = TextStyle(fontFamily = normalFont, fontSize = 16.sp, lineHeight = 20.8.sp),
                medium = TextStyle(fontFamily = mediumFont, fontSize = 16.sp, lineHeight = 20.8.sp),
                bold = TextStyle(fontFamily = boldFont, fontSize = 16.sp, lineHeight = 20.8.sp)
            ),
            small = FontScheme.Body.Weighty(
                regular = TextStyle(fontFamily = normalFont, fontSize = 14.sp, lineHeight = 15.4.sp),
                medium = TextStyle(fontFamily = mediumFont, fontSize = 14.sp, lineHeight = 15.4.sp),
                bold = TextStyle(fontFamily = boldFont, fontSize = 14.sp, lineHeight = 15.4.sp)
            )
        ),
        custom = FontScheme.Custom(
            carNumber = TextStyle(fontFamily = carNumberFont, fontSize = 12.sp, lineHeight = 16.sp)
        )
    )
}
