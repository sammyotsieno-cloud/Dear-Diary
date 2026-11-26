package com.daybook.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NotebookColors = lightColorScheme(
    primary = InkBlue,
    onPrimary = Paper,
    primaryContainer = Highlight,
    onPrimaryContainer = Ink,
    secondary = MarginRed,
    onSecondary = Ink,
    secondaryContainer = PaperDeep,
    onSecondaryContainer = Ink,
    background = Paper,
    onBackground = Ink,
    surface = PaperLight,
    onSurface = Ink,
    surfaceVariant = PaperDeep,
    onSurfaceVariant = InkSoft,
    outline = PaperEdge,
    outlineVariant = RuleBlue,
    error = ExpenseRed,
    onError = Paper
)

@Composable
fun DaybookTheme(content: @Composable () -> Unit) {
    // Paper is always light, so there is no dark mode.
    MaterialTheme(
        colorScheme = NotebookColors,
        typography = DaybookTypography,
        content = content
    )
}
