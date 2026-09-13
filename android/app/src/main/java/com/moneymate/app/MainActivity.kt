package com.moneymate.app

// =============================================================================
// File: MainActivity.kt
// Purpose: Application entry point. Sets up edge-to-edge Compose UI, preferences, theme state, and navigation.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.moneymate.app.core.navigation.AppNavigation
import com.moneymate.app.data.local.AppPreferences
import com.moneymate.app.ui.theme.MoneyMateTheme
import com.moneymate.app.ui.theme.MoneyMateThemeRuntime


// -----------------------------------------------------------------------------
// Section: MainActivity
// Purpose: Encapsulates the Main Activity section of this file.
// -----------------------------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = remember { AppPreferences(applicationContext) }
            var darkMode by remember { mutableStateOf(prefs.darkMode) }
            MoneyMateThemeRuntime.premiumThemeName = prefs.selectedPremiumTheme
            MoneyMateTheme(darkTheme = darkMode, premiumThemeName = MoneyMateThemeRuntime.premiumThemeName) {
                AppNavigation { enabled ->
                    prefs.darkMode = enabled
                    darkMode = enabled
                }
            }
        }
    }
}
