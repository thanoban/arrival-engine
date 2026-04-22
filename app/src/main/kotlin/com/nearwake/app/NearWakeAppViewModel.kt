package com.nearwake.app

import androidx.lifecycle.ViewModel
import com.nearwake.core.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NearWakeAppUiState(
    val startDestination: String? = null,
)

@HiltViewModel
class NearWakeAppViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(NearWakeAppUiState())
    val state: StateFlow<NearWakeAppUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
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

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}
