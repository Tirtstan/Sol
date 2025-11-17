package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.SessionManager
import com.std.sol.components.DashboardWidgetType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    sessionManager: SessionManager
) : ViewModel() {
    //get Flow<String?> of logged-in user's ID
    private val userIdFlow = sessionManager.userIdFlow

    val dashboardWidgets: StateFlow<List<DashboardWidgetType>> =
        userIdFlow.flatMapLatest { userId ->
            if (userId != null) {
                sessionManager.getDashboardWidgets(userId)
            } else {
                //no user logged in, return an empty list
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() //start with empty list while loading
        )
}