package com.daybook.app.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daybook.app.ui.screens.DayScreen
import com.daybook.app.ui.screens.MonthScreen
import com.daybook.app.ui.theme.*

object Routes {
    const val DAY = "day"
    const val MONTH = "month"
}

@Composable
fun DaybookApp(viewModel: DaybookViewModel = viewModel()) {
    val nav = rememberNavController()
    val context = LocalContext.current
    var showExit by remember { mutableStateOf(false) }

    // Ask before leaving, but only on the main page.
    val backStackEntry by nav.currentBackStackEntryAsState()
    val onDayScreen = backStackEntry?.destination?.route == Routes.DAY
    BackHandler(enabled = onDayScreen) { showExit = true }

    NavHost(navController = nav, startDestination = Routes.DAY) {
        composable(Routes.DAY) {
            DayScreen(
                viewModel = viewModel,
                onOpenCalendar = { nav.navigate(Routes.MONTH) }
            )
        }
        composable(Routes.MONTH) {
            MonthScreen(
                viewModel = viewModel,
                onPickDay = { date ->
                    viewModel.setDate(date)
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() }
            )
        }
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            containerColor = PaperLight,
            title = { Text("Close Daybook?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Your diary is saved automatically.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExit = false
                    (context as? Activity)?.finish()
                }) { Text("Exit", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { showExit = false }) { Text("Stay") }
            }
        )
    }
}
