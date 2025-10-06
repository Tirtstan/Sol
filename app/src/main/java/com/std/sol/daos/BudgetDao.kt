package com.std.sol.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.std.sol.entities.Budget
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Int): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY name ASC")
    fun getAllBudgetsAsc(userId: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY name DESC")
    fun getAllBudgetsDesc(userId: Int): Flow<List<Budget>>

    fun getAllBudgets(userId: Int, descending: Boolean = false): Flow<List<Budget>> {
        return if (descending) {
            getAllBudgetsDesc(userId)
        } else {
            getAllBudgetsAsc(userId)
        }
    }

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    /**
     * Calculate the current amount for a budget by summing transactions for the same user and category
     * between the provided start and end dates. Returns 0.0 when no matching transactions exist.
     *
     * Assumes transactions are stored in a table named `transactions` with columns:
     * - amount (numeric)
     * - userId (int)
     * - categoryId (int)
     * - date (stored as Date or compatible type via TypeConverter)
     * - type (the transaction type, e.g., '0' for income, '1' for expense)
     */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE userId = :userId
          AND categoryId = :categoryId
          AND date BETWEEN :start AND :end
          AND type = 1
    """
    )
    fun getCalculatedCurrentAmount(
        userId: Int,
        categoryId: Int,
        start: Date,
        end: Date
    ): Flow<Double>
}
