package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.daos.UserDao
import com.std.sol.entities.User
import kotlinx.coroutines.launch

open class UserViewModel(private val userDao: UserDao) : ViewModel() {

    fun addUser(user: User) {
        viewModelScope.launch {
            userDao.insertUser(user)
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            userDao.updateUser(user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.deleteUser(user)
        }
    }
}
