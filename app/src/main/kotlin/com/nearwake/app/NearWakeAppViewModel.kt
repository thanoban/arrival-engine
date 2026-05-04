package com.nearwake.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.core.datastore.UserPreferencesDataStore
import com.nearwake.data.alerts.worker.TripRecoveryScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NearWakeAppUiState(
    val startDestination: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class NearWakeAppViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val mutableState = MutableStateFlow(NearWakeAppUiState())
    val state: StateFlow<NearWakeAppUiState> = mutableState.asStateFlow()

    init {
        TripRecoveryScheduler.enqueue(appContext)
        viewModelScope.launch {
            userPreferencesDataStore.preferences.collect { preferences ->
                mutableState.value = NearWakeAppUiState(
                    startDestination = if (preferences.onboardingCompleted) {
                        NearWakeRoute.Home.route
                    } else {
                        NearWakeRoute.Onboarding.route
                    },
                )
            }
        }
    }
}
