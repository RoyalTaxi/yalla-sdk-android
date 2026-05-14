package uz.yalla.sdk.android.design

import uz.yalla.sdk.android.design.R

public object YallaColors {
    public object Text {
        public val base: Int get() = R.color.yalla_text_base
        public val subtle: Int get() = R.color.yalla_text_subtle
        public val link: Int get() = R.color.yalla_text_link
        public val red: Int get() = R.color.yalla_text_red
        public val white: Int get() = R.color.yalla_text_white
    }

    public object Background {
        public val base: Int get() = R.color.yalla_background_base
        public val brand: Int get() = R.color.yalla_background_brand
        public val secondary: Int get() = R.color.yalla_background_secondary
        public val tertiary: Int get() = R.color.yalla_background_tertiary
    }

    public object Border {
        public val disabled: Int get() = R.color.yalla_border_disabled
        public val filled: Int get() = R.color.yalla_border_filled
        public val white: Int get() = R.color.yalla_border_white
        public val error: Int get() = R.color.yalla_border_error
    }

    public object Button {
        public val active: Int get() = R.color.yalla_button_active
        public val disabled: Int get() = R.color.yalla_button_disabled
        public val secondary: Int get() = R.color.yalla_button_secondary
        public val tertiary: Int get() = R.color.yalla_button_tertiary
        public val disabledTertiary: Int get() = R.color.yalla_button_disabled_tertiary
    }

    public object Icon {
        public val white: Int get() = R.color.yalla_icon_white
        public val base: Int get() = R.color.yalla_icon_base
        public val secondary: Int get() = R.color.yalla_icon_secondary
        public val disabled: Int get() = R.color.yalla_icon_disabled
        public val red: Int get() = R.color.yalla_icon_red
        public val subtle: Int get() = R.color.yalla_icon_subtle
    }

    public object Accent {
        public val pinkSun: Int get() = R.color.yalla_accent_pink_sun
        public val color1: Int get() = R.color.yalla_accent_color1
        public val color2: Int get() = R.color.yalla_accent_color2
        public val color3: Int get() = R.color.yalla_accent_color3
        public val color4: Int get() = R.color.yalla_accent_color4
        public val color5: Int get() = R.color.yalla_accent_color5
    }

    public object Gradient {
        public val splash: IntArray get() = intArrayOf(R.color.yalla_gradient_splash_0, R.color.yalla_gradient_splash_1, R.color.yalla_gradient_splash_2)
        public val sunsetNight: IntArray get() = intArrayOf(R.color.yalla_gradient_sunset_night_0, R.color.yalla_gradient_sunset_night_1)
    }
}
