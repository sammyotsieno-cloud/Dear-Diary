package com.daybook.app.data

import java.math.BigDecimal
import java.math.RoundingMode

object Money {

    /** "12.50" or "12,50" -> 1250. Returns null if invalid or not above zero. */
    fun parseToCents(input: String): Long? {
        val value = input.trim().replace(',', '.').toBigDecimalOrNull() ?: return null
        if (value.signum() <= 0) return null
        return value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
    }

    /** 123456 -> "1,234.56" */
    fun format(cents: Long): String =
        String.format("%,.2f", BigDecimal.valueOf(cents, 2))

    /** Plain number for editing: 1250 -> "12.50" */
    fun toInput(cents: Long): String = BigDecimal.valueOf(cents, 2).toPlainString()
}
