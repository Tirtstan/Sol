package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.daos.TransactionDao
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    fun getAllTransactions(userId: Int, descending: Boolean = true): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions(userId, descending)
    }

    fun getTransactionsByPeriod(
        userId: Int,
        startDate: Date,
        endDate: Date,
        descending: Boolean = true
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByPeriod(userId, startDate, endDate, descending)
    }

    fun getTransactionsByType(
        userId: Int,
        type: TransactionType,
        descending: Boolean = true
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(userId, type, descending)
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
        }
    }
}