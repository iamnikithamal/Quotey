package com.quotey.create.ui.theme

import androidx.compose.ui.graphics.Color

// Quotey Brand Colors - Soft Sage, Dusty Rose, and Creamy Palette

// Primary - Soft Sage Green
val Primary = Color(0xFF8FB996)
val PrimaryLight = Color(0xFFB5D4BC)
val PrimaryDark = Color(0xFF6A9B72)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD8EDD9)
val OnPrimaryContainer = Color(0xFF1B3D1F)

// Secondary - Dusty Rose Pink
val Secondary = Color(0xFFD4A5A5)
val SecondaryLight = Color(0xFFF0D0D0)
val SecondaryDark = Color(0xFFB88585)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFFFE9E9)
val OnSecondaryContainer = Color(0xFF3D1F1F)

// Tertiary - Creamy Peach
val Tertiary = Color(0xFFE8C8A9)
val TertiaryLight = Color(0xFFFFF0DD)
val TertiaryDark = Color(0xFFC9A88A)
val OnTertiary = Color(0xFF3D3020)
val TertiaryContainer = Color(0xFFFFF4E8)
val OnTertiaryContainer = Color(0xFF3D2E1F)

// Surface colors - Light Theme
val Surface = Color(0xFFFFFBF8)
val SurfaceVariant = Color(0xFFF5EDE8)
val OnSurface = Color(0xFF1C1B1B)
val OnSurfaceVariant = Color(0xFF4A4544)
val SurfaceContainer = Color(0xFFF8F2EF)
val SurfaceContainerHigh = Color(0xFFFFF8F5)
val SurfaceContainerLow = Color(0xFFF2ECE9)
val SurfaceContainerHighest = Color(0xFFFAF4F1)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceDim = Color(0xFFE0DAD7)
val SurfaceBright = Color(0xFFFFFBF8)

// Background
val Background = Color(0xFFFFFBF8)
val OnBackground = Color(0xFF1C1B1B)

// Error
val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

// Outline
val Outline = Color(0xFF7D7573)
val OutlineVariant = Color(0xFFD0C4C1)

// Inverse
val InverseSurface = Color(0xFF322F2E)
val InverseOnSurface = Color(0xFFF6EFEC)
val InversePrimary = Color(0xFF9DD5A4)

// Scrim
val Scrim = Color(0xFF000000)

// Dark Theme Colors
val PrimaryDarkTheme = Color(0xFF9DD5A4)
val OnPrimaryDarkTheme = Color(0xFF003910)
val PrimaryContainerDarkTheme = Color(0xFF00531B)
val OnPrimaryContainerDarkTheme = Color(0xFFB8F1BE)

val SecondaryDarkTheme = Color(0xFFE8B8B8)
val OnSecondaryDarkTheme = Color(0xFF442727)
val SecondaryContainerDarkTheme = Color(0xFF5D3D3D)
val OnSecondaryContainerDarkTheme = Color(0xFFFFD9D9)

val TertiaryDarkTheme = Color(0xFFD4B89B)
val OnTertiaryDarkTheme = Color(0xFF3A2A17)
val TertiaryContainerDarkTheme = Color(0xFF53402B)
val OnTertiaryContainerDarkTheme = Color(0xFFF1DBC5)

val SurfaceDarkTheme = Color(0xFF141312)
val SurfaceVariantDarkTheme = Color(0xFF4A4544)
val OnSurfaceDarkTheme = Color(0xFFE7E1DE)
val OnSurfaceVariantDarkTheme = Color(0xFFD0C4C1)
val SurfaceContainerDarkTheme = Color(0xFF201F1E)
val SurfaceContainerHighDarkTheme = Color(0xFF2B2928)
val SurfaceContainerLowDarkTheme = Color(0xFF1C1B1A)
val SurfaceContainerHighestDarkTheme = Color(0xFF363433)
val SurfaceContainerLowestDarkTheme = Color(0xFF0F0E0D)
val SurfaceDimDarkTheme = Color(0xFF141312)
val SurfaceBrightDarkTheme = Color(0xFF3A3938)

val BackgroundDarkTheme = Color(0xFF141312)
val OnBackgroundDarkTheme = Color(0xFFE7E1DE)

val OutlineDarkTheme = Color(0xFF9A8F8C)
val OutlineVariantDarkTheme = Color(0xFF4A4544)

val InverseSurfaceDarkTheme = Color(0xFFE7E1DE)
val InverseOnSurfaceDarkTheme = Color(0xFF322F2E)
val InversePrimaryDarkTheme = Color(0xFF006D26)

// Preset Background Colors for Editor
object PresetColors {
    val SolidColors = listOf(
        Color(0xFFFFFFFF), // White
        Color(0xFF000000), // Black
        Color(0xFFF5F5F5), // Light Gray
        Color(0xFF2D2D2D), // Dark Gray
        Color(0xFFFFF8E7), // Cream
        Color(0xFFF0E6D3), // Beige
        Color(0xFFE8F5E9), // Mint Green
        Color(0xFFF3E5F5), // Lavender
        Color(0xFFFFF3E0), // Peach
        Color(0xFFE3F2FD), // Sky Blue
        Color(0xFFFCE4EC), // Blush Pink
        Color(0xFFE0F7FA), // Aqua
        Color(0xFFFFF9C4), // Soft Yellow
        Color(0xFFFFEBEE), // Rose
        Color(0xFFE8EAF6), // Soft Indigo
        Color(0xFFEFEBE9), // Warm Gray
        Color(0xFF8FB996), // Sage Green (Primary)
        Color(0xFFD4A5A5), // Dusty Rose (Secondary)
        Color(0xFFE8C8A9), // Creamy Peach (Tertiary)
        Color(0xFF5C6BC0), // Indigo
        Color(0xFF26A69A), // Teal
        Color(0xFFFF7043), // Deep Orange
        Color(0xFF7E57C2), // Deep Purple
        Color(0xFF66BB6A), // Green
    )

    val GradientPresets = listOf(
        // Soft & Minimal
        listOf(Color(0xFFFDFBFB), Color(0xFFEBEDEE)),
        listOf(Color(0xFFF5F7FA), Color(0xFFC3CFE2)),
        listOf(Color(0xFFFFE5E5), Color(0xFFFFE5B4)),
        listOf(Color(0xFFD4FC79), Color(0xFF96E6A1)),
        listOf(Color(0xFFA18CD1), Color(0xFFFBC2EB)),

        // Warm Tones
        listOf(Color(0xFFFECEA8), Color(0xFFFF847C)),
        listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF)),
        listOf(Color(0xFFFEDAE1), Color(0xFFFEFBF3)),
        listOf(Color(0xFFFFE985), Color(0xFFFA742B)),
        listOf(Color(0xFFFDCB6E), Color(0xFFE17055)),

        // Cool Tones
        listOf(Color(0xFF89F7FE), Color(0xFF66A6FF)),
        listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB)),
        listOf(Color(0xFFCCFBFF), Color(0xFFEF96C5)),
        listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
        listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),

        // Nature
        listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
        listOf(Color(0xFF8FB996), Color(0xFFD4FC79)),
        listOf(Color(0xFF56AB2F), Color(0xFFA8E063)),
        listOf(Color(0xFF134E5E), Color(0xFF71B280)),
        listOf(Color(0xFF1D976C), Color(0xFF93F9B9)),

        // Sunset/Sunrise
        listOf(Color(0xFFFF6B6B), Color(0xFFFECA57)),
        listOf(Color(0xFFFA709A), Color(0xFFFEE140)),
        listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)),
        listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
        listOf(Color(0xFFFC466B), Color(0xFF3F5EFB)),

        // Dark/Moody
        listOf(Color(0xFF232526), Color(0xFF414345)),
        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
        listOf(Color(0xFF1A1A2E), Color(0xFF16213E)),
        listOf(Color(0xFF2C3E50), Color(0xFF3498DB)),
        listOf(Color(0xFF200122), Color(0xFF6F0000)),

        // Quotey Brand
        listOf(Color(0xFF8FB996), Color(0xFFD4A5A5)),
        listOf(Color(0xFFD4A5A5), Color(0xFFE8C8A9)),
        listOf(Color(0xFF8FB996), Color(0xFFE8C8A9)),
        listOf(Color(0xFF8FB996), Color(0xFFD4A5A5), Color(0xFFE8C8A9)),
    )
}
