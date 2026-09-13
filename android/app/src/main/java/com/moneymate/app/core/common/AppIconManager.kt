package com.moneymate.app.core.common

// =============================================================================
// File: AppIconManager.kt
// Purpose: Helpers for selecting and managing the app icon state.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager


// -----------------------------------------------------------------------------
// Section: AppIconManager
// Purpose: Encapsulates the App Icon Manager section of this file.
// -----------------------------------------------------------------------------
object AppIconManager {
    private val aliases = linkedMapOf(
        "Classic White" to "LauncherClassic",
        "Midnight" to "LauncherMidnight",
        "Gold Premium" to "LauncherGold",
        "Forest" to "LauncherForest",
        "Ocean" to "LauncherOcean",
        "Sunset" to "LauncherSunset",
        "Royal" to "LauncherRoyal",
        "Rose" to "LauncherRose"
    )

    fun setIcon(context: Context, name: String) {
        val selected = aliases[name] ?: aliases.getValue("Classic White")
        val pm = context.packageManager
        aliases.values.forEach { alias ->
            val component = ComponentName(context.packageName, "${context.packageName}.$alias")
            val state = if (alias == selected)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            pm.setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP)
        }
    }
}
