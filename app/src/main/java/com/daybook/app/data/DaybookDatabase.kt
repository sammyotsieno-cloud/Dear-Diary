package com.daybook.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DiaryEntry::class, MoneyEntry::class],
    version = 1,
    exportSchema = false
)
abstract class DaybookDatabase : RoomDatabase() {

    abstract fun dao(): DaybookDao

    companion object {
        @Volatile
        private var instance: DaybookDatabase? = null

        fun get(context: Context): DaybookDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DaybookDatabase::class.java,
                    "daybook.db"
                ).build().also { instance = it }
            }
    }
}
