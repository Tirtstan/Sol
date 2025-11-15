package com.std.sol.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getAllTransactions(userId: String, descending: Boolean = true): Flow<List<Transaction>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("transactions")
        val query = if (descending) {
            collectionRef.orderBy("date", Query.Direction.DESCENDING)
        } else {
            collectionRef.orderBy("date", Query.Direction.ASCENDING)
        }

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val transactions = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)
            } ?: emptyList()
            
            trySend(transactions)
        }

        awaitClose { listenerRegistration.remove() }
    }

    fun getTransactionsByPeriod(
        userId: String,
        startDate: Timestamp,
        endDate: Timestamp,
        descending: Boolean = true
    ): Flow<List<Transaction>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("transactions")
        val query = collectionRef
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val transactions = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)
            } ?: emptyList()
            
            trySend(transactions)
        }

        awaitClose { listenerRegistration.remove() }
    }

    fun getTransactionsByType(
        userId: String,
        type: TransactionType,
        descending: Boolean = true
    ): Flow<List<Transaction>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("transactions")
        val query = collectionRef
            .whereEqualTo("type", type.name)
            .orderBy("date", if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val transactions = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)
            } ?: emptyList()
            
            trySend(transactions)
        }

        awaitClose { listenerRegistration.remove() }
    }

    fun getRecentTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("transactions")
        val query = collectionRef.orderBy("date", Query.Direction.DESCENDING).limit(2)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val transactions = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)
            } ?: emptyList()
            
            trySend(transactions)
        }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getTransactionById(userId: String, id: String): Transaction? {
        return try {
            val doc = db.collection("users").document(userId)
                .collection("transactions").document(id).get().await()
            doc.toObject(Transaction::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addTransaction(userId: String, transaction: Transaction): String {
        val docRef = db.collection("users").document(userId)
            .collection("transactions").document()
        transaction.userId = userId
        docRef.set(transaction).await()
        return docRef.id
    }

    suspend fun updateTransaction(userId: String, transaction: Transaction) {
        db.collection("users").document(userId)
            .collection("transactions").document(transaction.id)
            .set(transaction).await()
    }

    suspend fun deleteTransaction(userId: String, transaction: Transaction) {
        db.collection("users").document(userId)
            .collection("transactions").document(transaction.id)
            .delete().await()
    }
}

