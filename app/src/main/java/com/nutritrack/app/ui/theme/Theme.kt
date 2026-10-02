package com.nutritrack.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Leaf80,
    onPrimary = Leaf20,
    primaryContainer = Leaf30,
    onPrimaryContainer = Leaf90,
    secondary = Carrot80,
    onSecondary = Carrot20,
    secondaryContainer = Carrot30,
    onSecondaryContainer = Carrot90,
    tertiary = Water80,
    onTertiary = Water20,
    tertiaryContainer = Water30,
    onTertiaryContainer = Water90,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSurfaceLowest,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurfaceContainer,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Leaf40,
    onPrimary = LightSurfaceLowest,
    primaryContainer = Leaf90,
    onPrimaryContainer = Leaf10,
    secondary = Carrot40,
    onSecondary = LightSurfaceLowest,
    secondaryContainer = Carrot90,
    onSecondaryContainer = Carrot10,
    tertiary = Water40,
    onTertiary = LightSurfaceLowest,
    tertiaryContainer = Water90,
    onTertiaryContainer = Water10,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurfaceLowest,
    surfaceContainerLow = LightSurfaceLow,
    surfaceContainer = LightSurfaceContainer,
    outlineVariant = LightOutlineVariant
)

@Composable
fun NutriTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default: NutriTrack uses its own fresh-produce palette rather than wallpaper colors.
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}