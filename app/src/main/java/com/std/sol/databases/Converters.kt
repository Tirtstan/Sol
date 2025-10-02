package com.std.sol.databases

import androidx.room.TypeConverter
import com.std.sol.entities.TransactionType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toTransactionType(value: Int): TransactionType {
        return TransactionType.entries[value]
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): Int {
        return value.ordinal
    }
}
