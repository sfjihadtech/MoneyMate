package com.moneymate.app.ui.theme

// =============================================================================
// File: Theme.kt
// Purpose: MoneyMate theme tokens, light/dark palettes, and premium theme selection.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color


// -----------------------------------------------------------------------------
// Section: MoneyMateTokens
// Purpose: Formatting/helper logic for Money Mate Tokens.
// -----------------------------------------------------------------------------
@Immutable
data class MoneyMateTokens(
    val brand: Color,
    val action: Color,
    val success: Color,
    val error: Color,
    val warning: Color,
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val border: Color,
    val divider: Color,
    val lightAction: Color,
    val lightSuccess: Color,
    val lightError: Color,
    val lightWarning: Color
)


// -----------------------------------------------------------------------------
// Section: DefaultLightTokens
// Purpose: Encapsulates the Default Light Tokens section of this file.
// -----------------------------------------------------------------------------
val DefaultLightTokens = MoneyMateTokens(
    brand = MMBrand,
    action = MMAction,
    success = MMSuccess,
    error = MMError,
    warning = MMWarning,
    background = MMBackground,
    surface = MMSurface,
    elevatedSurface = MMSurface,
    primaryText = MMPrimaryText,
    secondaryText = MMSecondaryText,
    mutedText = MMMutedText,
    border = MMBorder,
    divider = MMDivider,
    lightAction = MMLightAction,
    lightSuccess = MMLightSuccess,
    lightError = MMLightError,
    lightWarning = MMLightWarning
)


// -----------------------------------------------------------------------------
// Section: DefaultDarkTokens
// Purpose: Encapsulates the Default Dark Tokens section of this file.
// -----------------------------------------------------------------------------
val DefaultDarkTokens = MoneyMateTokens(
    brand = MMBrand,
    action = MMAction,
    success = MMSuccess,
    error = MMError,
    warning = MMWarning,
    background = MMDarkBackground,
    surface = MMDarkSurface,
    elevatedSurface = MMDarkElevated,
    primaryText = MMDarkPrimaryText,
    secondaryText = MMDarkSecondaryText,
    mutedText = MMDarkMutedText,
    border = MMDarkBorder,
    divider = MMDarkDivider,
    lightAction = MMDarkElevated,
    lightSuccess = MMDarkElevated,
    lightError = MMDarkElevated,
    lightWarning = MMDarkElevated
)


// -----------------------------------------------------------------------------
// Section: LocalMoneyMateTokens
// Purpose: Encapsulates the Local Money Mate Tokens section of this file.
// -----------------------------------------------------------------------------
val LocalMoneyMateTokens = staticCompositionLocalOf { DefaultLightTokens }


// -----------------------------------------------------------------------------
// Section: MoneyMateThemeRuntime
// Purpose: Formatting/helper logic for Money Mate Theme Runtime.
// -----------------------------------------------------------------------------
object MoneyMateThemeRuntime {
    var premiumThemeName by mutableStateOf("Default")
}



// -----------------------------------------------------------------------------
// Section: LightScheme
// Purpose: Encapsulates the Light Scheme section of this file.
// -----------------------------------------------------------------------------
private val LightScheme = lightColorScheme(
    primary = MMAction,
    onPrimary = Color.White,
    primaryContainer = MMLightAction,
    onPrimaryContainer = MMBrand,
    secondary = MMBrand,
    onSecondary = Color.White,
    tertiary = MMSuccess,
    onTertiary = Color.White,
    error = MMError,
    onError = Color.White,
    background = MMBackground,
    onBackground = MMPrimaryText,
    surface = MMSurface,
    onSurface = MMPrimaryText,
    surfaceVariant = MMLightAction,
    onSurfaceVariant = MMSecondaryText,
    outline = MMBorder,
    outlineVariant = MMDivider
)


// -----------------------------------------------------------------------------
// Section: DarkScheme
// Purpose: Encapsulates the Dark Scheme section of this file.
// -----------------------------------------------------------------------------
private val DarkScheme = darkColorScheme(
    primary = MMAction,
    onPrimary = Color.White,
    primaryContainer = MMDarkElevated,
    onPrimaryContainer = MMDarkPrimaryText,
    secondary = Color(0xFFCBD5E1),
    onSecondary = MMDarkBackground,
    tertiary = MMSuccess,
    onTertiary = MMDarkBackground,
    error = MMError,
    onError = Color.White,
    background = MMDarkBackground,
    onBackground = MMDarkPrimaryText,
    surface = MMDarkSurface,
    onSurface = MMDarkPrimaryText,
    surfaceVariant = MMDarkElevated,
    onSurfaceVariant = MMDarkSecondaryText,
    outline = MMDarkBorder,
    outlineVariant = MMDarkDivider
)


// -----------------------------------------------------------------------------
// Section: MoneyMateTheme
// Purpose: Formatting/helper logic for Money Mate Theme.
// -----------------------------------------------------------------------------
@Composable
fun MoneyMateTheme(
    darkTheme: Boolean = false,
    premiumThemeName: String? = null,
    content: @Composable () -> Unit
) {
    val premium = premiumThemeName?.let { name -> PremiumThemes.firstOrNull { it.name == name } }
    val tokens = when {
        premium == null && darkTheme -> DefaultDarkTokens
        premium == null -> DefaultLightTokens
        darkTheme -> MoneyMateTokens(
            brand = premium.brand, action = premium.action, success = premium.success, error = premium.error, warning = premium.warning,
            background = premium.darkBackground, surface = premium.darkSurface, elevatedSurface = premium.darkElevatedSurface,
            primaryText = premium.darkPrimaryText, secondaryText = premium.darkSecondaryText, mutedText = premium.darkMutedText,
            border = premium.darkBorder, divider = premium.darkDivider, lightAction = premium.darkElevatedSurface,
            lightSuccess = premium.darkElevatedSurface, lightError = premium.darkElevatedSurface, lightWarning = premium.darkElevatedSurface
        )
        else -> MoneyMateTokens(
            brand = premium.brand, action = premium.action, success = premium.success, error = premium.error, warning = premium.warning,
            background = premium.background, surface = premium.surface, elevatedSurface = premium.surface,
            primaryText = premium.primaryText, secondaryText = premium.secondaryText, mutedText = premium.mutedText, border = premium.border,
            divider = premium.divider, lightAction = premium.lightAction, lightSuccess = premium.lightSuccess,
            lightError = premium.lightError, lightWarning = premium.lightWarning
        )
    }
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary=tokens.action,onPrimary=Color.White,secondary=tokens.brand,tertiary=tokens.success,error=tokens.error,
            background=tokens.background,onBackground=tokens.primaryText,surface=tokens.surface,onSurface=tokens.primaryText,
            surfaceVariant=tokens.elevatedSurface,onSurfaceVariant=tokens.secondaryText,outline=tokens.border,outlineVariant=tokens.divider
        )
    } else {
        lightColorScheme(
            primary=tokens.action,onPrimary=Color.White,secondary=tokens.brand,tertiary=tokens.success,error=tokens.error,
            background=tokens.background,onBackground=tokens.primaryText,surface=tokens.surface,onSurface=tokens.primaryText,
            surfaceVariant=tokens.lightAction,onSurfaceVariant=tokens.secondaryText,outline=tokens.border,outlineVariant=tokens.divider
        )
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalMoneyMateTokens provides tokens) {
        MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
    }
}

// Premium themes are applied only after the user explicitly selects one; the default app remains on the Default Template color system.
// Every token below is sourced from Premium_Template.pdf.

// -----------------------------------------------------------------------------
// Section: PremiumThemeSpec
// Purpose: Encapsulates the Premium Theme Spec section of this file.
// -----------------------------------------------------------------------------
data class PremiumThemeSpec(
    val name: String,
    val brand: Color,
    val action: Color,
    val success: Color,
    val error: Color,
    val warning: Color,
    val background: Color,
    val surface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val border: Color,
    val divider: Color,
    val lightAction: Color,
    val lightSuccess: Color,
    val lightError: Color,
    val lightWarning: Color,
    val darkBackground: Color = MMDarkBackground,
    val darkSurface: Color = MMDarkSurface,
    val darkElevatedSurface: Color = MMDarkElevated,
    val darkPrimaryText: Color = MMDarkPrimaryText,
    val darkSecondaryText: Color = MMDarkSecondaryText,
    val darkMutedText: Color = MMDarkMutedText,
    val darkBorder: Color = MMDarkBorder,
    val darkDivider: Color = MMDarkDivider
)


// -----------------------------------------------------------------------------
// Section: PremiumThemes
// Purpose: Encapsulates the Premium Themes section of this file.
// -----------------------------------------------------------------------------
val PremiumThemes = listOf(
    PremiumThemeSpec(
        name = "Midnight Sapphire",
        brand = Color(0xFF0B1F33), action = Color(0xFF2563EB), success = Color(0xFF10B981),
        error = Color(0xFFD92D20), warning = Color(0xFFF79009), background = Color(0xFFF7F9FC),
        surface = Color.White, primaryText = Color(0xFF101828), secondaryText = Color(0xFF667085),
        mutedText = Color(0xFF98A2B3), border = Color(0xFFE4E7EC), divider = Color(0xFFF0F2F5),
        lightAction = Color(0xFFE8F0FF), lightSuccess = Color(0xFFECFDF3), lightError = Color(0xFFFEF3F2),
        lightWarning = Color(0xFFFFFAEB)
    ),
    PremiumThemeSpec(
        name = "Royal Indigo",
        brand = Color(0xFF1E1B4B), action = Color(0xFF6366F1), success = Color(0xFF14B8A6),
        error = Color(0xFFE11D48), warning = Color(0xFFF59E0B), background = Color(0xFFF8F7FF),
        surface = Color.White, primaryText = Color(0xFF17152F), secondaryText = Color(0xFF6B6A82),
        mutedText = Color(0xFF9B9AAF), border = Color(0xFFE7E5F2), divider = Color(0xFFF0EFF6),
        lightAction = Color(0xFFEEF2FF), lightSuccess = Color(0xFFECFDF8), lightError = Color(0xFFFFF1F4),
        lightWarning = Color(0xFFFFFBEB)
    ),
    PremiumThemeSpec(
        name = "Graphite Emerald",
        brand = Color(0xFF17221D), action = Color(0xFF0F766E), success = Color(0xFF22C55E),
        error = Color(0xFFDC2626), warning = Color(0xFFD97706), background = Color(0xFFF6FAF8),
        surface = Color.White, primaryText = Color(0xFF142019), secondaryText = Color(0xFF607066),
        mutedText = Color(0xFF96A39B), border = Color(0xFFDDE7E1), divider = Color(0xFFEDF2EF),
        lightAction = Color(0xFFE7F6F3), lightSuccess = Color(0xFFECFDF3), lightError = Color(0xFFFEF2F2),
        lightWarning = Color(0xFFFFFAEB)
    ),
    PremiumThemeSpec(
        name = "Executive Plum",
        brand = Color(0xFF24152F), action = Color(0xFF7C3AED), success = Color(0xFF10B981),
        error = Color(0xFFDC2626), warning = Color(0xFFF59E0B), background = Color(0xFFFBF9FC),
        surface = Color.White, primaryText = Color(0xFF201526), secondaryText = Color(0xFF756B79),
        mutedText = Color(0xFFA49BA8), border = Color(0xFFE9E1EC), divider = Color(0xFFF2EEF4),
        lightAction = Color(0xFFF3EEFF), lightSuccess = Color(0xFFECFDF3), lightError = Color(0xFFFFF1F2),
        lightWarning = Color(0xFFFFFBEB)
    ),
    PremiumThemeSpec(
        name = "Titanium Blue",
        brand = Color(0xFF18212F), action = Color(0xFF0284C7), success = Color(0xFF16A34A),
        error = Color(0xFFDC2626), warning = Color(0xFFF59E0B), background = Color(0xFFF7FAFC),
        surface = Color.White, primaryText = Color(0xFF17202B), secondaryText = Color(0xFF64748B),
        mutedText = Color(0xFF94A3B8), border = Color(0xFFE2E8F0), divider = Color(0xFFEEF2F6),
        lightAction = Color(0xFFE0F2FE), lightSuccess = Color(0xFFECFDF3), lightError = Color(0xFFFEF2F2),
        lightWarning = Color(0xFFFFFAEB)
    )
)
