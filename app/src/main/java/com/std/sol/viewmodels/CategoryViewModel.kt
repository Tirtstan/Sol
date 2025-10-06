package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.daos.CategoryDao
import com.std.sol.entities.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryDao: CategoryDao) : ViewModel() {

    fun getAllCategories(userId: Int, descending: Boolean = false): Flow<List<Category>> {
        return categoryDao.getAllCategories(userId, descending)
    }

    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)
    }

    fun addCategory(category: Category): Long {
        var id: Long = -1
        viewModelScope.launch {
            id = categoryDao.insertCategory(category)
        }

        return id
    }

    fun ensureDefaultCategories(userId: Int) {
        viewModelScope.launch {
            val defaultCategories = listOf(
                Triple("Food", "#FF6B6B", "ic_food"),
                Triple("Transport", "#4DA1FF", "ic_transport"),
                Triple("Shopping", "#C77DFF", "ic_shopping"),
                Triple("Bills", "#FFB86B", "ic_bills"),
                Triple("Salary", "#6BE051", "ic_salary"),
                Triple("Entertainment", "#FF6FD8", "ic_entertainment"),
                Triple("Groceries", "#F4C047", "ic_groceries"),
                Triple("Health", "#57C5A2", "ic_health"),
                Triple("Education", "#7AA2FF", "ic_education"),
                Triple("Other", "#9E9E9E", "ic_other")
            )

            for ((name, color, icon) in defaultCategories) {
                val existing = categoryDao.getCategoryByNameForUser(name, userId)
                if (existing == null) {
                    categoryDao.insertCategory(
                        Category(
                            id = 0,
                            userId = userId,
                            name = name,
                            color = color,
                            icon = icon
                        )
                    )
                }
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }

}
