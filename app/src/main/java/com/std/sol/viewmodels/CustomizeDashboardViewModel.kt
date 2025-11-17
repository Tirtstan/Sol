package com.std.sol.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.SessionManager
import com.std.sol.components.DashboardWidgetType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CustomizeDashboardViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    //list of all possible widgets
    val allWidgets: List<DashboardWidgetType> = DashboardWidgetType.entries.toList()

    //list that UI can directly modify (add/remove)
    //compose automatically reacts to the changes
    val enabledWidgets = mutableStateListOf<DashboardWidgetType>()

    //loads user's currently saved settings into the list
    fun loadSettings(userId: String) {
        viewModelScope.launch {
            //.first() gets the most recent value from the flow
            val savedList = sessionManager.getDashboardWidgets(userId).first()
            enabledWidgets.clear()
            // If no widgets are saved, enable all widgets by default
            if (savedList.isEmpty()) {
                enabledWidgets.addAll(allWidgets)
                sessionManager.saveDashboardWidget(userId, allWidgets)
            } else {
                enabledWidgets.addAll(savedList)
            }
        }
    }

    //saves the current state of the list back to DataStore
    fun saveSettings(userId: String) {
        viewModelScope.launch {
            // Prevent saving empty list - if empty, don't save (keep previous state)
            // This prevents the issue where toggling the last widget resets everything
            if (enabledWidgets.isNotEmpty()) {
                sessionManager.saveDashboardWidget(userId, enabledWidgets.toList())
            }
        }
    }
}