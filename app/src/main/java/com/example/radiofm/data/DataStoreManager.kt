package com.example.radiofm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "radio_settings")

class DataStoreManager(private val context: Context) {

    private val FAVORITES_KEY = stringSetPreferencesKey("favorites")
    private val HISTORY_KEY = stringPreferencesKey("history")

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FAVORITES_KEY] ?: emptySet()
        }

    val historyFlow: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            preferences[HISTORY_KEY]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        }

    suspend fun toggleFavorite(stationId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITES_KEY] ?: emptySet()
            val newSet = if (current.contains(stationId)) {
                current.toMutableSet().apply { remove(stationId) }
            } else {
                current.toMutableSet().apply { add(stationId) }
            }
            preferences[FAVORITES_KEY] = newSet
        }
    }

    suspend fun addToHistory(stationId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[HISTORY_KEY]?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
            current.remove(stationId)
            current.add(0, stationId)
            val limited = current.take(20)
            preferences[HISTORY_KEY] = limited.joinToString(",")
        }
    }
}
