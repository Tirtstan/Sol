package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.std.sol.SessionManager
import com.std.sol.databases.AppDatabase
import com.std.sol.components.DashboardWidgetType

class ViewModelFactory(
    private val db: AppDatabase,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(db.userDao(), sessionManager) as T
        }
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(db.transactionDao()) as T
        }
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(db.categoryDao()) as T
        }
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(db.budgetDao()) as T
        }

        //teaches factory how to create the new ViewModel
        modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
            DashboardViewModel(db, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
