package com.daybook.app.ui

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.daybook.app.data.LockManager

class LockViewModel(app: Application) : AndroidViewModel(app) {

    private val manager = LockManager(app)

    var hasPassword by mutableStateOf(manager.hasPassword())
        private set

    /** Starts locked whenever a password exists. */
    var locked by mutableStateOf(manager.hasPassword())
        private set

    private var leftAt = 0L

    fun unlock(input: String): Boolean {
        val ok = manager.verify(input)
        if (ok) locked = false
        return ok
    }

    fun setPassword(password: String) {
        manager.setPassword(password)
        hasPassword = true
        locked = false
    }

    fun changePassword(current: String, new: String): Boolean {
        if (!manager.verify(current)) return false
        manager.setPassword(new)
        return true
    }

    fun removePassword(current: String): Boolean {
        if (!manager.verify(current)) return false
        manager.clear()
        hasPassword = false
        locked = false
        return true
    }

    fun onStopped() {
        leftAt = SystemClock.elapsedRealtime()
    }

    fun onStarted() {
        if (hasPassword && leftAt != 0L &&
            SystemClock.elapsedRealtime() - leftAt > RELOCK_AFTER_MS
        ) {
            locked = true
        }
    }

    private companion object {
        const val RELOCK_AFTER_MS = 60_000L
    }
}
