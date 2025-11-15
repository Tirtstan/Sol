package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.entities.Budget
import com.std.sol.repositories.BudgetRepository
import kotlinx.coroutines.flow.Flow
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class BudgetViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {
    private val _currentAmount = MutableStateFlow(0.0)
    val currentAmount: StateFlow<Double> = _currentAmount.asStateFlow()

    fun getAllBudgets(userId: String, descending: Boolean = true): Flow<List<Budget>> {
        return budgetRepository.getAllBudgets(userId, descending)
    }

    suspend fun getBudgetById(userId: String, id: String): Budget? {
        return budgetRepository.getBudgetById(userId, id)
    }

    fun updateCurrentAmount(userId: String, categoryId: String, start: Timestamp, end: Timestamp) {
        viewModelScope.launch {
            val amount = budgetRepository.getCalculatedCurrentAmount(userId, categoryId, start, end)
            _currentAmount.value = amount
        }
    }

    fun addBudget(userId: String, budget: Budget) {
        viewModelScope.launch {
            budgetRepository.addBudget(userId, budget)
        }
    }

    fun updateBudget(userId: String, budget: Budget) {
        viewModelScope.launch {
            budgetRepository.updateBudget(userId, budget)
        }
    }

    fun deleteBudget(userId: String, budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(userId, budget)
        }
    }
}
