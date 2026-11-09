package com.notivault.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notivault.app.NotiVaultApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val app = NotiVaultApplication.instance
            val cleanupDays = app.preferencesRepository.autoCleanupDays.value

            if (cleanupDays > 0) {
                val cutoffTimestamp = System.currentTimeMillis() - (cleanupDays.toLong() * 24L * 60L * 60L * 1000L)
                val deletedCount = app.notificationRepository.deleteOlderThan(cutoffTimestamp)
                android.util.Log.i("CleanupWorker", "Auto-cleanup deleted $deletedCount notifications older than $cleanupDays days.")
            }
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("CleanupWorker", "Cleanup failed: ${e.message}")
            Result.retry()
        }
    }
}
