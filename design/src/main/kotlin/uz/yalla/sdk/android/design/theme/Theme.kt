package uz.yalla.sdk.android.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import uz.yalla.sdk.android.design.color.ColorScheme
import uz.yalla.sdk.android.design.color.LocalColorScheme
import uz.yalla.sdk.android.design.color.dark
import uz.yalla.sdk.android.design.color.light
import uz.yalla.sdk.android.design.font.FontScheme
import uz.yalla.sdk.android.design.font.LocalFontScheme
import uz.yalla.sdk.android.design.font.rememberFontScheme
import androidx.compose.material3.darkColorScheme as materialDarkColorScheme
import androidx.compose.material3.lightColorScheme as materialLightColorScheme

internal val LocalIsDark = staticCompositionLocalOf { false }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YallaTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (isDark) dark() else light(),
    fontScheme: FontScheme = rememberFontScheme(),
    content: @Composable () -> Unit
) {
    val rippleConfiguration =
        RippleConfiguration(
            color = if (isDark) Color.White else Color.Black,
            rippleAlpha =
                RippleAlpha(
                    pressedAlpha = 0.12f,
                    focusedAlpha = 0.08f,
                    draggedAlpha = 0.12f,
                    hoveredAlpha = 0.08f
                )
        )

    val materialColorScheme =
        if (isDark) {
            materialDarkColorScheme(
                primary = colorScheme.button.active,
                onPrimary = colorScheme.text.white,
                secondary = colorScheme.button.secondary,
                tertiary = colorScheme.button.tertiary,
                background = colorScheme.background.base,
                surface = colorScheme.background.secondary,
                error = colorScheme.text.red,
                onBackground = colorScheme.text.base,
                onSurface = colorScheme.text.base
            )
        } else {
            materialLightColorScheme(
                primary = colorScheme.button.active,
                onPrimary = colorScheme.text.white,
                secondary = colorScheme.button.secondary,
                tertiary = colorScheme.button.tertiary,
                background = colorScheme.background.base,
                surface = colorScheme.background.secondary,
                error = colorScheme.text.red,
                onBackground = colorScheme.text.base,
                onSurface = colorScheme.text.base
            )
        }

    CompositionLocalProvider(
        LocalIsDark provides isDark,
        LocalColorScheme provides colorScheme,
        LocalFontScheme provides fontScheme,
        LocalRippleConfiguration provides rippleConfiguration
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}

object System {
    val color: ColorScheme
        @Composable
        get() = LocalColorScheme.current

    val font: FontScheme
        @Composable
        get() = LocalFontScheme.current
}
