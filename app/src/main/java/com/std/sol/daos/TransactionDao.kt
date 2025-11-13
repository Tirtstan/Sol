package com.std.sol.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactionsDesc(userId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date ASC")
    fun getAllTransactionsAsc(userId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByPeriodDesc(userId: Int, startDate: Date, endDate: Date): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getTransactionsByPeriodAsc(userId: Int, startDate: Date, endDate: Date): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getTransactionsByTypeDesc(userId: Int, type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY date ASC")
    fun getTransactionsByTypeAsc(userId: Int, type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC LIMIT 2")
    fun getRecentTransactions(userId: Int): Flow<List<Transaction>>

    fun getAllTransactions(
        userId: Int,
        descending: Boolean = false
    ): Flow<List<Transaction>> {
        return if (descending) {
            getAllTransactionsDesc(userId)
        } else {
            getAllTransactionsAsc(userId)
        }
    }

    fun getTransactionsByPeriod(
        userId: Int,
        startDate: Date,
        endDate: Date,
        descending: Boolean = true
    ): Flow<List<Transaction>> {
        return if (descending) {
            getTransactionsByPeriodDesc(userId, startDate, endDate)
        } else {
            getTransactionsByPeriodAsc(userId, startDate, endDate)
        }
    }

    fun getTransactionsByType(
        userId: Int,
        type: TransactionType,
        descending: Boolean = true
    ): Flow<List<Transaction>> {
        return if (descending) {
            getTransactionsByTypeDesc(userId, type)
        } else {
            getTransactionsByTypeAsc(userId, type)
        }
    }

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}