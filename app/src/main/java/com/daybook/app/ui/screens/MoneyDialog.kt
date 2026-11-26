package com.daybook.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.daybook.app.data.Categories
import com.daybook.app.data.Money
import com.daybook.app.data.MoneyEntry
import com.daybook.app.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoneyDialog(
    initial: MoneyEntry?,
    onSave: (cents: Long, isExpense: Boolean, category: String, label: String) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var isExpense by remember { mutableStateOf(initial?.isExpense ?: true) }
    var amount by remember {
        mutableStateOf(initial?.let { Money.toInput(it.amountCents) } ?: "")
    }
    var category by remember {
        mutableStateOf(initial?.category ?: Categories.expense.first())
    }
    var label by remember { mutableStateOf(initial?.label ?: "") }

    val cents = Money.parseToCents(amount)
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Highlight,
        selectedLabelColor = Ink
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperLight,
        title = {
            Text(
                if (initial == null) "New entry" else "Edit entry",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isExpense,
                        onClick = {
                            if (!isExpense) {
                                isExpense = true
                                category = Categories.expense.first()
                            }
                        },
                        label = { Text("Expense") },
                        colors = chipColors
                    )
                    FilterChip(
                        selected = !isExpense,
                        onClick = {
                            if (isExpense) {
                                isExpense = false
                                category = Categories.income.first()
                            }
                        },
                        label = { Text("Income") },
                        colors = chipColors
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    isError = amount.isNotBlank() && cents == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Categories.forType(isExpense).forEach { c ->
                        FilterChip(
                            selected = c == category,
                            onClick = { category = c },
                            label = { Text(c) },
                            colors = chipColors
                        )
                    }
                }

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Note (optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = cents != null,
                onClick = { onSave(cents!!, isExpense, category, label.trim()) }
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = ExpenseRed)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
