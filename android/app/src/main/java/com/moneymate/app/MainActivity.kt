package com.moneymate.app

// =============================================================================
// File: MainActivity.kt
// Purpose: Application entry point. Sets up edge-to-edge Compose UI,
//          Android 17 local-network permission, preferences, theme and navigation.
// =============================================================================

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
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
// -----------------------------------------------------------------------------
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            // -----------------------------------------------------------------
            // Android 17 / API 37 local-network permission.
            //
            // ACCESS_LOCAL_NETWORK exists from API 37.
            // The permission itself is declared only in src/debug manifest,
            // therefore the production/release manifest remains unchanged.
            // -----------------------------------------------------------------
            val localNetworkPermissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    // No action is required here.
                    // If granted, local development APIs such as 10.0.2.2
                    // become accessible to the debug application.
                }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= 37) {
                    localNetworkPermissionLauncher.launch(
                        Manifest.permission.ACCESS_LOCAL_NETWORK
                    )
                }
            }

            // -----------------------------------------------------------------
            // Application preferences
            // -----------------------------------------------------------------
            val prefs = remember {
                AppPreferences(applicationContext)
            }

            var darkMode by remember {
                mutableStateOf(prefs.darkMode)
            }

            MoneyMateThemeRuntime.premiumThemeName =
                prefs.selectedPremiumTheme

            // -----------------------------------------------------------------
            // MoneyMate theme + navigation
            // -----------------------------------------------------------------
            MoneyMateTheme(
                darkTheme = darkMode,
                premiumThemeName = MoneyMateThemeRuntime.premiumThemeName
            ) {
                AppNavigation { enabled ->
                    prefs.darkMode = enabled
                    darkMode = enabled
                }
            }
        }
    }
}