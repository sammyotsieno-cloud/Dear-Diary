package com.daybook.app.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

class DaybookRepository(private val dao: DaybookDao) {

    // ----- Day view -----
    fun diary(date: LocalDate): Flow<DiaryEntry?> = dao.observeDiary(date.toString())

    fun money(date: LocalDate): Flow<List<MoneyEntry>> = dao.observeMoney(date.toString())

    suspend fun saveDiary(date: LocalDate, note: String, mood: String) {
        if (note.isBlank() && mood.isBlank()) {
            dao.deleteDiary(date.toString())
        } else {
            dao.upsertDiary(DiaryEntry(date.toString(), note, mood))
        }
    }

    suspend fun addMoney(
        date: LocalDate,
        amountCents: Long,
        isExpense: Boolean,
        category: String,
        label: String
    ) {
        dao.insertMoney(
            MoneyEntry(
                date = date.toString(),
                amountCents = amountCents,
                isExpense = isExpense,
                category = category,
                label = label
            )
        )
    }

    suspend fun updateMoney(entry: MoneyEntry) = dao.updateMoney(entry)

    suspend fun deleteMoney(entry: MoneyEntry) = dao.deleteMoney(entry)

    // ----- Month view -----
    fun diaryInMonth(month: YearMonth): Flow<List<DiaryEntry>> =
        dao.observeDiaryBetween(month.atDay(1).toString(), month.atEndOfMonth().toString())

    fun moneyInMonth(month: YearMonth): Flow<List<MoneyEntry>> =
        dao.observeMoneyBetween(month.atDay(1).toString(), month.atEndOfMonth().toString())
}
