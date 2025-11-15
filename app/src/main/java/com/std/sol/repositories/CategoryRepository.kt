package com.std.sol.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.std.sol.entities.Category
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getAllCategories(userId: String, descending: Boolean = false): Flow<List<Category>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("categories")
        val query = if (descending) {
            collectionRef.orderBy("name", Query.Direction.DESCENDING)
        } else {
            collectionRef.orderBy("name", Query.Direction.ASCENDING)
        }

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val categories = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Category::class.java)
            } ?: emptyList()
            
            trySend(categories)
        }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getCategoryById(userId: String, id: String): Category? {
        return try {
            val doc = db.collection("users").document(userId)
                .collection("categories").document(id).get().await()
            doc.toObject(Category::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCategoryByName(userId: String, name: String): Category? {
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("categories")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(Category::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addCategory(userId: String, category: Category): String {
        val docRef = db.collection("users").document(userId)
            .collection("categories").document()
        category.userId = userId
        docRef.set(category).await()
        return docRef.id
    }

    suspend fun updateCategory(userId: String, category: Category) {
        db.collection("users").document(userId)
            .collection("categories").document(category.id)
            .set(category).await()
    }

    suspend fun deleteCategory(userId: String, category: Category) {
        db.collection("users").document(userId)
            .collection("categories").document(category.id)
            .delete().await()
    }
}

