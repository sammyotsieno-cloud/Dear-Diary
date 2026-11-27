package com.daybook.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daybook.app.ui.ProfileViewModel
import com.daybook.app.ui.greetingFor
import com.daybook.app.ui.theme.*
import java.time.LocalTime

@Composable
fun GreetingBar() {
    val activity = LocalContext.current as ComponentActivity
    val profile: ProfileViewModel = viewModel(viewModelStoreOwner = activity)
    var editing by remember { mutableStateOf(false) }

    val greeting = remember { greetingFor(LocalTime.now().hour) }
    val text = if (profile.name.isBlank()) "Hello! Tap here to add your name"
    else "$greeting, ${profile.name}"

    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = InkSoft,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { editing = true }
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )

    if (editing) {
        var draft by remember { mutableStateOf(profile.name) }
        AlertDialog(
            onDismissRequest = { editing = false },
            containerColor = PaperLight,
            title = { Text("Your name", style = MaterialTheme.typography.titleLarge) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    profile.saveName(draft)
                    editing = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text("Cancel") }
            }
        )
    }
}
