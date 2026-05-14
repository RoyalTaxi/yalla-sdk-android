package uz.yalla.design.color

import uz.yalla.sdk.android.design.R

data class YallaTextStyle(
    val fontRes: Int,
    val sizeSp: Float,
    val lineHeightSp: Float
)

object YallaFonts {
    object Title {
        val xLarge: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 30.0f, lineHeightSp = 30.0f)
        val large: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 22.0f, lineHeightSp = 22.0f)
        val base: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 20.0f, lineHeightSp = 20.0f)
    }

    object Body {
        val caption: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_medium, sizeSp = 13.0f, lineHeightSp = 15.6f)

        object Large {
            val regular: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_normal, sizeSp = 18.0f, lineHeightSp = 21.6f)
            val medium: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_medium, sizeSp = 18.0f, lineHeightSp = 21.6f)
            val bold: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 18.0f, lineHeightSp = 21.6f)
        }

        object Base {
            val regular: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_normal, sizeSp = 16.0f, lineHeightSp = 20.8f)
            val medium: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_medium, sizeSp = 16.0f, lineHeightSp = 20.8f)
            val bold: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 16.0f, lineHeightSp = 20.8f)
        }

        object Small {
            val regular: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_normal, sizeSp = 14.0f, lineHeightSp = 15.4f)
            val medium: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_medium, sizeSp = 14.0f, lineHeightSp = 15.4f)
            val bold: YallaTextStyle = YallaTextStyle(fontRes = R.font.roboto_bold, sizeSp = 14.0f, lineHeightSp = 15.4f)
        }
    }

    object Custom {
        val carNumber: YallaTextStyle = YallaTextStyle(fontRes = R.font.nummernschild, sizeSp = 12.0f, lineHeightSp = 16.0f)
    }
}
