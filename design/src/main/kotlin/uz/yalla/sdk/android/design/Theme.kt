package uz.yalla.sdk.android.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme as materialDarkColorScheme
import androidx.compose.material3.lightColorScheme as materialLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
public fun YallaTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = rememberColorScheme(),
    fontScheme: FontScheme = rememberFontScheme(),
    content: @Composable () -> Unit
) {
    val materialColorScheme = if (isDark) {
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
        LocalColorScheme provides colorScheme,
        LocalFontScheme provides fontScheme
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}

public object System {
    public val color: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    public val font: FontScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalFontScheme.current
}
