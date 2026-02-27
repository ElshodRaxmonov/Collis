package com.example.collis.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


internal val LightCollisColorScheme = lightColorScheme(

    primary = Color(0xFF163E6C),          // Slightly deeper navy (more premium)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E7FF),
    onPrimaryContainer = Color(0xFF001B3A),

    secondary = Color(0xFFB7791F),        // Rich academic gold (more elegant)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE5C2),
    onSecondaryContainer = Color(0xFF3B2500),

    tertiary = Color(0xFF00838F),         // Cleaner teal
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF002022),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFF5F7FA),       // Softer academic white
    onBackground = Color(0xFF121417),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE6E9EF),
    onSurfaceVariant = Color(0xFF44474F),

    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC4C6CF),

    inverseSurface = Color(0xFF2B3035),
    inverseOnSurface = Color(0xFFF1F1F5),
    inversePrimary = Color(0xFF9EC5FF)
)

internal val DarkCollisColorScheme = darkColorScheme(

    primary = Color(0xFF9EC5FF),          // Soft electric blue
    onPrimary = Color(0xFF00305F),
    primaryContainer = Color(0xFF0F3C73),
    onPrimaryContainer = Color(0xFFD9E7FF),

    secondary = Color(0xFFFFC977),        // Lighter academic gold
    onSecondary = Color(0xFF402600),
    secondaryContainer = Color(0xFF5C3A00),
    onSecondaryContainer = Color(0xFFFFE5C2),

    tertiary = Color(0xFF4DD0E1),         // Brighter teal for live feel
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF004F56),
    onTertiaryContainer = Color(0xFFB2EBF2),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0B1220),       // Slightly deeper than yours (more contrast)
    onBackground = Color(0xFFE3E6EB),

    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFE3E6EB),
    surfaceVariant = Color(0xFF42464E),
    onSurfaceVariant = Color(0xFFC4C6CF),

    outline = Color(0xFF8B9199),
    outlineVariant = Color(0xFF42464E),

    inverseSurface = Color(0xFFE3E6EB),
    inverseOnSurface = Color(0xFF2B3035),
    inversePrimary = Color(0xFF163E6C)
)

/**
 * Extended Color Palette for specific UI elements.
 * These supplement the Material3 color scheme for domain-specific visuals
 * like task status indicators and live-class badges.
 */
object CollisColors {
    // Task & lesson status indicators
    val LiveNow = Color(0xFF00E676)      // Bright green for live classes
    val Completed = Color(0xFF2E7D32)    // Deep green for completed items
    val Pending = Color(0xFFFFB300)      // Golden amber for pending items
    val Cancelled = Color(0xFFD32F2F)    // Red for cancelled items
}
