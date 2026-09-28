package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = StaticMedicalTeal,
    onPrimary = Color.White,
    primaryContainer = StaticMedicalTealLight,
    onPrimaryContainer = StaticMedicalTealDark,
    secondary = StaticMedicalBlue,
    onSecondary = Color.White,
    secondaryContainer = StaticMedicalBlueLight,
    onSecondaryContainer = Color(0xFF00294A),
    background = StaticMedicalBackground,
    onBackground = StaticMedicalTextPrimary,
    surface = StaticMedicalSurface,
    onSurface = StaticMedicalTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = StaticMedicalTextSecondary,
    outline = StaticMedicalCardBorder,
    outlineVariant = StaticMedicalCardBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF0B1118), // Deep non-reflective clinical dark background
    onBackground = Color(0xFFF1F5F9), // Crisp clear text
    surface = Color(0xFF131D28), // Distinct clinical dark card surface
    onSurface = Color(0xFFF8FAFC), // High contrast readable card text
    surfaceVariant = Color(0xFF1A2634), // Subtle inner containers
    onSurfaceVariant = Color(0xFF94A3B8), // Muted labels
    outline = Color(0xFF243647), // Distinct card border
    outlineVariant = Color(0xFF1B2836)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
