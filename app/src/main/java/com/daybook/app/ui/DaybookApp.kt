package com.daybook.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daybook.app.ui.screens.DayScreen
import com.daybook.app.ui.screens.MonthScreen

object Routes {
    const val DAY = "day"
    const val MONTH = "month"
}

@Composable
fun DaybookApp(viewModel: DaybookViewModel = viewModel()) {
    val nav = rememberNavController()

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
}
