package com.notivault.app.ui.screens.rules

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notivault.app.NotiVaultApplication
import com.notivault.app.data.local.entity.AppRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val shouldIntercept: Boolean,
    val isProtected: Boolean
)

class RulesViewModel : ViewModel() {

    private val rulesRepo = NotiVaultApplication.instance.appRuleRepository
    private val prefsRepo = NotiVaultApplication.instance.preferencesRepository

    val searchQuery = MutableStateFlow("")

    val rules = rulesRepo.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleAppIntercept(app: InstalledAppItem) {
        if (app.isProtected) return
        viewModelScope.launch {
            rulesRepo.setRule(
                packageName = app.packageName,
                appName = app.appName,
                shouldIntercept = !app.shouldIntercept,
                isProtected = app.isProtected
            )
        }
    }

    fun setAll(intercept: Boolean) {
        viewModelScope.launch {
            rulesRepo.setAllInterceptStatus(intercept)
        }
    }
}
