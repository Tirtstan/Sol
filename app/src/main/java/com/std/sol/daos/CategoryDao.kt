package com.std.sol.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.std.sol.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): Category?

    @Query("SELECT * FROM categories WHERE name = :name AND userId = :userId LIMIT 1")
    suspend fun getCategoryByNameForUser(name: String, userId: Int): Category?

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getAllCategoriesAsc(userId: Int): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name DESC")
    fun getAllCategoriesDesc(userId: Int): Flow<List<Category>>

    fun getAllCategories(userId: Int, descending: Boolean = false): Flow<List<Category>> {
        return if (descending) {
            getAllCategoriesDesc(userId)
        } else {
            getAllCategoriesAsc(userId)
        }
    }

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
