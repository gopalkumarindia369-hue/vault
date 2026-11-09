package com.notivault.app.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.notivault.app.NotiVaultApplication
import com.notivault.app.data.local.entity.NotificationEntity
import com.notivault.app.util.NotificationExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NotificationInterceptorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Known critical apps that should never be intercepted from status bar
    private val protectedPackages = setOf(
        "com.google.android.dialer",
        "com.android.server.telecom",
        "com.samsung.android.dialer",
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.android.emergency"
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "NotiVault NotificationListenerService connected successfully")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "NotificationListenerService disconnected! Requesting rebind...")
        // Request rebind to survive system resource cleanup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotificationInterceptorService::class.java))
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkgName = sbn.packageName

        // 1. Skip our own app's notifications to prevent infinite loops
        if (pkgName == packageName) return

        val notification = sbn.notification ?: return

        // 2. Extract title, text, subtext, bigText
        val extracted = NotificationExtractor.extract(this, sbn)

        // 3. Check if ongoing / foreground service / non-clearable
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 ||
                (notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0 ||
                (notification.flags and Notification.FLAG_NO_CLEAR) != 0

        // 4. Compute content hash to detect duplicate spam
        val contentHash = NotificationExtractor.computeContentHash(
            pkgName,
            extracted.title,
            extracted.text,
            extracted.bigText
        )

        serviceScope.launch {
            try {
                val app = NotiVaultApplication.instance
                val repo = app.notificationRepository
                val rulesRepo = app.appRuleRepository
                val prefsRepo = app.preferencesRepository

                // Check duplicate: if same key and same content hash was received within last 3 seconds
                val existing = repo.getLatestByKey(sbn.key)
                if (existing != null && existing.contentHash == contentHash &&
                    (System.currentTimeMillis() - existing.timestamp < 3000)
                ) {
                    // Skip identical duplicate
                    return@launch
                }

                // Determine interception decision
                val masterInterceptOn = prefsRepo.isMasterInterceptEnabled.value
                val appRule = rulesRepo.getRuleForPackage(pkgName)
                val defaultAction = prefsRepo.defaultAction.value

                val isProtectedApp = protectedPackages.contains(pkgName) || (appRule?.isProtected == true)

                val shouldInterceptThisApp = if (isProtectedApp) {
                    false
                } else if (appRule != null) {
                    appRule.shouldIntercept
                } else {
                    defaultAction == "intercept"
                }

                val willIntercept = masterInterceptOn && shouldInterceptThisApp && !isOngoing

                // 5. Store in Room database
                val entity = NotificationEntity(
                    notificationKey = sbn.key,
                    packageName = pkgName,
                    appName = extracted.appName,
                    title = extracted.title,
                    text = extracted.text,
                    bigText = extracted.bigText,
                    subText = extracted.subText,
                    timestamp = sbn.postTime.takeIf { it > 0 } ?: System.currentTimeMillis(),
                    category = notification.category,
                    channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notification.channelId else null,
                    isIntercepted = willIntercept,
                    isOngoing = isOngoing,
                    isRead = false,
                    contentHash = contentHash
                )

                repo.insertNotification(entity)

                // 6. If intercept is ON and it is clearable, dismiss from notification shade
                if (willIntercept) {
                    try {
                        cancelNotification(sbn.key)
                        Log.d(TAG, "Intercepted and dismissed from shade: ${sbn.key}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to cancel notification: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "NotiVaultService"

        fun ensureRebound(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    requestRebind(ComponentName(context, NotificationInterceptorService::class.java))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to trigger requestRebind: ${e.message}")
                }
            }
        }
    }
}
