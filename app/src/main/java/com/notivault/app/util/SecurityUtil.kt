package com.notivault.app.util

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object SecurityUtil {

    private const val PREFS_NAME = "notivault_secure_keystore_prefs"
    private const val KEY_DB_PASSPHRASE = "encrypted_db_passphrase_key"

    /**
     * Retrieves or generates a 256-bit (32-byte) cryptographic passphrase
     * for SQLCipher database encryption. Stored safely in EncryptedSharedPreferences
     * backed by the Android Keystore (hardware TEE/StrongBox).
     */
    fun getOrCreateDatabasePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false)
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existingBase64 = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)
        if (existingBase64 != null) {
            return Base64.decode(existingBase64, Base64.DEFAULT)
        }

        // Generate 32 cryptographically secure random bytes (256-bit AES key)
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)

        val encoded = Base64.encodeToString(randomBytes, Base64.DEFAULT)
        encryptedPrefs.edit().putString(KEY_DB_PASSPHRASE, encoded).apply()

        return randomBytes
    }
}
