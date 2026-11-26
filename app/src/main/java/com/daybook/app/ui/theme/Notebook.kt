package com.daybook.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Distance between ruled lines. Note text uses the same value as its line height. */
val RuleSpacing = 32.dp

/**
 * Draws ruled lines and a red margin line behind a composable.
 * For scrolling pages, put it AFTER verticalScroll() so the lines scroll with the text:
 *   Modifier.verticalScroll(state).ruledPaper()
 */
fun Modifier.ruledPaper(
    lineSpacing: Dp = RuleSpacing,
    firstLineAt: Dp = RuleSpacing,
    marginAt: Dp = 40.dp,
    showMargin: Boolean = true
): Modifier = this.drawBehind {
    val step = lineSpacing.toPx()
    var y = firstLineAt.toPx()
    while (y < size.height) {
        drawLine(
            color = RuleBlue,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
        y += step
    }
    if (showMargin) {
        val x = marginAt.toPx()
        drawLine(
            color = MarginRed,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

/** Row of spiral rings, for the top of a page. */
@Composable
fun SpiralBinding(modifier: Modifier = Modifier, spacing: Dp = 26.dp) {
    Canvas(modifier = modifier.fillMaxWidth().height(34.dp)) {
        val step = spacing.toPx()
        val count = (size.width / step).toInt()
        if (count < 1) return@Canvas
        val startX = (size.width - (count - 1) * step) / 2f
        for (i in 0 until count) {
            val x = startX + i * step
            drawCircle(
                color = BindingHole,
                radius = 5.dp.toPx(),
                center = Offset(x, 22.dp.toPx())
            )
            drawLine(
                color = BindingMetal,
                start = Offset(x, 3.dp.toPx()),
                end = Offset(x, 22.dp.toPx()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/** A slip of paper with a soft shadow. */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = PaperLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, PaperEdge)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

/** Thin dashed line, like a tear-off edge. */
@Composable
fun DashedDivider(modifier: Modifier = Modifier, color: Color = RuleBlue) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
        )
    }
}

/** Handwriting style whose line height matches the ruled lines, so text sits on them. */
@Composable
fun noteTextStyle(): TextStyle {
    val lineHeight = with(LocalDensity.current) { RuleSpacing.toSp() }
    return MaterialTheme.typography.bodyLarge.copy(
        color = Ink,
        lineHeight = lineHeight,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.None
        )
    )
}
