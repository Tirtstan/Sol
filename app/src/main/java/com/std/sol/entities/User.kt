package com.std.sol.entities

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    var id: String = "",
    var username: String = "",
    var createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", System.currentTimeMillis())
}
