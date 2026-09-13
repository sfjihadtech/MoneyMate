package com.moneymate.app.core.common

// =============================================================================
// File: LauncherIconManager.kt
// Purpose: Controls launcher activity-alias state for alternate MoneyMate launcher icons.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager


// -----------------------------------------------------------------------------
// Section: LauncherIconManager
// Purpose: Encapsulates the Launcher Icon Manager section of this file.
// -----------------------------------------------------------------------------
object LauncherIconManager {
    private val aliases = mapOf(
        "Classic White" to "com.moneymate.app.LauncherClassic",
        "Classic" to "com.moneymate.app.LauncherClassic",
        "Midnight" to "com.moneymate.app.LauncherMidnight",
        "Gold Premium" to "com.moneymate.app.LauncherGold",
        "Forest" to "com.moneymate.app.LauncherForest",
        "Ocean" to "com.moneymate.app.LauncherOcean",
        "Sunset" to "com.moneymate.app.LauncherSunset",
        "Royal" to "com.moneymate.app.LauncherRoyal",
        "Rose" to "com.moneymate.app.LauncherRose"
    )

    fun apply(context: Context, id: String) {
        val pm = context.packageManager
        val target = aliases[id] ?: aliases.getValue("Classic White")
        aliases.values.toSet().forEach { className ->
            val state = if (className == target) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(
                ComponentName(context.packageName, className),
                state,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
