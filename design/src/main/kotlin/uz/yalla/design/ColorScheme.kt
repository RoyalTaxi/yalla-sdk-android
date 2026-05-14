package uz.yalla.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import uz.yalla.sdk.android.design.R

@Immutable
public data class ColorScheme(
    public val text: Text,
    public val background: Background,
    public val border: Border,
    public val button: Button,
    public val icon: Icon,
    public val accent: Accent,
    public val gradient: Gradient
)

    @Immutable
    public data class Text(
        public val base: Color,
        public val subtle: Color,
        public val link: Color,
        public val red: Color,
        public val white: Color
    )

    @Immutable
    public data class Background(
        public val base: Color,
        public val brand: Color,
        public val secondary: Color,
        public val tertiary: Color
    )

    @Immutable
    public data class Border(
        public val disabled: Color,
        public val filled: Color,
        public val white: Color,
        public val error: Color
    )

    @Immutable
    public data class Button(
        public val active: Color,
        public val disabled: Color,
        public val secondary: Color,
        public val tertiary: Color,
        public val disabledTertiary: Color
    )

    @Immutable
    public data class Icon(
        public val white: Color,
        public val base: Color,
        public val secondary: Color,
        public val disabled: Color,
        public val red: Color,
        public val subtle: Color
    )

    @Immutable
    public data class Accent(
        public val pinkSun: Color,
        public val color1: Color,
        public val color2: Color,
        public val color3: Color,
        public val color4: Color,
        public val color5: Color
    )

    @Immutable
    public data class Gradient(
        public val splash: Brush,
        public val sunsetNight: Brush
    )

@Composable
public fun rememberColorScheme(): ColorScheme = ColorScheme(
    text = ColorScheme.Text(
        base = colorResource(R.color.text_base),
        subtle = colorResource(R.color.text_subtle),
        link = colorResource(R.color.text_link),
        red = colorResource(R.color.text_red),
        white = colorResource(R.color.text_white)
    ),
    background = ColorScheme.Background(
        base = colorResource(R.color.background_base),
        brand = colorResource(R.color.background_brand),
        secondary = colorResource(R.color.background_secondary),
        tertiary = colorResource(R.color.background_tertiary)
    ),
    border = ColorScheme.Border(
        disabled = colorResource(R.color.border_disabled),
        filled = colorResource(R.color.border_filled),
        white = colorResource(R.color.border_white),
        error = colorResource(R.color.border_error)
    ),
    button = ColorScheme.Button(
        active = colorResource(R.color.button_active),
        disabled = colorResource(R.color.button_disabled),
        secondary = colorResource(R.color.button_secondary),
        tertiary = colorResource(R.color.button_tertiary),
        disabledTertiary = colorResource(R.color.button_disabled_tertiary)
    ),
    icon = ColorScheme.Icon(
        white = colorResource(R.color.icon_white),
        base = colorResource(R.color.icon_base),
        secondary = colorResource(R.color.icon_secondary),
        disabled = colorResource(R.color.icon_disabled),
        red = colorResource(R.color.icon_red),
        subtle = colorResource(R.color.icon_subtle)
    ),
    accent = ColorScheme.Accent(
        pinkSun = colorResource(R.color.accent_pink_sun),
        color1 = colorResource(R.color.accent_color1),
        color2 = colorResource(R.color.accent_color2),
        color3 = colorResource(R.color.accent_color3),
        color4 = colorResource(R.color.accent_color4),
        color5 = colorResource(R.color.accent_color5)
    ),
    gradient = ColorScheme.Gradient(
        splash = Brush.linearGradient(listOf(colorResource(R.color.gradient_splash_0), colorResource(R.color.gradient_splash_1), colorResource(R.color.gradient_splash_2))),
        sunsetNight = Brush.linearGradient(listOf(colorResource(R.color.gradient_sunset_night_0), colorResource(R.color.gradient_sunset_night_1)))
    )
)

public val LocalColorScheme = staticCompositionLocalOf {
    error("No ColorScheme provided")
}
