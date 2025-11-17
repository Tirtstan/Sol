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
            val budget = doc.toObject(Budget::class.java)
            // Ensure the document ID is set (Firestore should do this automatically with @DocumentId, but ensure it)
            budget?.id = doc.id
            budget
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
            // Query transactions by date range first (requires index on date field)
            // Then filter by categoryId and type in memory to avoid composite index requirement
            val transactions = db.collection("users").document(userId)
                .collection("transactions")
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            // Filter by categoryId and type (EXPENSE) in memory and sum amounts
            transactions.documents
                .mapNotNull { doc -> doc.toObject(com.std.sol.entities.Transaction::class.java) }
                .filter { transaction ->
                    transaction.categoryId == categoryId && 
                    transaction.type == "EXPENSE"
                }
                .sumOf { it.amount }
        } catch (e: Exception) {
            // If query fails (e.g., missing index), fall back to fetching all and filtering
            try {
                val allTransactions = db.collection("users").document(userId)
                    .collection("transactions")
                    .get()
                    .await()
                
                allTransactions.documents
                    .mapNotNull { doc -> doc.toObject(com.std.sol.entities.Transaction::class.java) }
                    .filter { transaction ->
                        transaction.categoryId == categoryId &&
                        transaction.type == "EXPENSE" &&
                        transaction.date >= start &&
                        transaction.date <= end
                    }
                    .sumOf { it.amount }
            } catch (fallbackException: Exception) {
                0.0
            }
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

