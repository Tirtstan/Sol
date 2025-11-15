package com.std.sol


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.stringPreferencesKey
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
    private fun dashboardWidgetsKey(userId: Int) =
        stringPreferencesKey("dashboard_widgets_user_${userId}")

    //Flow to READ user's saved widget list
    //automatically update when settings are changed
    fun getDashboardWidgets(userId: Int): Flow<List<DashboardWidgetType>> {
        return appContext.dataStore.data.map { preferences ->
            //get saved string
            //if nothing is saved, use "RECENT_BUDGETS" as the default
            val savedString = preferences[dashboardWidgetsKey(userId)]
                ?: DashboardWidgetType.RECENT_BUDGETS.name

            //convert the "RECENT_BUDGETS" string back to a list
            savedString.split(",")
                .mapNotNull { widgetName ->
                    try {
                        //find the enum value for each name
                        DashboardWidgetType.valueOf(widgetName)
                    } catch (e: Exception) {
                        //in case of a saved value that no longer exists
                        null
                    }
                }
        }
    }

    suspend fun saveDashboardWidget(userId: Int, widgets: List<DashboardWidgetType>) {
        appContext.dataStore.edit { preferences ->

            val widgetString = widgets.joinToString(",") { it.name }
            preferences[dashboardWidgetsKey(userId)] = widgetString
        }
    }
}
