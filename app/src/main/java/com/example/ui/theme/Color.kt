package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Static base colors for Theme.kt initial definitions
internal val StaticMedicalTeal = Color(0xFF007A8C)
internal val StaticMedicalTealDark = Color(0xFF005666)
internal val StaticMedicalTealLight = Color(0xFFE2F4F7)
internal val StaticMedicalBlue = Color(0xFF0284C7)
internal val StaticMedicalBlueLight = Color(0xFFE0F2FE)
internal val StaticMedicalBackground = Color(0xFFF4F8FA)
internal val StaticMedicalSurface = Color(0xFFFFFFFF)
internal val StaticMedicalTextPrimary = Color(0xFF0F172A)
internal val StaticMedicalTextSecondary = Color(0xFF475569)
internal val StaticMedicalTextMuted = Color(0xFF94A3B8)
internal val StaticMedicalCardBorder = Color(0xFFE2EAF0)
internal val StaticMedicalCardBorderLight = Color(0xFFEDF2F7)

// Dynamic theme-aware Composable accessors for Primary & Accent colors
val MedicalTeal: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF2DD4BF) else StaticMedicalTeal

val MedicalTealDark = Color(0xFF005666)
val MedicalTealLight = Color(0xFFE2F4F7)

val MedicalBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF38BDF8) else StaticMedicalBlue

val MedicalBlueLight = Color(0xFFE0F2FE)

val MedicalBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val MedicalSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val MedicalSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val MedicalCardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val MedicalCardBorderLight: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF223446) else Color(0xFFEDF2F7)

val MedicalTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val MedicalTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val MedicalTextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF64748B) else Color(0xFF94A3B8)

val MedicalBadgeBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF1E3244) else Color(0xFFE0F2FE)

val MedicalSubtleBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF162330) else Color(0xFFF8FAFC)

// Risk Badges and Indicators
val RiskRed: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFFF87171) else Color(0xFFDC2626)

val RiskRedBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF2D1214) else Color(0xFFFEE2E2)

val RiskRedBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF5C1D1D) else Color(0xFFFECACA)

val RiskOrange: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFD97706)

val RiskOrangeBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF2E1F0A) else Color(0xFFFEF3C7)

val RiskOrangeBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF5E340E) else Color(0xFFFDE68A)

val RiskGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF34D399) else Color(0xFF059669)

val RiskGreenBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF0B291B) else Color(0xFFD1FAE5)

val RiskGreenBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF164E35) else Color(0xFFA7F3D0)

val StatusPurple: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFFC084FC) else Color(0xFF7E22CE)

val StatusPurpleBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF241538) else Color(0xFFEDE9FE)

val StatusPurpleBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF4C1D73) else Color(0xFFDDD6FE)

val StatusPink: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFFF472B6) else Color(0xFFBE185D)

val StatusPinkBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF2E1220) else Color(0xFFFCE7F3)

val StatusPinkBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF5A1E3C) else Color(0xFFFBCFE8)

// Hero Gradients (Theme-aware to eliminate white/glare in dark mode)
val HeroGradientStart: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF0F2B36) else Color(0xFF00758F)

val HeroGradientEnd: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF091920) else Color(0xFF004D5A)

@Composable
@ReadOnlyComposable
fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
