package uz.yalla.sdk.android.design

import uz.yalla.sdk.android.design.R

public object YallaColors {
    public object Text {
        public val base: Int get() = R.color.text_base
        public val subtle: Int get() = R.color.text_subtle
        public val link: Int get() = R.color.text_link
        public val red: Int get() = R.color.text_red
        public val white: Int get() = R.color.text_white
    }

    public object Background {
        public val base: Int get() = R.color.background_base
        public val brand: Int get() = R.color.background_brand
        public val secondary: Int get() = R.color.background_secondary
        public val tertiary: Int get() = R.color.background_tertiary
    }

    public object Border {
        public val disabled: Int get() = R.color.border_disabled
        public val filled: Int get() = R.color.border_filled
        public val white: Int get() = R.color.border_white
        public val error: Int get() = R.color.border_error
    }

    public object Button {
        public val active: Int get() = R.color.button_active
        public val disabled: Int get() = R.color.button_disabled
        public val secondary: Int get() = R.color.button_secondary
        public val tertiary: Int get() = R.color.button_tertiary
        public val disabledTertiary: Int get() = R.color.button_disabled_tertiary
    }

    public object Icon {
        public val white: Int get() = R.color.icon_white
        public val base: Int get() = R.color.icon_base
        public val secondary: Int get() = R.color.icon_secondary
        public val disabled: Int get() = R.color.icon_disabled
        public val red: Int get() = R.color.icon_red
        public val subtle: Int get() = R.color.icon_subtle
    }

    public object Accent {
        public val pinkSun: Int get() = R.color.accent_pink_sun
        public val color1: Int get() = R.color.accent_color1
        public val color2: Int get() = R.color.accent_color2
        public val color3: Int get() = R.color.accent_color3
        public val color4: Int get() = R.color.accent_color4
        public val color5: Int get() = R.color.accent_color5
    }

    public object Gradient {
        public val splash: IntArray get() = intArrayOf(R.color.gradient_splash_0, R.color.gradient_splash_1, R.color.gradient_splash_2)
        public val sunsetNight: IntArray get() = intArrayOf(R.color.gradient_sunset_night_0, R.color.gradient_sunset_night_1)
    }
}
