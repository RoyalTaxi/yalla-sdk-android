package uz.yalla.sdk.android.design

import uz.yalla.sdk.android.design.R

public data class YallaTextStyle(
    public val fontRes: Int,
    public val sizeSp: Float,
    public val lineHeightSp: Float
)

public object YallaFonts {
    public object Title {
        public val xLarge: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 30.0f, lineHeightSp = 30.0f)
        public val large: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 22.0f, lineHeightSp = 22.0f)
        public val base: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 20.0f, lineHeightSp = 20.0f)
    }

    public object Body {
        public val caption: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_medium, sizeSp = 13.0f, lineHeightSp = 15.6f)

        public object Large {
            public val regular: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_normal, sizeSp = 18.0f, lineHeightSp = 21.6f)
            public val medium: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_medium, sizeSp = 18.0f, lineHeightSp = 21.6f)
            public val bold: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 18.0f, lineHeightSp = 21.6f)
        }

        public object Base {
            public val regular: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_normal, sizeSp = 16.0f, lineHeightSp = 20.8f)
            public val medium: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_medium, sizeSp = 16.0f, lineHeightSp = 20.8f)
            public val bold: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 16.0f, lineHeightSp = 20.8f)
        }

        public object Small {
            public val regular: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_normal, sizeSp = 14.0f, lineHeightSp = 15.4f)
            public val medium: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_medium, sizeSp = 14.0f, lineHeightSp = 15.4f)
            public val bold: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 14.0f, lineHeightSp = 15.4f)
        }
    }

    public object Custom {
        public val carNumber: YallaTextStyle = YallaTextStyle(fontRes = R.font.nummernschild, sizeSp = 12.0f, lineHeightSp = 16.0f)
    }
}
