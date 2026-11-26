package com.daybook.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daybook.app.data.DaybookDatabase
import com.daybook.app.data.DaybookRepository
import com.daybook.app.data.DiaryEntry
import com.daybook.app.data.MoneyEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class DaybookViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DaybookRepository(DaybookDatabase.get(app).dao())

    // ----- Selected day -----
    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    fun setDate(date: LocalDate) { _date.value = date }

    val money: StateFlow<List<MoneyEntry>> = _date
        .flatMapLatest { repo.money(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** One-shot read, used to fill the notebook page when a day opens. */
    suspend fun loadDiary(date: LocalDate): DiaryEntry? = repo.diary(date).first()

    fun saveDiary(date: LocalDate, note: String, mood: String) {
        viewModelScope.launch { repo.saveDiary(date, note, mood) }
    }

    fun addMoney(
        date: LocalDate,
        amountCents: Long,
        isExpense: Boolean,
        category: String,
        label: String
    ) {
        viewModelScope.launch { repo.addMoney(date, amountCents, isExpense, category, label) }
    }

    fun updateMoney(entry: MoneyEntry) {
        viewModelScope.launch { repo.updateMoney(entry) }
    }

    fun deleteMoney(entry: MoneyEntry) {
        viewModelScope.launch { repo.deleteMoney(entry) }
    }

    // ----- Selected month -----
    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    fun setMonth(month: YearMonth) { _month.value = month }
    fun nextMonth() { _month.value = _month.value.plusMonths(1) }
    fun previousMonth() { _month.value = _month.value.minusMonths(1) }

    val monthSummary: StateFlow<MonthSummary> = _month
        .flatMapLatest { month ->
            combine(repo.diaryInMonth(month), repo.moneyInMonth(month)) { diary, money ->
                val expenses = money.filter { it.isExpense }
                MonthSummary(
                    incomeCents = money.filter { !it.isExpense }.sumOf { it.amountCents },
                    expenseCents = expenses.sumOf { it.amountCents },
                    expenseByCategory = expenses
                        .groupBy { it.category }
                        .map { (category, list) -> category to list.sumOf { it.amountCents } }
                        .sortedByDescending { it.second },
                    spendByDay = expenses
                        .groupBy { it.date }
                        .mapValues { (_, list) -> list.sumOf { it.amountCents } },
                    daysWithNotes = diary.filter { it.note.isNotBlank() }.map { it.date }.toSet(),
                    daysWithMoney = money.map { it.date }.toSet()
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthSummary())
}
