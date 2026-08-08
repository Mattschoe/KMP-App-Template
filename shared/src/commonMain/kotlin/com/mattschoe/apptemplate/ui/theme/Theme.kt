package com.mattschoe.apptemplate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mattschoe.apptemplate.domain.repositories.PreferencesRepository

// The container roles matter more than they look: Card, FloatingActionButton and
// AlertDialog all derive their background from them, so leaving them unset makes
// those components fall back to Material's default purple regardless of the palette.
private val LightColors = lightColorScheme(
    primary = NavyDeep,
    onPrimary = BeigeSurface,
    primaryContainer = NavyMuted,
    onPrimaryContainer = BeigeSurface,
    secondary = OliveGreen,
    onSecondary = BeigeSurface,
    secondaryContainer = OliveLight,
    onSecondaryContainer = CharcoalText,
    tertiary = RustRed,
    onTertiary = BeigeSurface,
    tertiaryContainer = RustLight,
    onTertiaryContainer = CharcoalText,
    background = BeigeBackground,
    onBackground = CharcoalText,
    surface = BeigeSurface,
    onSurface = CharcoalText,
    surfaceVariant = BeigeBackground,
    onSurfaceVariant = CharcoalText,
    surfaceContainer = BeigeBackground,
    surfaceContainerHigh = BeigeSurface,
    surfaceContainerHighest = BeigeSurface,
    error = RustRed,
    onError = BeigeSurface
)

private val DarkColors = darkColorScheme(
    primary = NavyLight,
    onPrimary = NavyDeep,
    primaryContainer = NavyMuted,
    onPrimaryContainer = OffWhiteText,
    secondary = OliveLight,
    onSecondary = NavyDeep,
    secondaryContainer = OliveGreen,
    onSecondaryContainer = OffWhiteText,
    tertiary = RustLight,
    onTertiary = NavyDeep,
    tertiaryContainer = RustRed,
    onTertiaryContainer = OffWhiteText,
    background = DarkBackground,
    onBackground = OffWhiteText,
    surface = DarkSurface,
    onSurface = OffWhiteText,
    surfaceVariant = NavyMuted,
    onSurfaceVariant = OffWhiteText,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = NavyMuted,
    surfaceContainerHighest = NavyMuted,
    error = RustLight,
    onError = NavyDeep
)

/**
 * Reads the user's stored theme choice reactively; `null` means "follow the system".
 * Dynamic colour is deliberately not wired up — the palette above is the brand.
 */
@Composable
fun AppTheme(
    preferencesRepository: PreferencesRepository,
    content: @Composable () -> Unit
) {
    val storedDarkMode: Boolean? by preferencesRepository.isDarkMode
        .collectAsStateWithLifecycle(initialValue = null)
    val darkTheme = storedDarkMode ?: isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = appTypography(),
        content = content
    )
}
