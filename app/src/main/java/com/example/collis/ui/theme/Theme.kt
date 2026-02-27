package com.example.collis.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Collis App Theme
 *
 * Industry-standard Material3 theming with:
 * - Dark/Light mode support via DataStore preference
 * - Dynamic color support (Android 12+)
 * - Edge-to-edge system bar integration
 * - Custom typography with Montserrat & Roboto
 *
 * The [darkTheme] parameter is driven by [com.example.collis.presentation.viewmodel.MainViewModel]
 * which reads the user preference from DataStore. Toggling the switch on the
 * Profile screen immediately recomposes the entire tree.
 */
@Composable
fun CollisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default to maintain brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkCollisColorScheme
        else -> LightCollisColorScheme
    }

    // Keep status-bar icon color in sync with the chosen theme.
    // enableEdgeToEdge() handles the transparent status bar; we only
    // need to flip the light/dark icon flag here.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CollisTypography,
        content = content
    )
}

/** Light-theme preview helper */
@Composable
fun CollisThemeLightPreview(content: @Composable () -> Unit) {
    CollisTheme(darkTheme = false, content = content)
}

/** Dark-theme preview helper */
@Composable
fun CollisThemeDarkPreview(content: @Composable () -> Unit) {
    CollisTheme(darkTheme = true, content = content)
}