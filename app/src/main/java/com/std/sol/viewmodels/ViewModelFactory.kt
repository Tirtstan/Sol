package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.std.sol.SessionManager
import com.std.sol.repositories.BudgetRepository
import com.std.sol.repositories.CategoryRepository
import com.std.sol.repositories.TransactionRepository
import com.std.sol.repositories.UserRepository

class ViewModelFactory(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository, sessionManager) as T
        }
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(transactionRepository) as T
        }
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(categoryRepository) as T
        }
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(budgetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
