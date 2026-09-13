package com.nearwake.data.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.LocaleList
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.nearwake.core.common.region.RegionSignalDataSource
import com.nearwake.core.common.region.RegionSignals
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class AndroidRegionSignalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : RegionSignalDataSource {
    override fun observeRegionSignals(): Flow<RegionSignals> =
        callbackFlow {
            fun sendCurrentSignals() {
                trySend(readCurrentSignals())
            }

            sendCurrentSignals()
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    sendCurrentSignals()
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_LOCALE_CHANGED)
                addAction(ACTION_SIM_STATE_CHANGED)
            }
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            awaitClose {
                runCatching { context.unregisterReceiver(receiver) }
            }
        }.distinctUntilChanged()

    private fun readCurrentSignals(): RegionSignals =
        RegionSignals(
            simCountryCode = telephonyManager()?.simCountryIso,
            localeCountryCode = currentLocaleCountryCode(),
        )

    private fun telephonyManager(): TelephonyManager? =
        context.getSystemService(TelephonyManager::class.java)

    private fun currentLocaleCountryCode(): String? {
        val locale = LocaleList.getDefault().takeIf { it.size() > 0 }?.get(0) ?: Locale.getDefault()
        return locale.country
    }

    private companion object {
        const val ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"
    }
}
