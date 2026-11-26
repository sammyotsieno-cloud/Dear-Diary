package com.daybook.app.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daybook.app.data.BackupManager
import kotlinx.coroutines.launch

class BackupViewModel(app: Application) : AndroidViewModel(app) {

    private val manager = BackupManager(app)

    var message by mutableStateOf<String?>(null)
        private set
    var busy by mutableStateOf(false)
        private set

    fun export(uri: Uri) {
        viewModelScope.launch {
            busy = true
            message = try {
                "Backup saved (${manager.export(uri)} items)."
            } catch (e: Exception) {
                "Backup failed: ${e.message}"
            }
            busy = false
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            busy = true
            message = try {
                "Restored ${manager.import(uri)} items."
            } catch (e: Exception) {
                "Restore failed: ${e.message}"
            }
            busy = false
        }
    }
}
