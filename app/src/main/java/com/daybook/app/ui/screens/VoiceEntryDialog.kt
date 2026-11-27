package com.daybook.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.daybook.app.data.DraftMoney
import com.daybook.app.data.EntryOrganizer
import com.daybook.app.data.Money
import com.daybook.app.speech.SpeechController
import com.daybook.app.ui.theme.*

private enum class Stage { LISTENING, REVIEW }

private val VoiceMoods = listOf("😄", "🙂", "😐", "😔", "😡", "😴")

@Composable
fun VoiceEntryDialog(
    onApply: (note: String, mood: String, money: List<DraftMoney>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var stage by remember { mutableStateOf(Stage.LISTENING) }
    var listening by remember { mutableStateOf(false) }
    var transcript by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    var note by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    val money = remember { mutableStateListOf<DraftMoney>() }

    val controller = remember {
        SpeechController(
            context = context,
            onTranscript = { transcript = it },
            onError = { message ->
                error = message
                listening = false
            }
        )
    }
    DisposableEffect(Unit) { onDispose { controller.stop() } }

    fun begin() {
        error = null
        transcript = ""
        if (!controller.isAvailable) {
            error = "Speech recognition isn't available on this phone."
            return
        }
        controller.start()
        listening = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) begin() else error = "Microphone permission is needed to dictate."
    }

    fun ensureAndBegin() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) begin() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun finishListening() {
        controller.stop()
        listening = false
        val text = controller.transcript()
        if (text.isBlank()) {
            error = "I didn't catch anything. Try again."
            return
        }
        val result = EntryOrganizer.organize(text)
        note = result.note
        mood = result.mood
        money.clear()
        money.addAll(result.money)
        stage = Stage.REVIEW
    }

    LaunchedEffect(Unit) { ensureAndBegin() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperLight,
        title = {
            Text(
                if (stage == Stage.LISTENING) "Dictate your day" else "Review your entry",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (stage == Stage.LISTENING) {
                    Text(
                        if (listening)
                            "🎙 Listening… talk naturally and tap Stop when you're done. " +
                                "Mention what you spent or earned, with amounts."
                        else
                            "Not listening.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft
                    )
                    if (transcript.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PaperDeep)
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(transcript, style = MaterialTheme.typography.bodyMedium, color = Ink)
                        }
                    }
                    error?.let {
                        Text(it, style = MaterialTheme.typography.labelLarge, color = ExpenseRed)
                    }
                } else {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Diary note") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Mood", style = MaterialTheme.typography.labelLarge, color = InkSoft)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        VoiceMoods.forEach { emoji ->
                            val selected = mood == emoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Highlight else Color.Transparent)
                                    .clickable { mood = if (selected) "" else emoji },
                                contentAlignment = Alignment.Center
                            ) { Text(emoji, fontSize = 22.sp) }
                        }
                    }

                    Text("Money found", style = MaterialTheme.typography.labelLarge, color = InkSoft)
                    if (money.isEmpty()) {
                        Text(
                            "No amounts found. You can add entries by hand afterwards.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSoft
                        )
                    }
                    money.toList().forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.category, style = MaterialTheme.typography.titleMedium)
                                if (entry.label.isNotBlank()) {
                                    Text(
                                        entry.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = InkSoft
                                    )
                                }
                            }
                            Text(
                                (if (entry.isExpense) "-" else "+") + Money.format(entry.amountCents),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (entry.isExpense) ExpenseRed else IncomeGreen
                            )
                            TextButton(onClick = { money.remove(entry) }) { Text("✕") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (stage == Stage.LISTENING) {
                if (listening) {
                    TextButton(onClick = { finishListening() }) { Text("Stop") }
                } else {
                    TextButton(onClick = { ensureAndBegin() }) { Text("Try again") }
                }
            } else {
                TextButton(
                    enabled = note.isNotBlank() || money.isNotEmpty(),
                    onClick = { onApply(note.trim(), mood, money.toList()) }
                ) { Text("Add to diary") }
            }
        },
        dismissButton = {
            Row {
                if (stage == Stage.REVIEW) {
                    TextButton(onClick = {
                        stage = Stage.LISTENING
                        ensureAndBegin()
                    }) { Text("Record again") }
                } else if (!listening && transcript.isNotBlank()) {
                    TextButton(onClick = { finishListening() }) { Text("Use text") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
