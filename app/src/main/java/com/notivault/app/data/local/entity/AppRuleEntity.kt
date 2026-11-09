package com.notivault.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    // Default is true: intercept and hide from status bar
    val shouldIntercept: Boolean = true,
    // System critical apps (Phone dialer, emergency, alarms) that must never be cancelled
    val isProtected: Boolean = false,
    // Explicitly marked as excluded by user
    val isExcluded: Boolean = false
)
