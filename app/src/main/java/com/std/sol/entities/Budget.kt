package com.std.sol.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Budget(
    @DocumentId
    var id: String = "",
    var userId: String = "",
    var categoryId: String = "",
    var name: String = "",
    var description: String? = null,
    var minGoalAmount: Double = 0.0,
    var maxGoalAmount: Double = 0.0,
    var startDate: Timestamp = Timestamp.now(),
    var endDate: Timestamp = Timestamp.now()
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", "", "", null, 0.0, 0.0, Timestamp.now(), Timestamp.now())
    
    // Helper methods for working with Date
    fun getStartDateAsDate(): Date = startDate.toDate()
    fun getEndDateAsDate(): Date = endDate.toDate()
}
