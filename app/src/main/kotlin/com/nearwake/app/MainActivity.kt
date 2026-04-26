package com.nearwake.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nearwake.core.datastore.UserPreferencesDataStore
import com.nearwake.core.datastore.model.ThemeMode
import com.nearwake.core.datastore.model.UserPreferences
import com.nearwake.core.designsystem.NearWakeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences by userPreferencesDataStore.preferences.collectAsState(initial = UserPreferences())
            val systemDarkTheme = isSystemInDarkTheme()
            NearWakeTheme(darkTheme = preferences.themeMode.resolveDarkTheme(systemDarkTheme)) {
                NearWakeAppContent()
            }
        }
    }
}

private fun ThemeMode.resolveDarkTheme(systemDarkTheme: Boolean): Boolean =
    when (this) {
        ThemeMode.SYSTEM -> systemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
