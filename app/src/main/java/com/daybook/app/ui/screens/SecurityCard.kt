package com.daybook.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daybook.app.ui.LockViewModel
import com.daybook.app.ui.theme.*

private enum class Mode { SET, CHANGE, REMOVE }

@Composable
fun SecurityCard() {
    // Use the activity's copy so the lock state is shared with MainActivity.
    val activity = LocalContext.current as ComponentActivity
    val vm: LockViewModel = viewModel(viewModelStoreOwner = activity)
    var mode by remember { mutableStateOf<Mode?>(null) }

    PaperCard(Modifier.fillMaxWidth()) {
        Text(
            if (vm.hasPassword)
                "Your diary is protected. It locks again after you've been away for a minute."
            else
                "Protect your diary and money with a password.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft
        )
        Spacer(Modifier.height(12.dp))
        if (vm.hasPassword) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { mode = Mode.CHANGE },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Change") }
                OutlinedButton(
                    onClick = { mode = Mode.REMOVE },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Remove") }
            }
        } else {
            Button(
                onClick = { mode = Mode.SET },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Set password") }
        }
    }

    mode?.let { PasswordDialog(it, vm) { mode = null } }
}

@Composable
private fun PasswordDialog(mode: Mode, vm: LockViewModel, onDismiss: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val needsCurrent = mode != Mode.SET
    val needsNew = mode != Mode.REMOVE

    fun submit() {
        if (needsNew) {
            if (newPw.length < 4) { error = "Use at least 4 characters"; return }
            if (newPw != confirm) { error = "The two passwords don't match"; return }
        }
        val ok = when (mode) {
            Mode.SET -> { vm.setPassword(newPw); true }
            Mode.CHANGE -> vm.changePassword(current, newPw)
            Mode.REMOVE -> vm.removePassword(current)
        }
        if (ok) onDismiss() else error = "Current password is wrong"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperLight,
        title = {
            Text(
                when (mode) {
                    Mode.SET -> "Set a password"
                    Mode.CHANGE -> "Change password"
                    Mode.REMOVE -> "Remove password"
                },
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (needsCurrent) {
                    PasswordField(current, "Current password") { current = it; error = null }
                }
                if (needsNew) {
                    PasswordField(newPw, "New password") { newPw = it; error = null }
                    PasswordField(confirm, "Repeat new password") { confirm = it; error = null }
                }
                if (mode == Mode.SET) {
                    Text(
                        "There is no password recovery. If you forget it, the only way back in " +
                            "is clearing the app's data, which erases the diary. Keep a Drive backup.",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSoft
                    )
                }
                error?.let {
                    Text(it, style = MaterialTheme.typography.labelLarge, color = ExpenseRed)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { submit() }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PasswordField(value: String, label: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}
