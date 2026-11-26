package com.daybook.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daybook.app.ui.BackupViewModel
import com.daybook.app.ui.theme.*
import java.time.LocalDate

@Composable
fun BackupCard(vm: BackupViewModel = viewModel()) {
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) vm.export(uri) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) pendingRestore = uri }

    PaperCard(Modifier.fillMaxWidth()) {
        Text(
            "Save a copy of your diary and money. In the picker, choose Drive " +
                "to keep it in your Google account.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { exportLauncher.launch("daybook-backup-${LocalDate.now()}.json") },
                enabled = !vm.busy,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) { Text("Save backup") }

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("*/*")) },
                enabled = !vm.busy,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) { Text("Restore") }
        }
        vm.message?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, style = MaterialTheme.typography.labelLarge, color = InkBlue)
        }
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            containerColor = PaperLight,
            title = { Text("Restore backup?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "This replaces everything currently in the app with the contents " +
                        "of the backup file.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.import(uri)
                    pendingRestore = null
                }) { Text("Restore", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text("Cancel") }
            }
        )
    }
}
