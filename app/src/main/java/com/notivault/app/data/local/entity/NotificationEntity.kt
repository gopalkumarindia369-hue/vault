package com.notivault.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["notificationKey"]),
        Index(value = ["packageName"]),
        Index(value = ["timestamp"]),
        Index(value = ["isRead"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationKey: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val subText: String?,
    val timestamp: Long,
    val category: String?,
    val channelId: String?,
    val isIntercepted: Boolean,
    val isOngoing: Boolean,
    val isRead: Boolean = false,
    val contentHash: String
)
