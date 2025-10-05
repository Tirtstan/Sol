package com.std.sol.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    tableName = "budgets",
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
data class Budget(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "userId")
    @SerializedName("userId")
    val userId: Int,
    @ColumnInfo(name = "categoryId")
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("currentAmount")
    val currentAmount: Double,
    @SerializedName("minGoalAmount")
    val minGoalAmount: Double,
    @SerializedName("maxGoalAmount")
    val maxGoalAmount: Double,
    @SerializedName("startDate")
    val startDate: Date,
    @SerializedName("endDate")
    val endDate: Date
)
