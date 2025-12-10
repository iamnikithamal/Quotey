package com.quotey.create.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    scrim = Scrim,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkTheme,
    onPrimary = OnPrimaryDarkTheme,
    primaryContainer = PrimaryContainerDarkTheme,
    onPrimaryContainer = OnPrimaryContainerDarkTheme,
    secondary = SecondaryDarkTheme,
    onSecondary = OnSecondaryDarkTheme,
    secondaryContainer = SecondaryContainerDarkTheme,
    onSecondaryContainer = OnSecondaryContainerDarkTheme,
    tertiary = TertiaryDarkTheme,
    onTertiary = OnTertiaryDarkTheme,
    tertiaryContainer = TertiaryContainerDarkTheme,
    onTertiaryContainer = OnTertiaryContainerDarkTheme,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundDarkTheme,
    onBackground = OnBackgroundDarkTheme,
    surface = SurfaceDarkTheme,
    onSurface = OnSurfaceDarkTheme,
    surfaceVariant = SurfaceVariantDarkTheme,
    onSurfaceVariant = OnSurfaceVariantDarkTheme,
    outline = OutlineDarkTheme,
    outlineVariant = OutlineVariantDarkTheme,
    scrim = Scrim,
    inverseSurface = InverseSurfaceDarkTheme,
    inverseOnSurface = InverseOnSurfaceDarkTheme,
    inversePrimary = InversePrimaryDarkTheme,
    surfaceDim = SurfaceDimDarkTheme,
    surfaceBright = SurfaceBrightDarkTheme,
    surfaceContainerLowest = SurfaceContainerLowestDarkTheme,
    surfaceContainerLow = SurfaceContainerLowDarkTheme,
    surfaceContainer = SurfaceContainerDarkTheme,
    surfaceContainerHigh = SurfaceContainerHighDarkTheme,
    surfaceContainerHighest = SurfaceContainerHighestDarkTheme,
)

@Composable
fun QuoteyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuoteyTypography,
        content = content
    )
}
