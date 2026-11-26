package com.daybook.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One income or expense line on a given day. */
@Entity(
    tableName = "money_entries",
    indices = [Index("date")]
)
data class MoneyEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,             // ISO format: yyyy-MM-dd
    val amountCents: Long,        // always positive
    val isExpense: Boolean = true,
    val category: String,
    val label: String = ""
)
