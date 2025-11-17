package com.std.sol.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.std.sol.entities.Budget
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class BudgetRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getAllBudgets(userId: String, descending: Boolean = true): Flow<List<Budget>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("budgets")
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

            val budgets = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Budget::class.java)
            } ?: emptyList()
            
            trySend(budgets)
        }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getBudgetById(userId: String, id: String): Budget? {
        return try {
            val doc = db.collection("users").document(userId)
                .collection("budgets").document(id).get().await()
            doc.toObject(Budget::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCalculatedCurrentAmount(
        userId: String,
        categoryId: String,
        start: Timestamp,
        end: Timestamp
    ): Double {
        return try {
            val transactions = db.collection("users").document(userId)
                .collection("transactions")
                .whereEqualTo("categoryId", categoryId)
                .whereEqualTo("type", "EXPENSE")
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .get()
                .await()

            transactions.documents.sumOf { doc ->
                val transaction = doc.toObject(com.std.sol.entities.Transaction::class.java)
                transaction?.amount ?: 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun addBudget(userId: String, budget: Budget): String {
        val docRef = db.collection("users").document(userId)
            .collection("budgets").document()
        budget.userId = userId
        docRef.set(budget).await()
        return docRef.id
    }

    suspend fun updateBudget(userId: String, budget: Budget) {
        db.collection("users").document(userId)
            .collection("budgets").document(budget.id)
            .set(budget).await()
    }

    suspend fun deleteBudget(userId: String, budget: Budget) {
        db.collection("users").document(userId)
            .collection("budgets").document(budget.id)
            .delete().await()
    }
}

