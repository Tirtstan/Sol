package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.entities.Category
import com.std.sol.repositories.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    fun getAllCategories(userId: String, descending: Boolean = false): Flow<List<Category>> {
        return categoryRepository.getAllCategories(userId, descending)
    }

    suspend fun getCategoryById(userId: String, id: String): Category? {
        return categoryRepository.getCategoryById(userId, id)
    }

    suspend fun getCategoryByName(userId: String, name: String): Category? {
        return categoryRepository.getCategoryByName(userId, name)
    }

    fun addCategory(userId: String, category: Category) {
        viewModelScope.launch {
            categoryRepository.addCategory(userId, category)
        }
    }

    fun ensureDefaultCategories(userId: String) {
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
                val existing = categoryRepository.getCategoryByName(userId, name)
                if (existing == null) {
                    categoryRepository.addCategory(
                        userId,
                        Category(
                            id = "",
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

    fun updateCategory(userId: String, category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(userId, category)
        }
    }

    fun deleteCategory(userId: String, category: Category) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(userId, category)
        }
    }
}
