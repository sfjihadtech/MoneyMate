package com.moneymate.app.data.local

// =============================================================================
// File: TokenManager.kt
// Purpose: Stores and retrieves authentication/session tokens from app-private local storage.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores a remembered JWT encrypted with an Android Keystore AES key.
 * Non-remembered sessions live only in process memory.
 */

// -----------------------------------------------------------------------------
// Section: TokenManager
// Purpose: Encapsulates the Token Manager section of this file.
// -----------------------------------------------------------------------------
class TokenManager(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String, rememberMe: Boolean = true) {
        memoryToken = token
        if (!rememberMe) {
            preferences.edit().remove(KEY_TOKEN).remove(KEY_IV).apply()
            return
        }
        runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            preferences.edit()
                .putString(KEY_TOKEN, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .apply()
        }.onFailure {
            // Do not silently fall back to plaintext token persistence.
            preferences.edit().remove(KEY_TOKEN).remove(KEY_IV).apply()
        }
    }

    fun getToken(): String? {
        memoryToken?.let { return it }
        val payload = preferences.getString(KEY_TOKEN, null) ?: return null
        val iv = preferences.getString(KEY_IV, null) ?: return null
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
            )
            String(cipher.doFinal(Base64.decode(payload, Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrNull()?.also { memoryToken = it } ?: run {
            clearToken()
            null
        }
    }

    fun getAuthorizationHeader(): String? = getToken()?.trim()?.takeIf { it.isNotEmpty() }?.let { "Bearer $it" }
    fun hasToken(): Boolean = !getToken().isNullOrBlank()

    fun clearToken() {
        memoryToken = null
        preferences.edit().remove(KEY_TOKEN).remove(KEY_IV).apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val PREF_NAME = "moneymate_auth"
        private const val KEY_TOKEN = "auth_token_ciphertext"
        private const val KEY_IV = "auth_token_iv"
        private const val KEY_ALIAS = "MoneyMateAuthKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        @Volatile private var memoryToken: String? = null
    }
}
