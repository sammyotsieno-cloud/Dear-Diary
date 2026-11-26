package com.daybook.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daybook.app.data.Money
import com.daybook.app.data.MoneyEntry
import com.daybook.app.ui.DaybookViewModel
import com.daybook.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Moods = listOf("😄", "🙂", "😐", "😔", "😡", "😴")

@Composable
fun DayScreen(viewModel: DaybookViewModel, onOpenCalendar: () -> Unit) {
    val date by viewModel.date.collectAsState()
    val money by viewModel.money.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<MoneyEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .systemBarsPadding()
            .imePadding()
    ) {
        SpiralBinding()
        DayHeader(
            date = date,
            onPrev = { viewModel.setDate(date.minusDays(1)) },
            onNext = { viewModel.setDate(date.plusDays(1)) },
            onToday = { viewModel.setDate(LocalDate.now()) },
            onOpenCalendar = onOpenCalendar
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            NotePage(viewModel, date)
            Spacer(Modifier.height(24.dp))
            MoneySection(
                money = money,
                onAdd = { showAdd = true },
                onEdit = { editing = it }
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showAdd) {
        MoneyDialog(
            initial = null,
            onSave = { cents, isExpense, category, label ->
                viewModel.addMoney(date, cents, isExpense, category, label)
                showAdd = false
            },
            onDelete = null,
            onDismiss = { showAdd = false }
        )
    }

    editing?.let { entry ->
        MoneyDialog(
            initial = entry,
            onSave = { cents, isExpense, category, label ->
                viewModel.updateMoney(
                    entry.copy(
                        amountCents = cents,
                        isExpense = isExpense,
                        category = category,
                        label = label
                    )
                )
                editing = null
            },
            onDelete = {
                viewModel.deleteMoney(entry)
                editing = null
            },
            onDismiss = { editing = null }
        )
    }
}

// ---------------------------------------------------------------- Header

@Composable
private fun DayHeader(
    date: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val weekday = remember(date) {
        date.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()))
    }
    val full = remember(date) {
        date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderButton("‹", onPrev)
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !isToday, onClick = onToday),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(weekday, style = MaterialTheme.typography.headlineMedium, color = InkBlue)
            Text(full, style = MaterialTheme.typography.labelLarge, color = InkSoft)
            if (!isToday) {
                Text("tap to jump to today", style = MaterialTheme.typography.labelMedium, color = InkSoft)
            }
        }
        HeaderButton("›", onNext)
        HeaderButton("📅", onOpenCalendar)
    }
}

@Composable
private fun HeaderButton(label: String, onClick: () -> Unit) {
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

// ---------------------------------------------------------------- Diary page

private class NoteState {
    var note by mutableStateOf("")
    var mood by mutableStateOf("")
    var loaded by mutableStateOf(false)
}

@Composable
private fun NotePage(viewModel: DaybookViewModel, date: LocalDate) {
    val state = remember(date) { NoteState() }

    // Load this day's page once.
    LaunchedEffect(state) {
        val entry = viewModel.loadDiary(date)
        state.note = entry?.note ?: ""
        state.mood = entry?.mood ?: ""
        state.loaded = true
    }

    // Auto-save shortly after the last change.
    LaunchedEffect(state, state.note, state.mood, state.loaded) {
        if (state.loaded) {
            delay(600)
            viewModel.saveDiary(date, state.note, state.mood)
        }
    }

    // Save right away when leaving the page or switching day.
    DisposableEffect(state) {
        onDispose {
            if (state.loaded) viewModel.saveDiary(date, state.note, state.mood)
        }
    }

    Text("My day", style = MaterialTheme.typography.titleLarge, color = InkBlue)
    Spacer(Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Moods.forEach { emoji ->
            val selected = state.mood == emoji
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (selected) Highlight else Color.Transparent)
                    .clickable { state.mood = if (selected) "" else emoji },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
        }
    }
    Spacer(Modifier.height(10.dp))

    val shape = RoundedCornerShape(6.dp)
    val textStyle = noteTextStyle()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .background(PaperLight, shape)
            .border(1.dp, PaperEdge, shape)
            .clip(shape)
    ) {
        BasicTextField(
            value = state.note,
            onValueChange = { state.note = it },
            textStyle = textStyle,
            cursorBrush = SolidColor(InkBlue),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = RuleSpacing * 8)
                .ruledPaper()
                .padding(start = 52.dp, end = 12.dp),
            decorationBox = { inner ->
                Box {
                    if (state.note.isEmpty()) {
                        Text(
                            "Dear diary…",
                            style = textStyle.copy(color = InkSoft)
                        )
                    }
                    inner()
                }
            }
        )
    }
}

// ---------------------------------------------------------------- Money

private fun signed(cents: Long): String =
    if (cents < 0) "-" + Money.format(-cents) else Money.format(cents)

@Composable
private fun MoneySection(
    money: List<MoneyEntry>,
    onAdd: () -> Unit,
    onEdit: (MoneyEntry) -> Unit
) {
    val spent = money.filter { it.isExpense }.sumOf { it.amountCents }
    val earned = money.filter { !it.isExpense }.sumOf { it.amountCents }

    Text("Money", style = MaterialTheme.typography.titleLarge, color = InkBlue)
    Spacer(Modifier.height(8.dp))

    PaperCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Total("Spent", Money.format(spent), ExpenseRed)
            Total("Earned", Money.format(earned), IncomeGreen)
            Total("Net", signed(earned - spent), Ink)
        }

        if (money.isEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "No entries for this day yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft
            )
        } else {
            Spacer(Modifier.height(12.dp))
            DashedDivider()
            money.forEach { entry ->
                MoneyRow(entry, onClick = { onEdit(entry) })
                DashedDivider()
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    Button(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text("+ Add entry")
    }
}

@Composable
private fun Total(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = InkSoft)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun MoneyRow(entry: MoneyEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.category, style = MaterialTheme.typography.titleMedium, color = Ink)
            if (entry.label.isNotBlank()) {
                Text(entry.label, style = MaterialTheme.typography.labelMedium, color = InkSoft)
            }
        }
        Text(
            text = (if (entry.isExpense) "-" else "+") + Money.format(entry.amountCents),
            style = MaterialTheme.typography.titleMedium,
            color = if (entry.isExpense) ExpenseRed else IncomeGreen
        )
    }
}
