package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light colors are styled dark as well because eye-safety themes are requested
private val CosmicColorPalette = darkColorScheme(
    primary = CosmicPrimarySilv,
    secondary = CosmicAccentSilver,
    tertiary = CosmicPrimarySilv,
    background = CosmicBackground,
    surface = CosmicCardBg,
    onPrimary = CosmicTextOnPrimary,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val CharcoalGoldPalette = darkColorScheme(
    primary = CharcoalPrimaryGold,
    secondary = CharcoalAccentGold,
    tertiary = CharcoalPrimaryGold,
    background = CharcoalBackground,
    surface = CharcoalCardBg,
    onPrimary = CharcoalTextOnPrimary,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val RoyalEmeraldPalette = darkColorScheme(
    primary = EmeraldPrimaryGreen,
    secondary = EmeraldAccentGreen,
    tertiary = EmeraldPrimaryGreen,
    background = EmeraldBackground,
    surface = EmeraldCardBg,
    onPrimary = EmeraldTextOnPrimary,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun DynamicAppTheme(
    themeName: String,
    textColorName: String,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Charcoal Gold" -> CharcoalGoldPalette
        "Royal Emerald" -> RoyalEmeraldPalette
        else -> CosmicColorPalette
    }

    val finalTypography = Typography

    MaterialTheme(
        colorScheme = colorScheme,
        typography = finalTypography,
        content = content
    )
}

fun getSelectedTextColor(name: String): Color {
    return when (name) {
        "Light Gold" -> LightGoldText
        "Vibrant Silver" -> VibrantSilverText
        else -> BrightWhiteText
    }
}
