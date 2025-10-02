package com.std.sol.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.std.sol.entities.Budget
import kotlinx.coroutines.flow.Flow

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
}
