package com.daybook.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DaybookDao {

    // ----- Diary -----
    @Query("SELECT * FROM diary_entries WHERE date = :date")
    fun observeDiary(date: String): Flow<DiaryEntry?>

    @Query("SELECT * FROM diary_entries WHERE date BETWEEN :start AND :end")
    fun observeDiaryBetween(start: String, end: String): Flow<List<DiaryEntry>>

    @Upsert
    suspend fun upsertDiary(entry: DiaryEntry)

    @Query("DELETE FROM diary_entries WHERE date = :date")
    suspend fun deleteDiary(date: String)

    // ----- Money -----
    @Query("SELECT * FROM money_entries WHERE date = :date ORDER BY id DESC")
    fun observeMoney(date: String): Flow<List<MoneyEntry>>

    @Query("SELECT * FROM money_entries WHERE date BETWEEN :start AND :end ORDER BY date DESC, id DESC")
    fun observeMoneyBetween(start: String, end: String): Flow<List<MoneyEntry>>

    @Insert
    suspend fun insertMoney(entry: MoneyEntry)

    @Update
    suspend fun updateMoney(entry: MoneyEntry)

    @Delete
    suspend fun deleteMoney(entry: MoneyEntry)
}
