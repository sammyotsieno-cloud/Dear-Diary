package com.daybook.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.daybook.app.ui.DaybookApp
import com.daybook.app.ui.LockViewModel
import com.daybook.app.ui.ProfileViewModel
import com.daybook.app.ui.screens.LockScreen
import com.daybook.app.ui.screens.SplashScreen
import com.daybook.app.ui.screens.WelcomeScreen
import com.daybook.app.ui.theme.DaybookTheme

class MainActivity : ComponentActivity() {

    private val lockVm: LockViewModel by viewModels()
    private val profileVm: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaybookTheme {
                var splashDone by rememberSaveable { mutableStateOf(false) }
                when {
                    !splashDone -> SplashScreen(
                        name = profileVm.name,
                        onFinished = { splashDone = true }
                    )
                    lockVm.locked -> LockScreen(onSubmit = lockVm::unlock)
                    !profileVm.onboarded -> WelcomeScreen(
                        onSave = profileVm::saveName,
                        onSkip = profileVm::skipOnboarding
                    )
                    else -> DaybookApp()
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
