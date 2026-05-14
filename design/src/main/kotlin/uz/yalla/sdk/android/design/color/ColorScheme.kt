package uz.yalla.sdk.android.design.color

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class ColorScheme(
    val text: Text,
    val background: Background,
    val border: Border,
    val button: Button,
    val icon: Icon,
    val accent: Accent,
    val gradient: Gradient
) {
    @Immutable
    data class Text(
        val base: Color,
        val subtle: Color,
        val link: Color,
        val red: Color,
        val white: Color,
    )

    @Immutable
    data class Background(
        val base: Color,
        val brand: Color,
        val secondary: Color,
        val tertiary: Color,
    )

    @Immutable
    data class Border(
        val disabled: Color,
        val filled: Color,
        val white: Color,
        val error: Color,
    )

    @Immutable
    data class Button(
        val active: Color,
        val disabled: Color,
        val secondary: Color,
        val tertiary: Color,
        val disabledTertiary: Color,
    )

    @Immutable
    data class Icon(
        val white: Color,
        val base: Color,
        val secondary: Color,
        val disabled: Color,
        val red: Color,
        val subtle: Color,
    )

    @Immutable
    data class Accent(
        val pinkSun: Color,
        val color1: Color,
        val color2: Color,
        val color3: Color,
        val color4: Color,
        val color5: Color,
    )

    @Immutable
    data class Gradient(
        val splash: Brush,
        val sunsetNight: Brush,
    )
}

fun light() =
    ColorScheme(
        text =
            ColorScheme.Text(
                base = LightTextBase,
                subtle = LightTextSubtle,
                link = LightTextLink,
                red = LightTextRed,
                white = LightTextWhite,
            ),
        background =
            ColorScheme.Background(
                base = LightBackgroundBase,
                brand = LightBackgroundBrandBase,
                secondary = LightBackgroundSecondary,
                tertiary = LightBackgroundTertiary,
            ),
        border =
            ColorScheme.Border(
                disabled = LightBorderDisabled,
                filled = LightBorderFilled,
                white = LightBorderWhite,
                error = LightBorderError,
            ),
        button =
            ColorScheme.Button(
                active = LightButtonActive,
                disabled = LightButtonDisabled,
                secondary = LightButtonSecondary,
                tertiary = LightButtonTertiary,
                disabledTertiary = LightButtonDisabledTertiary,
            ),
        icon =
            ColorScheme.Icon(
                white = LightIconWhite,
                base = LightIconBase,
                secondary = LightIconSecondary,
                disabled = LightIconDisabled,
                red = LightIconRed,
                subtle = LightIconSubtle,
            ),
        accent =
            ColorScheme.Accent(
                pinkSun = PinkSun,
                color1 = Color1,
                color2 = Color2,
                color3 = Color3,
                color4 = Color4,
                color5 = Color5,
            ),
        gradient =
            ColorScheme.Gradient(
                splash = SplashBackground,
                sunsetNight = SunsetNight,
            )
    )

fun dark() =
    ColorScheme(
        text =
            ColorScheme.Text(
                base = DarkTextBase,
                subtle = DarkTextSubtle,
                link = DarkTextLink,
                red = DarkTextRed,
                white = DarkTextWhite,
            ),
        background =
            ColorScheme.Background(
                base = DarkBackgroundBase,
                brand = DarkBackgroundBrandBase,
                secondary = DarkBackgroundSecondary,
                tertiary = DarkBackgroundTertiary,
            ),
        border =
            ColorScheme.Border(
                disabled = DarkBorderDisabled,
                filled = DarkBorderFilled,
                white = DarkBorderWhite,
                error = DarkBorderError,
            ),
        button =
            ColorScheme.Button(
                active = DarkButtonActive,
                disabled = DarkButtonDisabled,
                secondary = DarkButtonSecondary,
                tertiary = DarkButtonTertiary,
                disabledTertiary = DarkButtonDisabledTertiary,
            ),
        icon =
            ColorScheme.Icon(
                white = DarkIconWhite,
                base = DarkIconBase,
                secondary = DarkIconSecondary,
                disabled = DarkIconDisabled,
                red = DarkIconRed,
                subtle = DarkIconSubtle,
            ),
        accent =
            ColorScheme.Accent(
                pinkSun = PinkSun,
                color1 = Color1,
                color2 = Color2,
                color3 = Color3,
                color4 = Color4,
                color5 = Color5,
            ),
        gradient =
            ColorScheme.Gradient(
                splash = SplashBackground,
                sunsetNight = SunsetNight,
            )
    )

val LocalColorScheme = staticCompositionLocalOf { light() }
