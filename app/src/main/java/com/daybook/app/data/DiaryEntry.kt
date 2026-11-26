package com.daybook.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One diary page per day. */
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey val date: String, // ISO format: yyyy-MM-dd
    val note: String = "",
    val mood: String = ""         // an emoji, or empty
)
