package com.notivault.app.ui.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notivault.app.NotiVaultApplication
import com.notivault.app.data.local.dao.AppSummary
import com.notivault.app.data.local.entity.NotificationEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModel : ViewModel() {

    private val repository = NotiVaultApplication.instance.notificationRepository
    private val prefs = NotiVaultApplication.instance.preferencesRepository

    val isMasterIntercept: StateFlow<Boolean> = prefs.isMasterInterceptEnabled
    val unreadCount: StateFlow<Int> = repository.unreadCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val distinctApps: StateFlow<List<AppSummary>> = repository.distinctApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and filters
    val searchQuery = MutableStateFlow("")
    val selectedAppFilter = MutableStateFlow<String?>(null)
    val isGroupedByApp = MutableStateFlow(true)

    // Selection mode for batch delete
    val isSelectionMode = MutableStateFlow(false)
    val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    // Selected notification for detail modal
    val selectedNotification = MutableStateFlow<NotificationEntity?>(null)

    val notifications: StateFlow<List<NotificationEntity>> = combine(
        searchQuery,
        selectedAppFilter
    ) { query, app ->
        Pair(query.takeIf { it.isNotBlank() }, app)
    }.flatMapLatest { (query, app) ->
        repository.getFiltered(
            packageName = app,
            searchQuery = query,
            startDate = null,
            endDate = null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleMasterIntercept() {
        val current = isMasterIntercept.value
        prefs.setMasterIntercept(!current)
    }

    fun toggleSelectionMode() {
        val active = !isSelectionMode.value
        isSelectionMode.value = active
        if (!active) {
            selectedIds.value = emptySet()
        }
    }

    fun toggleSelectItem(id: Long) {
        val set = selectedIds.value.toMutableSet()
        if (set.contains(id)) {
            set.remove(id)
        } else {
            set.add(id)
        }
        selectedIds.value = set
        if (set.isEmpty() && isSelectionMode.value) {
            // Keep selection mode active or allow user to exit
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun deleteSelected() {
        val ids = selectedIds.value.toList()
        viewModelScope.launch {
            repository.deleteByIds(ids)
            selectedIds.value = emptySet()
            isSelectionMode.value = false
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun deleteFromApp(packageName: String) {
        viewModelScope.launch {
            repository.deleteByPackage(packageName)
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }
}
