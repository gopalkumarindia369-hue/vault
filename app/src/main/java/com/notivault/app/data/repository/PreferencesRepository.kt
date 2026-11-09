package com.notivault.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("notivault_prefs", Context.MODE_PRIVATE)

    private val _isMasterInterceptEnabled = MutableStateFlow(prefs.getBoolean(KEY_MASTER_INTERCEPT, true))
    val isMasterInterceptEnabled: StateFlow<Boolean> = _isMasterInterceptEnabled.asStateFlow()

    private val _defaultAction = MutableStateFlow(prefs.getString(KEY_DEFAULT_ACTION, "intercept") ?: "intercept")
    val defaultAction: StateFlow<String> = _defaultAction.asStateFlow()

    private val _autoCleanupDays = MutableStateFlow(prefs.getInt(KEY_AUTO_CLEANUP_DAYS, 30))
    val autoCleanupDays: StateFlow<Int> = _autoCleanupDays.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_APP_LOCK_ENABLED, false))
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _appPinCode = MutableStateFlow(prefs.getString(KEY_APP_PIN, "") ?: "")
    val appPinCode: StateFlow<String> = _appPinCode.asStateFlow()

    fun setMasterIntercept(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MASTER_INTERCEPT, enabled).apply()
        _isMasterInterceptEnabled.value = enabled
    }

    fun setDefaultAction(action: String) {
        prefs.edit().putString(KEY_DEFAULT_ACTION, action).apply()
        _defaultAction.value = action
    }

    fun setAutoCleanupDays(days: Int) {
        prefs.edit().putInt(KEY_AUTO_CLEANUP_DAYS, days).apply()
        _autoCleanupDays.value = days
    }

    fun setAppLock(enabled: Boolean, pin: String = "") {
        prefs.edit()
            .putBoolean(KEY_APP_LOCK_ENABLED, enabled)
            .putString(KEY_APP_PIN, pin)
            .apply()
        _isAppLockEnabled.value = enabled
        _appPinCode.value = pin
    }

    companion object {
        private const val KEY_MASTER_INTERCEPT = "key_master_intercept"
        private const val KEY_DEFAULT_ACTION = "key_default_action"
        private const val KEY_AUTO_CLEANUP_DAYS = "key_auto_cleanup_days"
        private const val KEY_APP_LOCK_ENABLED = "key_app_lock_enabled"
        private const val KEY_APP_PIN = "key_app_pin"
    }
}
