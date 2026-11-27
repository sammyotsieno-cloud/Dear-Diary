package com.daybook.app.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Hello"
}

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("daybook_profile", Context.MODE_PRIVATE)

    var name by mutableStateOf(prefs.getString("name", "") ?: "")
        private set

    /** False until the welcome screen has been completed or skipped. */
    var onboarded by mutableStateOf(prefs.getBoolean("onboarded", false))
        private set

    fun saveName(newName: String) {
        name = newName.trim()
        onboarded = true
        prefs.edit().putString("name", name).putBoolean("onboarded", true).apply()
    }

    fun skipOnboarding() {
        onboarded = true
        prefs.edit().putBoolean("onboarded", true).apply()
    }
}
