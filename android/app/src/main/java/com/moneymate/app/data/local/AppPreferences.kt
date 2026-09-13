package com.moneymate.app.data.local

// =============================================================================
// File: AppPreferences.kt
// Purpose: Persistent local preferences for theme, language, currency, security, and other app settings.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.time.LocalDate


// -----------------------------------------------------------------------------
// Section: AppPreferences
// Purpose: Encapsulates the App Preferences section of this file.
// -----------------------------------------------------------------------------
class AppPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("moneymate_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var onboardingComplete: Boolean
        get() = prefs.getBoolean("onboarding_complete", false)
        set(value) = prefs.edit().putBoolean("onboarding_complete", value).apply()

    var darkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(value) = prefs.edit().putBoolean("dark_mode", value).apply()

    var language: String
        get() = prefs.getString("language", "en") ?: "en"
        set(value) = prefs.edit().putString("language", value).apply()

    var currency: String
        get() = prefs.getString("currency", "USD") ?: "USD"
        set(value) = prefs.edit().putString("currency", value).apply()

    var pushNotifications: Boolean
        get() = prefs.getBoolean("push_notifications", true)
        set(value) = prefs.edit().putBoolean("push_notifications", value).apply()

    var highContrast: Boolean
        get() = prefs.getBoolean("high_contrast", false)
        set(value) = prefs.edit().putBoolean("high_contrast", value).apply()

    var largeText: Boolean
        get() = prefs.getBoolean("large_text", false)
        set(value) = prefs.edit().putBoolean("large_text", value).apply()

    var reduceMotion: Boolean
        get() = prefs.getBoolean("reduce_motion", false)
        set(value) = prefs.edit().putBoolean("reduce_motion", value).apply()

    var biometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", false)
        set(value) = prefs.edit().putBoolean("biometric_enabled", value).apply()

    var autoLockMinutes: Int
        get() = prefs.getInt("auto_lock_minutes", 1)
        set(value) = prefs.edit().putInt("auto_lock_minutes", value).apply()

    var selectedPremiumTheme: String
        get() = prefs.getString("premium_theme", "Default") ?: "Default"
        set(value) = prefs.edit().putString("premium_theme", value).apply()

    var selectedAppIcon: String
        get() = prefs.getString("app_icon", "Classic") ?: "Classic"
        set(value) = prefs.edit().putString("app_icon", value).apply()

    var premiumPlan: String?
        get() = prefs.getString("premium_plan", null)
        set(value) = prefs.edit().putString("premium_plan", value).apply()

    val trialStartedEpochDay: Long
        get() {
            val existing = prefs.getLong("trial_started", Long.MIN_VALUE)
            if (existing != Long.MIN_VALUE) return existing
            val today = LocalDate.now().toEpochDay()
            prefs.edit().putLong("trial_started", today).apply()
            return today
        }

    val trialDaysLeft: Int
        get() = (7 - (LocalDate.now().toEpochDay() - trialStartedEpochDay).toInt()).coerceIn(0, 7)

    val hasFullAccess: Boolean
        get() = premiumPlan != null || trialDaysLeft > 0

    fun setPin(pin: String?) {
        if (pin.isNullOrBlank()) {
            prefs.edit().remove("pin_hash").remove("pin_salt").apply()
            return
        }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = pinHash(pin, salt)
        prefs.edit()
            .putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("pin_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
    }

    fun hasPin(): Boolean = !prefs.getString("pin_hash", null).isNullOrBlank()

    fun verifyPin(pin: String): Boolean {
        val saltEncoded = prefs.getString("pin_salt", null) ?: return false
        val expected = prefs.getString("pin_hash", null) ?: return false
        return runCatching {
            val salt = Base64.decode(saltEncoded, Base64.NO_WRAP)
            Base64.encodeToString(pinHash(pin, salt), Base64.NO_WRAP) == expected
        }.getOrDefault(false)
    }

    fun recurring(): List<RecurringTemplate> {
        val json = prefs.getString("recurring", null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<RecurringTemplate>>() {}.type
            gson.fromJson<List<RecurringTemplate>>(json, type)
        }.getOrDefault(emptyList())
    }

    fun saveRecurring(items: List<RecurringTemplate>) {
        prefs.edit().putString("recurring", gson.toJson(items)).apply()
    }

    private fun pinHash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 100_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}


// -----------------------------------------------------------------------------
// Section: RecurringTemplate
// Purpose: Encapsulates the Recurring Template section of this file.
// -----------------------------------------------------------------------------
data class RecurringTemplate(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val type: String,
    val amount: Double,
    val accountId: Int,
    val categoryId: Int? = null,
    val frequency: String = "monthly",
    val nextDate: String,
    val enabled: Boolean = true
)
