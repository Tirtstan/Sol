package com.std.sol

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
    }

    val userIdFlow: Flow<Int?> = appContext.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    suspend fun saveUserId(userId: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun clearSession() {
        appContext.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
}
