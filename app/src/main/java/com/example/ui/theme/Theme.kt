package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF0E4C6D),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = IndigoAccent,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFC7D2FE),
    tertiary = EmeraldAccent,
    background = MathDarkBackground,
    onBackground = TextPrimary,
    surface = MathDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = MathDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = MathDarkBorder,
    error = RoseAccent,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF6366F1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF4338CA),
    tertiary = Color(0xFF059669),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFE11D48),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep tailored sleek dark mathematical palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // default to dark studio aesthetic

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MathTypography,
        content = content
    )
}
