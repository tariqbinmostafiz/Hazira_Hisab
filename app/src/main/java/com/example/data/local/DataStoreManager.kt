package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "worker_ledger_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_DEFAULT_RATE_POISHA = longPreferencesKey("default_rate_poisha")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_USE_BENGALI_DIGITS = booleanPreferencesKey("use_bengali_digits")

        // Default ৳1,000 = 100,000 poisha
        const val DEFAULT_RATE_POISHA = 100000L
    }

    val defaultRatePoisha: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_RATE_POISHA] ?: DEFAULT_RATE_POISHA
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val useBengaliDigits: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_USE_BENGALI_DIGITS] ?: true
    }

    suspend fun setDefaultRatePoisha(ratePoisha: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_RATE_POISHA] = ratePoisha
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setUseBengaliDigits(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USE_BENGALI_DIGITS] = enabled
        }
    }
}
