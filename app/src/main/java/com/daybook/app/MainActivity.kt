package com.daybook.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.daybook.app.ui.DaybookApp
import com.daybook.app.ui.LockViewModel
import com.daybook.app.ui.screens.LockScreen
import com.daybook.app.ui.theme.DaybookTheme

class MainActivity : ComponentActivity() {

    private val lockVm: LockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaybookTheme {
                if (lockVm.locked) {
                    LockScreen(onSubmit = lockVm::unlock)
                } else {
                    DaybookApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lockVm.onStarted()
    }

    override fun onStop() {
        lockVm.onStopped()
        super.onStop()
    }
}
