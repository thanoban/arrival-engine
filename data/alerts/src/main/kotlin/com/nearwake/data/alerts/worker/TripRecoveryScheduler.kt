package com.nearwake.data.alerts.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object TripRecoveryScheduler {
    private const val RECOVERY_WORK_NAME = "trip_recovery_check"
    private const val TAG_RECOVERY = "trip_recovery"

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<TripRecoveryWorker>()
            .addTag(TAG_RECOVERY)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            RECOVERY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
