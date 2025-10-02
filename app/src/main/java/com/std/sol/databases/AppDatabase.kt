package com.std.sol.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.std.sol.daos.BudgetDao
import com.std.sol.daos.CategoryDao
import com.std.sol.daos.TransactionDao
import com.std.sol.daos.UserDao
import com.std.sol.entities.Budget
import com.std.sol.entities.Category
import com.std.sol.entities.Transaction
import com.std.sol.entities.User

@Database(
    entities = [User::class, Transaction::class, Category::class, Budget::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
}
