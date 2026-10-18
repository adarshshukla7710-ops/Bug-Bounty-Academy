package com.bugbounty.academy.ui.theme

import androidx.compose.ui.graphics.Color

// Cyber Terminal Dark Palette
val CyberDarkBg = Color(0xFF0B0F19)
val CyberDarkSurface = Color(0xFF151B28)
val CyberDarkSurfaceVariant = Color(0xFF1E2638)
val CyberDarkBorder = Color(0xFF2E384D)

// Primary Accents
val CyberCyan = Color(0xFF00E5FF)
val CyberCyanDark = Color(0xFF00B0FF)
val CyberGreen = Color(0xFF00E676)
val CyberPurple = Color(0xFF7C4DFF)
val CyberAmber = Color(0xFFFFB300)
val CyberRed = Color(0xFFFF5252)

// Text Colors
val TextPrimary = Color(0xFFF0F6FC)
val TextSecondary = Color(0xFF8B949E)
val TextMuted = Color(0xFF6E7681)
val TerminalText = Color(0xFF50FA7B)
val TerminalBg = Color(0xFF080C14)

// Light Palette Equivalents
val CyberLightBg = Color(0xFFF4F6FB)
val CyberLightSurface = Color(0xFFFFFFFF)
val CyberLightSurfaceVariant = Color(0xFFE8EDF5)
val CyberLightBorder = Color(0xFFD0D7DE)
val TextLightPrimary = Color(0xFF1F2328)
val TextLightSecondary = Color(0xFF656D76)

fun parseHexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    val colorLong = clean.toLongOrNull(16) ?: 0xFF3B82F6
    return if (clean.length == 6) {
        Color(0xFF000000 or colorLong)
    } else {
        Color(colorLong)
    }
}
