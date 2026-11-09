package com.notivault.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notivault.app.NotiVaultApplication
import com.notivault.app.util.ExportUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val prefs = NotiVaultApplication.instance.preferencesRepository
    private val repo = NotiVaultApplication.instance.notificationRepository

    val masterIntercept = prefs.isMasterInterceptEnabled
    val defaultAction = prefs.defaultAction
    val autoCleanupDays = prefs.autoCleanupDays
    val isAppLockEnabled = prefs.isAppLockEnabled

    fun setMasterIntercept(enabled: Boolean) = prefs.setMasterIntercept(enabled)
    fun setAutoCleanupDays(days: Int) = prefs.setAutoCleanupDays(days)
    fun setAppLock(enabled: Boolean, pin: String = "") = prefs.setAppLock(enabled, pin)

    fun exportData(context: Context, format: String) {
        viewModelScope.launch {
            val list = repo.allNotifications.first()
            if (format == "json") {
                val file = ExportUtil.exportToJson(context, list)
                ExportUtil.shareExportedFile(context, file, "application/json")
            } else {
                val file = ExportUtil.exportToCsv(context, list)
                ExportUtil.shareExportedFile(context, file, "text/csv")
            }
        }
    }
}
