// In C:/--Files and shit--/Coding Stuff/Android/Sol/app/src/main/java/com/std/sol/viewmodels/BudgetViewModel.kt

package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.daos.BudgetDao
import com.std.sol.entities.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class BudgetViewModel(private val budgetDao: BudgetDao) : ViewModel() {
    private val _currentAmount = MutableStateFlow(0.0)
    val currentAmount: StateFlow<Double> = _currentAmount.asStateFlow()

    fun getAllBudgets(userId: Int, descending: Boolean = true): Flow<List<Budget>> {
        return budgetDao.getAllBudgets(userId, descending)
    }

    suspend fun getBudgetById(id: Int): Budget? {
        return budgetDao.getBudgetById(id)
    }

    fun updateCurrentAmount(userId: Int, categoryId: Int, start: Date, end: Date) {
        viewModelScope.launch {
            budgetDao.getCalculatedCurrentAmount(userId, categoryId, start, end).collect { amount ->
                _currentAmount.value = amount
            }
        }
    }


    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.insertBudget(budget)
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }
}
