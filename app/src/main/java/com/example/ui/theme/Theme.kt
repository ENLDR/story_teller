package com.example.ui.theme

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
    primary = CrimsonPrimary,
    onPrimary = GhostlyWhite,
    secondary = ScarletAccent,
    onSecondary = ObsidianBackground,
    tertiary = BloodGold,
    background = ObsidianBackground,
    onBackground = GhostlyWhite,
    surface = DarkCrimsonSurface,
    onSurface = GhostlyWhite,
    surfaceVariant = DarkMutedCard,
    onSurfaceVariant = ShadowGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for immersive horror mood
    dynamicColor: Boolean = false, // Disable dynamic colors so our crimson blood palette is forced
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
