package com.std.sol


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.std.sol.components.DashboardWidgetType

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val userIdFlow: Flow<String?> = appContext.dataStore.data
        .map { preferences ->
            val rawValue = preferences.asMap()[USER_ID_KEY]
            when (rawValue) {
                is String -> rawValue
                is Int -> rawValue.toString()
                is Long -> rawValue.toString()
                else -> null
            }
        }

    suspend fun saveUserId(userId: String) {
        appContext.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun clearSession() {
        appContext.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    //private helper to create a unique key for each user's dashboard
    private fun dashboardWidgetsKey(userId: String) =
        stringPreferencesKey("dashboard_widgets_user_${userId}")

    //Flow to READ user's saved widget list
    //automatically update when settings are changed
    fun getDashboardWidgets(userId: String): Flow<List<DashboardWidgetType>> {
        return appContext.dataStore.data.map { preferences ->
            val savedString = preferences[dashboardWidgetsKey(userId)]
            if (savedString.isNullOrBlank()) {
                DashboardWidgetType.entries.toList()
            } else {
                savedString.split(",")
                    .mapNotNull { widgetName ->
                        try {
                            DashboardWidgetType.valueOf(widgetName.trim())
                        } catch (e: Exception) {
                            null
                        }
                    }
            }
        }
    }

    suspend fun saveDashboardWidget(userId: String, widgets: List<DashboardWidgetType>) {
        appContext.dataStore.edit { preferences ->

            val widgetString = widgets.joinToString(",") { it.name }
            preferences[dashboardWidgetsKey(userId)] = widgetString
        }
    }
}
