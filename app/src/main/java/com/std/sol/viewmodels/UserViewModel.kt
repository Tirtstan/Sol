package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.std.sol.SessionManager
import com.std.sol.daos.UserDao
import com.std.sol.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class UserViewModel(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = sessionManager.userIdFlow.firstOrNull()
            if (userId != null) {
                val user = getUserById(userId)
                _currentUser.value = user
            }
            _isLoading.value = false
        }
    }


    fun setCurrentUser(user: User) {
        viewModelScope.launch {
            _currentUser.value = user
            sessionManager.saveUserId(user.id)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _currentUser.value = null
            sessionManager.clearSession()
        }
    }

    fun addUser(user: User): Long {
        var newUserId: Long = -1
        viewModelScope.launch(Dispatchers.IO) {
            newUserId = userDao.insertUser(user)
        }

        return newUserId
    }

    suspend fun addUserAndWait(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    fun updateUser(user: User) = viewModelScope.launch {
        userDao.updateUser(user)
    }

    fun deleteUser(user: User) = viewModelScope.launch {
        userDao.deleteUser(user)
    }
}
