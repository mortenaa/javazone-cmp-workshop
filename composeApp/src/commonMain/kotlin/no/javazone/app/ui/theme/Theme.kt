package no.javazone.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * JavaZone 2026 "deep ocean" palette (DESIGN.md §5, verified against 2026.javazone.no).
 * Colors only — typography and shapes stay at Material 3 defaults.
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF0B3554),            // deep ocean
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBEE8E6),   // sea mist
    onPrimaryContainer = Color(0xFF071A2B), // abyss navy
    secondary = Color(0xFF094D55),          // reef teal
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2EEF2),
    onSecondaryContainer = Color(0xFF05282C),
    tertiary = Color(0xFF7C5800),           // dark gold — raw gold fails contrast on white
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF2C14E),  // sunbeam gold
    onTertiaryContainer = Color(0xFF3F2E00),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF7F9F9),
    onBackground = Color(0xFF171C1E),
    surface = Color(0xFFF7F9F9),
    onSurface = Color(0xFF171C1E),
    surfaceVariant = Color(0xFFDBE4E6),
    onSurfaceVariant = Color(0xFF40484B),
    surfaceContainerHighest = Color(0xFFE2E8E9),
    outline = Color(0xFF70787B),
    outlineVariant = Color(0xFFBFC8CB),
    inversePrimary = Color(0xFF57C4D1),     // blue lagoon
    inverseSurface = Color(0xFF2B3133),
    inverseOnSurface = Color(0xFFECF2F3),
    scrim = Color(0xFF000000),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF57C4D1),            // blue lagoon
    onPrimary = Color(0xFF00363C),
    primaryContainer = Color(0xFF0B3554),   // deep ocean
    onPrimaryContainer = Color(0xFFBEE8E6), // sea mist
    secondary = Color(0xFF9BD0D8),
    onSecondary = Color(0xFF003A40),
    secondaryContainer = Color(0xFF094D55), // reef teal
    onSecondaryContainer = Color(0xFFBEE8E6),
    tertiary = Color(0xFFF2C14E),           // sunbeam gold — pops on navy
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5B4300),
    onTertiaryContainer = Color(0xFFFFDF9E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF071A2B),         // abyss navy — THE brand background
    onBackground = Color(0xFFF0EEE9),       // cloud dancer
    surface = Color(0xFF071A2B),
    onSurface = Color(0xFFF0EEE9),
    surfaceVariant = Color(0xFF33424B),
    onSurfaceVariant = Color(0xFFB9C6CD),
    surfaceContainerHighest = Color(0xFF16354C),
    outline = Color(0xFF8FB2C6),            // slate blue-gray
    outlineVariant = Color(0xFF33424B),
    inversePrimary = Color(0xFF0B3554),
    inverseSurface = Color(0xFFF0EEE9),
    inverseOnSurface = Color(0xFF2B3133),
    scrim = Color(0xFF000000),
)

@Composable
fun JavaZoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
