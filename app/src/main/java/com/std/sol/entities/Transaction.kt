package com.std.sol.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
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
    constructor() : this("", "", "", "", 0.0, Timestamp.now(), null, TransactionType.EXPENSE.name, null)

    @Exclude
    fun getDateAsDate(): Date = date.toDate()
    @Exclude
    fun getTransactionType(): TransactionType = TransactionType.valueOf(type)
}