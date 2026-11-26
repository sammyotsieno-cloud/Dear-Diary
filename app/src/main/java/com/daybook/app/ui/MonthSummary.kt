package com.daybook.app.ui

/** Everything the calendar page needs for one month. */
data class MonthSummary(
    val incomeCents: Long = 0,
    val expenseCents: Long = 0,
    val expenseByCategory: List<Pair<String, Long>> = emptyList(), // biggest first
    val spendByDay: Map<String, Long> = emptyMap(),                // "yyyy-MM-dd" -> cents spent
    val daysWithNotes: Set<String> = emptySet(),
    val daysWithMoney: Set<String> = emptySet()
) {
    val balanceCents: Long get() = incomeCents - expenseCents
}
