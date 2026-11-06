package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 1. Dark Premium (Official ZAVA Palette: Navy Blue + Turquoise + Gold + Emerald + Silver)
val DarkPremiumColorScheme = darkColorScheme(
    primary = TurquoiseAccent,
    onPrimary = NavyDeep,
    primaryContainer = NavyCardElevated,
    onPrimaryContainer = TurquoiseLight,
    secondary = GoldAccent,
    onSecondary = NavyDeep,
    secondaryContainer = NavyCard,
    onSecondaryContainer = GoldLight,
    tertiary = EmeraldGreen,
    onTertiary = NavyDeep,
    background = NavyDeep,
    onBackground = SilverWhite,
    surface = NavySurface,
    onSurface = SilverWhite,
    surfaceVariant = NavyCard,
    onSurfaceVariant = SilverMedium,
    outline = NavyBorder,
    error = DangerRed,
    onError = Color.White
)

// 2. Light Minimalist
val LightMinimalistColorScheme = lightColorScheme(
    primary = Color(0xFF0C243B), // Deep Navy
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1), // Light Turquoise
    onPrimaryContainer = Color(0xFF0F766E),
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0C243B),
    surface = Color.White,
    onSurface = Color(0xFF0C243B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = DangerRed,
    onError = Color.White
)

// 3. Purple Luxury
val PurpleLuxuryColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = PurpleLuxuryDeep,
    primaryContainer = PurpleLuxuryCard,
    onPrimaryContainer = GoldLight,
    secondary = TurquoiseAccent,
    onSecondary = NavyDeep,
    secondaryContainer = PurpleLuxurySurface,
    onSecondaryContainer = PurpleLuxuryLight,
    tertiary = EmeraldGreen,
    onTertiary = NavyDeep,
    background = PurpleLuxuryDeep,
    onBackground = SilverWhite,
    surface = PurpleLuxurySurface,
    onSurface = SilverWhite,
    surfaceVariant = PurpleLuxuryCard,
    onSurfaceVariant = SilverMedium,
    outline = Color(0xFF4C1D95),
    error = DangerRed,
    onError = Color.White
)

// 4. Navy Gold (Classic Swiss Navy & Brushed Gold)
val NavyGoldColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyDeep,
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = GoldLight,
    secondary = TurquoiseAccent,
    onSecondary = NavyDeep,
    secondaryContainer = NavyCard,
    onSecondaryContainer = SilverWhite,
    tertiary = EmeraldGreen,
    onTertiary = NavyDeep,
    background = Color(0xFF071A2E),
    onBackground = SilverWhite,
    surface = Color(0xFF0C2340),
    onSurface = SilverWhite,
    surfaceVariant = Color(0xFF132F4C),
    onSurfaceVariant = SilverMedium,
    outline = Color(0xFF224E7D),
    error = DangerRed,
    onError = Color.White
)

fun getCustomColorScheme(accentHex: String, darkTheme: Boolean): ColorScheme {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(accentHex))
    } catch (e: Exception) {
        TurquoiseAccent
    }
    return if (darkTheme) {
        DarkPremiumColorScheme.copy(primary = accentColor)
    } else {
        LightMinimalistColorScheme.copy(primary = accentColor)
    }
}

fun getZavaTypography(isLargeFont: Boolean): Typography {
    val scale = if (isLargeFont) 1.15f else 1.0f
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * scale).sp,
            lineHeight = (40 * scale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = (26 * scale).sp,
            lineHeight = (32 * scale).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (22 * scale).sp,
            lineHeight = (28 * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * scale).sp,
            lineHeight = (26 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * scale).sp,
            lineHeight = (22 * scale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (14 * scale).sp
        )
    )
}

@Composable
fun ZavaTheme(
    themeName: String = "DARK_PREMIUM",
    customAccentHex: String = "#14F1D9",
    isLargeFont: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "LIGHT_MINIMALIST" -> LightMinimalistColorScheme
        "PURPLE_LUXURY" -> PurpleLuxuryColorScheme
        "NAVY_GOLD" -> NavyGoldColorScheme
        "CUSTOM" -> getCustomColorScheme(customAccentHex, darkTheme = true)
        else -> DarkPremiumColorScheme // DARK_PREMIUM is default
    }

    val typography = getZavaTypography(isLargeFont)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
