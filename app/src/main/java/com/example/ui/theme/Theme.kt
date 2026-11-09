package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RamsInstrumentColorScheme = darkColorScheme(
    primary = RamsAmberAttack,
    onPrimary = RamsBackground,
    primaryContainer = RamsAmberContainer,
    onPrimaryContainer = RamsTextPrimary,
    secondary = RamsSageRecovery,
    onSecondary = RamsBackground,
    secondaryContainer = RamsSageContainer,
    onSecondaryContainer = RamsTextPrimary,
    tertiary = RamsWarmYellow,
    onTertiary = RamsBackground,
    tertiaryContainer = RamsWarmYellowContainer,
    onTertiaryContainer = RamsTextPrimary,
    background = RamsBackground,
    onBackground = RamsTextPrimary,
    surface = RamsSurface,
    onSurface = RamsTextPrimary,
    surfaceVariant = RamsSurfaceElevated,
    onSurfaceVariant = RamsTextSecondary,
    outline = RamsOutline,
    outlineVariant = RamsTextMuted
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RamsInstrumentColorScheme,
        typography = Typography,
        content = content
    )
}
