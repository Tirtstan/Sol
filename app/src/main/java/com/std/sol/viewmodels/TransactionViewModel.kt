package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import com.std.sol.repositories.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

    fun getAllTransactions(userId: String, descending: Boolean = true): Flow<List<Transaction>> {
        return transactionRepository.getAllTransactions(userId, descending)
    }

    fun getTransactionsByPeriod(
        userId: String,
        startDate: Timestamp,
        endDate: Timestamp,
        descending: Boolean = true
    ): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByPeriod(userId, startDate, endDate, descending)
    }

    fun getTransactionsByType(
        userId: String,
        type: TransactionType,
        descending: Boolean = true
    ): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByType(userId, type, descending)
    }

    fun getRecentTransactions(userId: String): Flow<List<Transaction>> {
        return transactionRepository.getRecentTransactions(userId)
    }

    suspend fun getTransactionById(userId: String, id: String): Transaction? {
        return transactionRepository.getTransactionById(userId, id)
    }

    fun addTransaction(userId: String, transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.addTransaction(userId, transaction)
        }
    }

    fun updateTransaction(userId: String, transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(userId, transaction)
        }
    }

    fun deleteTransaction(userId: String, transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(userId, transaction)
        }
    }
}
