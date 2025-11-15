package com.std.sol.entities

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var color: String = "",
    var icon: String = ""
) {
    constructor() : this("", "", "", "", "")
}
