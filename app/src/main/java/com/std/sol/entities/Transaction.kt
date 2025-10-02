package com.std.sol.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["categoryId"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "userId")
    @SerializedName("userId")
    val userId: Int,
    @ColumnInfo(name = "categoryId")
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("date")
    val date: Date,
    @SerializedName("note")
    val note: String?,
    @SerializedName("type")
    val type: TransactionType
)
