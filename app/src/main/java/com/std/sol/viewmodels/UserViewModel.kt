package com.std.sol.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.std.sol.SessionManager
import com.std.sol.entities.User
import com.std.sol.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = sessionManager.userIdFlow.firstOrNull()
            if (userId != null && auth.currentUser != null) {
                loadUserProfile(userId)
            } else {
                _isLoading.value = false
            }

            auth.addAuthStateListener { firebaseAuth ->
                viewModelScope.launch {
                    val firebaseUser = firebaseAuth.currentUser
                    if (firebaseUser != null) {
                        val uid = firebaseUser.uid
                        sessionManager.saveUserId(uid)
                        loadUserProfile(uid)
                    } else {
                        _currentUser.value = null
                        sessionManager.clearSession()
                    }
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun loadUserProfile(userId: String) {
        val user = userRepository.getUserById(userId)
        _currentUser.value = user
        _isLoading.value = false
    }

    suspend fun register(email: String, password: String, username: String): Result<User> {
        return try {
            _authError.value = null
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Registration failed"))
            
            userRepository.createUserProfile(firebaseUser.uid, username)
            
            val user = userRepository.getUserById(firebaseUser.uid)
            if (user != null) {
                _currentUser.value = user
                sessionManager.saveUserId(firebaseUser.uid)
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to create user profile"))
            }
        } catch (e: Exception) {
            _authError.value = e.message ?: "Registration failed"
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            _authError.value = null
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Login failed"))
            
            val user = userRepository.getUserById(firebaseUser.uid)
            if (user != null) {
                _currentUser.value = user
                sessionManager.saveUserId(firebaseUser.uid)
                Result.success(user)
            } else {
                val username = firebaseUser.email?.substringBefore("@") ?: "User"
                userRepository.createUserProfile(firebaseUser.uid, username)
                val newUser = userRepository.getUserById(firebaseUser.uid)
                if (newUser != null) {
                    _currentUser.value = newUser
                    sessionManager.saveUserId(firebaseUser.uid)
                    Result.success(newUser)
                } else {
                    Result.failure(Exception("Failed to load user profile"))
                }
            }
        } catch (e: Exception) {
            _authError.value = e.message ?: "Login failed"
            Result.failure(e)
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _currentUser.value = null
            sessionManager.clearSession()
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getUserById(id: String): User? {
        return userRepository.getUserById(id)
    }

    fun updateUser(user: User) = viewModelScope.launch {
        userRepository.updateUser(user)
        _currentUser.value = user
    }
}
