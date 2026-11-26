package com.daybook.app.data

object Categories {
    val expense = listOf(
        "Food", "Transport", "Bills", "Shopping", "Health", "Fun", "Other"
    )
    val income = listOf(
        "Salary", "Gift", "Other"
    )

    fun forType(isExpense: Boolean) = if (isExpense) expense else income
}
