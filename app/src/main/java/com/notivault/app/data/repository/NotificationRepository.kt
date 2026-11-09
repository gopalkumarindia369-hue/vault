package com.notivault.app.data.repository

import com.notivault.app.data.local.dao.AppSummary
import com.notivault.app.data.local.dao.NotificationDao
import com.notivault.app.data.local.entity.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NotificationRepository(private val notificationDao: NotificationDao) {

    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    val unreadCount: Flow<Int> = notificationDao.getUnreadCountFlow()
    val distinctApps: Flow<List<AppSummary>> = notificationDao.getDistinctAppsFlow()

    fun getFiltered(
        packageName: String?,
        searchQuery: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<NotificationEntity>> {
        return notificationDao.getFilteredNotificationsFlow(packageName, searchQuery, startDate, endDate)
    }

    suspend fun getNotificationById(id: Long): NotificationEntity? = withContext(Dispatchers.IO) {
        notificationDao.getNotificationById(id)
    }

    suspend fun insertNotification(notification: NotificationEntity): Long = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(notification)
    }

    suspend fun getLatestByKey(key: String): NotificationEntity? = withContext(Dispatchers.IO) {
        notificationDao.getLatestByKey(key)
    }

    suspend fun markAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.deleteById(id)
    }

    suspend fun deleteByIds(ids: List<Long>) = withContext(Dispatchers.IO) {
        notificationDao.deleteByIds(ids)
    }

    suspend fun deleteByPackage(packageName: String) = withContext(Dispatchers.IO) {
        notificationDao.deleteByPackage(packageName)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        notificationDao.deleteAll()
    }

    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int = withContext(Dispatchers.IO) {
        notificationDao.deleteOlderThan(cutoffTimestamp)
    }
}
