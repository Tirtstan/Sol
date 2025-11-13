package com.std.sol.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.std.sol.entities.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUserProfile(userId: String, username: String) {
        val user = User(
            id = userId,
            username = username,
            createdAt = System.currentTimeMillis()
        )
        db.collection("users").document(userId).set(user).await()
    }

    suspend fun updateUser(user: User) {
        db.collection("users").document(user.id).set(user).await()
    }
}

