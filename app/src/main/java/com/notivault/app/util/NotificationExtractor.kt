package com.notivault.app.util

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.notification.StatusBarNotification
import java.security.MessageDigest

data class ExtractedNotification(
    val appName: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val subText: String?
)

object NotificationExtractor {

    fun extract(context: Context, sbn: StatusBarNotification): ExtractedNotification {
        val pm = context.packageManager
        val pkgName = sbn.packageName

        val appName = try {
            val appInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            pkgName
        }

        val extras: Bundle? = sbn.notification?.extras

        var title: String? = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        var text: String? = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText: String? = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val subText: String? = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        // Fallback for messaging style conversation notifications
        if (text == null && extras?.containsKey(Notification.EXTRA_MESSAGES) == true) {
            text = "[Message Conversation]"
        }

        return ExtractedNotification(
            appName = appName,
            title = title?.trim()?.takeIf { it.isNotEmpty() },
            text = text?.trim()?.takeIf { it.isNotEmpty() },
            bigText = bigText?.trim()?.takeIf { it.isNotEmpty() },
            subText = subText?.trim()?.takeIf { it.isNotEmpty() }
        )
    }

    fun computeContentHash(packageName: String, title: String?, text: String?, bigText: String?): String {
        val raw = "$packageName|${title ?: ""}|${text ?: ""}|${bigText ?: ""}"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
