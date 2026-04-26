package com.nearwake.feature.departure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nearwake.core.datastore.UserPreferencesDataStore
import com.nearwake.data.alerts.DepartureReminderScheduler
import com.nearwake.data.patterns.CommutePredictionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DepartureReminderBootReceiver : BroadcastReceiver() {
    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore
    @Inject lateinit var commutePredictionRepository: CommutePredictionRepository
    @Inject lateinit var departureReminderScheduler: DepartureReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val preferences = userPreferencesDataStore.preferences.first()
                if (preferences.departureRemindersEnabled) {
                    runCatching { commutePredictionRepository.refreshPredictions() }
                    departureReminderScheduler.scheduleToday(commutePredictionRepository.getPredictions())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
