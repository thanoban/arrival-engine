package com.nearwake.data.alerts.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class TripRecoveryBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            TripRecoveryScheduler.enqueue(context)
            Timber.i("Boot completed. Enqueued trip recovery work.")
        }
    }
}
