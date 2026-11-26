package com.daybook.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Handwriting = FontFamily.Cursive
val Print = FontFamily.Serif

val DaybookTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = Handwriting, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 42.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Handwriting, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Handwriting, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Print, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Handwriting, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 32.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Print, fontSize = 15.sp, lineHeight = 22.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Print, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Print, fontSize = 12.sp, lineHeight = 16.sp
    )
)
