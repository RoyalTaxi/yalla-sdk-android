package uz.yalla.design.color

import androidx.compose.ui.graphics.Color

object YallaColors {
    object Text {
        val base: Color get() = light_text_base
        val subtle: Color get() = light_text_subtle
        val link: Color get() = light_text_link
        val red: Color get() = light_text_red
        val white: Color get() = light_text_white
    }

    object Background {
        val base: Color get() = light_background_base
        val brand: Color get() = light_background_brand
        val secondary: Color get() = light_background_secondary
        val tertiary: Color get() = light_background_tertiary
    }

    object Border {
        val disabled: Color get() = light_border_disabled
        val filled: Color get() = light_border_filled
        val white: Color get() = light_border_white
        val error: Color get() = light_border_error
    }

    object Button {
        val active: Color get() = light_button_active
        val disabled: Color get() = light_button_disabled
        val secondary: Color get() = light_button_secondary
        val tertiary: Color get() = light_button_tertiary
        val disabledTertiary: Color get() = light_button_disabled_tertiary
    }

    object Icon {
        val white: Color get() = light_icon_white
        val base: Color get() = light_icon_base
        val secondary: Color get() = light_icon_secondary
        val disabled: Color get() = light_icon_disabled
        val red: Color get() = light_icon_red
        val subtle: Color get() = light_icon_subtle
    }

    object Accent {
        val pinkSun: Color get() = accent_pink_sun
        val color1: Color get() = accent_color1
        val color2: Color get() = accent_color2
        val color3: Color get() = accent_color3
        val color4: Color get() = accent_color4
        val color5: Color get() = accent_color5
    }

    object Gradient {
        val splash: List<Color> get() = listOf(gradient_splash_0, gradient_splash_1, gradient_splash_2)
        val sunsetNight: List<Color> get() = listOf(gradient_sunset_night_0, gradient_sunset_night_1)
    }
}
