package com.daybook.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daybook.app.data.Money
import com.daybook.app.ui.DaybookViewModel
import com.daybook.app.ui.MonthSummary
import com.daybook.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun MonthScreen(
    viewModel: DaybookViewModel,
    onPickDay: (LocalDate) -> Unit,
    onBack: () -> Unit
) {
    val month by viewModel.month.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()
    val selected by viewModel.date.collectAsState()

    BackHandler(onBack = onBack)

    // Open on the month of the day you were viewing.
    LaunchedEffect(Unit) { viewModel.setMonth(YearMonth.from(selected)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .systemBarsPadding()
    ) {
        SpiralBinding()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundButton("←", onBack)
            Spacer(Modifier.weight(1f))
            RoundButton("‹", viewModel::previousMonth)
            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                style = MaterialTheme.typography.headlineMedium,
                color = InkBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            RoundButton("›", viewModel::nextMonth)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(44.dp)) // balances the back button
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            PaperCard(Modifier.fillMaxWidth()) {
                CalendarGrid(
                    month = month,
                    summary = summary,
                    selected = selected,
                    onPick = onPickDay
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Dot(InkBlue)
                    Text(
                        "  note      ",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSoft
                    )
                    Dot(ExpenseRed)
                    Text(
                        "  money",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSoft
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("This month", style = MaterialTheme.typography.titleLarge, color = InkBlue)
            Spacer(Modifier.height(8.dp))
            SummaryCard(summary)

            Spacer(Modifier.height(24.dp))
            Text("Where it went", style = MaterialTheme.typography.titleLarge, color = InkBlue)
            Spacer(Modifier.height(8.dp))
            CategoryCard(summary)

            Spacer(Modifier.height(24.dp))
            Text("Backup", style = MaterialTheme.typography.titleLarge, color = InkBlue)
            Spacer(Modifier.height(8.dp))
            BackupCard()

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ---------------------------------------------------------------- Calendar

@Composable
private fun CalendarGrid(
    month: YearMonth,
    summary: MonthSummary,
    selected: LocalDate,
    onPick: (LocalDate) -> Unit
) {
    val locale = Locale.getDefault()
    val firstDow = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val offset = (month.atDay(1).dayOfWeek.value - firstDow.value + 7) % 7
    val daysInMonth = month.lengthOfMonth()
    val rows = (offset + daysInMonth + 6) / 7
    val today = LocalDate.now()

    Row(modifier = Modifier.fillMaxWidth()) {
        for (i in 0 until 7) {
            Text(
                text = firstDow.plus(i.toLong()).getDisplayName(JTextStyle.NARROW, locale),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = InkSoft
            )
        }
    }
    Spacer(Modifier.height(4.dp))

    for (r in 0 until rows) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (c in 0 until 7) {
                val dayNumber = r * 7 + c - offset + 1
                if (dayNumber < 1 || dayNumber > daysInMonth) {
                    Spacer(Modifier.weight(1f).aspectRatio(1f))
                } else {
                    val date = month.atDay(dayNumber)
                    val key = date.toString()
                    DayCell(
                        modifier = Modifier.weight(1f),
                        day = dayNumber,
                        isSelected = date == selected,
                        isToday = date == today,
                        hasNote = key in summary.daysWithNotes,
                        hasMoney = key in summary.daysWithMoney,
                        onClick = { onPick(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier,
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasNote: Boolean,
    hasMoney: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(shape)
            .background(if (isSelected) Highlight else Color.Transparent)
            .then(if (isToday) Modifier.border(1.5.dp, InkBlue, shape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
            Row(
                modifier = Modifier.height(6.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (hasNote) Dot(InkBlue)
                if (hasMoney) Dot(ExpenseRed)
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun RoundButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 26.sp, color = InkBlue)
    }
}

// ---------------------------------------------------------------- Summary

private fun signedAmount(cents: Long): String =
    if (cents < 0) "-" + Money.format(-cents) else Money.format(cents)

@Composable
private fun SummaryCard(summary: MonthSummary) {
    PaperCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Stat("Earned", Money.format(summary.incomeCents), IncomeGreen)
            Stat("Spent", Money.format(summary.expenseCents), ExpenseRed)
            Stat("Balance", signedAmount(summary.balanceCents), Ink)
        }
        Spacer(Modifier.height(12.dp))
        DashedDivider()
        Spacer(Modifier.height(10.dp))
        Text(
            text = "${summary.daysWithNotes.size} days with notes  ·  " +
                "${summary.daysWithMoney.size} days with money",
            style = MaterialTheme.typography.labelMedium,
            color = InkSoft,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Stat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = InkSoft)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun CategoryCard(summary: MonthSummary) {
    PaperCard(Modifier.fillMaxWidth()) {
        if (summary.expenseByCategory.isEmpty()) {
            Text(
                "No spending recorded this month.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft
            )
            return@PaperCard
        }

        val biggest = summary.expenseByCategory.first().second.toFloat()
        val total = summary.expenseCents.toFloat()

        summary.expenseByCategory.forEachIndexed { index, (category, cents) ->
            if (index > 0) Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    category,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${Money.format(cents)}  (${(cents / total * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkSoft
                )
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PaperDeep)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((cents / biggest).coerceIn(0.03f, 1f))
                        .height(8.dp)
                        .background(MarginRed)
                )
            }
        }
    }
}
