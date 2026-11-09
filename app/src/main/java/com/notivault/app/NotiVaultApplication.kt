package com.notivault.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.notivault.app.data.local.NotiVaultDatabase
import com.notivault.app.data.repository.AppRuleRepository
import com.notivault.app.data.repository.NotificationRepository
import com.notivault.app.data.repository.PreferencesRepository
import com.notivault.app.worker.CleanupWorker
import java.util.concurrent.TimeUnit

class NotiVaultApplication : Application() {

    lateinit var database: NotiVaultDatabase
        private set

    lateinit var notificationRepository: NotificationRepository
        private set

    lateinit var appRuleRepository: AppRuleRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Room Database
        database = NotiVaultDatabase.getDatabase(this)
        notificationRepository = NotificationRepository(database.notificationDao())
        appRuleRepository = AppRuleRepository(database.appRuleDao())
        preferencesRepository = PreferencesRepository(this)

        // Schedule Daily Database Auto-Cleanup Worker
        scheduleAutoCleanupWorker()
    }

    private fun scheduleAutoCleanupWorker() {
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NotificationAutoCleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }

    companion object {
        lateinit var instance: NotiVaultApplication
            private set
    }
}
