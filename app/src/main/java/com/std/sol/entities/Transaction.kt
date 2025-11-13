package com.std.sol.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Transaction(
    @DocumentId
    var id: String = "",
    var userId: String = "",
    var categoryId: String = "",
    var name: String = "",
    var amount: Double = 0.0,
    var date: Timestamp = Timestamp.now(),
    var note: String? = null,
    var type: String = TransactionType.EXPENSE.name,
    var imagePath: String? = null
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", "", "", 0.0, Timestamp.now(), null, TransactionType.EXPENSE.name, null)
    
    // Helper methods for working with Date and TransactionType
    fun getDateAsDate(): Date = date.toDate()
    fun getTransactionType(): TransactionType = TransactionType.valueOf(type)
}