package com.daybook.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daybook.app.ui.theme.*

@Composable
fun LockScreen(onSubmit: (String) -> Boolean) {
    var input by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    fun tryUnlock() {
        if (!onSubmit(input)) {
            wrong = true
            input = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .systemBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpiralBinding()
        Spacer(Modifier.weight(1f))

        Text("🔒", fontSize = 48.sp)
        Text("Daybook", style = MaterialTheme.typography.displaySmall, color = InkBlue)
        Spacer(Modifier.height(4.dp))
        Text(
            "Enter your password to open your diary",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it; wrong = false },
            label = { Text("Password") },
            singleLine = true,
            isError = wrong,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { tryUnlock() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
        if (wrong) {
            Spacer(Modifier.height(6.dp))
            Text("Wrong password", style = MaterialTheme.typography.labelLarge, color = ExpenseRed)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { tryUnlock() },
            enabled = input.isNotEmpty(),
            shape = RoundedCornerShape(6.dp)
        ) { Text("Open") }

        Spacer(Modifier.weight(1.4f))
    }
}
