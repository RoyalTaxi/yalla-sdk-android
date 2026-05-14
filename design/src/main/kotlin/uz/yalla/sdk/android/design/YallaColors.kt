package uz.yalla.design.color

import uz.yalla.sdk.android.design.R

object YallaColors {
    object Text {
        val base: Int get() = R.color.text_base
        val subtle: Int get() = R.color.text_subtle
        val link: Int get() = R.color.text_link
        val red: Int get() = R.color.text_red
        val white: Int get() = R.color.text_white
    }

    object Background {
        val base: Int get() = R.color.background_base
        val brand: Int get() = R.color.background_brand
        val secondary: Int get() = R.color.background_secondary
        val tertiary: Int get() = R.color.background_tertiary
    }

    object Border {
        val disabled: Int get() = R.color.border_disabled
        val filled: Int get() = R.color.border_filled
        val white: Int get() = R.color.border_white
        val error: Int get() = R.color.border_error
    }

    object Button {
        val active: Int get() = R.color.button_active
        val disabled: Int get() = R.color.button_disabled
        val secondary: Int get() = R.color.button_secondary
        val tertiary: Int get() = R.color.button_tertiary
        val disabledTertiary: Int get() = R.color.button_disabled_tertiary
    }

    object Icon {
        val white: Int get() = R.color.icon_white
        val base: Int get() = R.color.icon_base
        val secondary: Int get() = R.color.icon_secondary
        val disabled: Int get() = R.color.icon_disabled
        val red: Int get() = R.color.icon_red
        val subtle: Int get() = R.color.icon_subtle
    }

    object Accent {
        val pinkSun: Int get() = R.color.accent_pink_sun
        val color1: Int get() = R.color.accent_color1
        val color2: Int get() = R.color.accent_color2
        val color3: Int get() = R.color.accent_color3
        val color4: Int get() = R.color.accent_color4
        val color5: Int get() = R.color.accent_color5
    }

    object Gradient {
        val splash: IntArray get() = intArrayOf(R.color.gradient_splash_0, R.color.gradient_splash_1, R.color.gradient_splash_2)
        val sunsetNight: IntArray get() = intArrayOf(R.color.gradient_sunset_night_0, R.color.gradient_sunset_night_1)
    }
}
