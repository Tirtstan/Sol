package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.daos.BudgetDao
import com.std.sol.entities.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BudgetViewModel(private val budgetDao: BudgetDao) : ViewModel() {

    fun getAllBudgets(userId: Int, descending: Boolean = true): Flow<List<Budget>> {
        return budgetDao.getAllBudgets(userId, descending)
    }

    suspend fun getBudgetById(id: Int): Budget? {
        return budgetDao.getBudgetById(id)
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
