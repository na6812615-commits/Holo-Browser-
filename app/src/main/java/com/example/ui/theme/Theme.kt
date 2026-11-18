package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val HoloLightColorScheme = lightColorScheme(
    primary = HoloBluePrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = HoloCyanAccent,
    onSecondary = Color.White,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = HoloVioletAccent,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = DangerRed,
    onError = Color.White
)

private val HoloDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF1E1B4B),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val HoloIncognitoColorScheme = darkColorScheme(
    primary = IncognitoPrimary,
    onPrimary = IncognitoOnPrimary,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = IncognitoSecondary,
    onSecondary = Color.White,
    secondaryContainer = IncognitoSurfaceVariant,
    onSecondaryContainer = IncognitoOnSurface,
    tertiary = Color(0xFFC4B5FD),
    background = IncognitoBackground,
    onBackground = IncognitoOnSurface,
    surface = IncognitoSurface,
    onSurface = IncognitoOnSurface,
    surfaceVariant = IncognitoSurfaceVariant,
    onSurfaceVariant = IncognitoOnSurfaceVariant,
    outline = IncognitoOutline,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

val HoloShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun HoloBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isIncognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isIncognito -> HoloIncognitoColorScheme
        darkTheme -> HoloDarkColorScheme
        else -> HoloLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HoloTypography,
        shapes = HoloShapes,
        content = content
    )
}
