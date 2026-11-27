package com.daybook.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daybook.app.ui.greetingFor
import com.daybook.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalTime

private val CoverBlue = Color(0xFF2F4B7C)
private val CoinGold = Color(0xFFF2B632)
private val CoinEdge = Color(0xFFC98A10)

@Composable
fun SplashScreen(name: String, onFinished: () -> Unit) {
    val open = remember { Animatable(0f) }
    val reveal = remember { Animatable(0f) }
    val density = LocalDensity.current.density

    LaunchedEffect(Unit) {
        delay(300)
        open.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        reveal.animateTo(1f, tween(700))
        delay(600)
        onFinished()
    }

    val subtitle = if (name.isBlank()) "Your daily notebook"
    else "${greetingFor(LocalTime.now().hour)}, $name"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoverBlue)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
    ) {
        // ---- The first page, revealed as the cover opens ----
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
                .ruledPaper(lineSpacing = 36.dp, firstLineAt = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Daybook",
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = 58.sp,
                    color = InkBlue,
                    modifier = Modifier.graphicsLayer {
                        alpha = reveal.value
                        translationY = (1f - reveal.value) * 24.dp.toPx()
                    }
                )
                Canvas(
                    modifier = Modifier
                        .width(190.dp)
                        .height(16.dp)
                ) {
                    val path = Path().apply {
                        moveTo(0f, size.height * 0.75f)
                        quadraticBezierTo(
                            size.width * 0.5f, -size.height * 0.3f,
                            size.width, size.height * 0.6f
                        )
                    }
                    val measure = PathMeasure()
                    measure.setPath(path, false)
                    val drawn = Path()
                    measure.getSegment(0f, measure.length * reveal.value, drawn, true)
                    drawBehindPath(drawn)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .graphicsLayer { alpha = reveal.value }
                )
            }
        }

        // ---- The cover, swinging open from the left edge ----
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    rotationY = -115f * open.value
                    cameraDistance = 12f * density
                    alpha = if (open.value > 0.8f) ((1f - open.value) / 0.2f).coerceIn(0f, 1f) else 1f
                }
                .background(CoverBlue)
                .drawBehind {
                    // Stitched border
                    drawRoundRect(
                        color = Color(0x66FFFFFF),
                        topLeft = Offset(20.dp.toPx(), 20.dp.toPx()),
                        size = Size(size.width - 40.dp.toPx(), size.height - 40.dp.toPx()),
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(Paper, RoundedCornerShape(6.dp))
                        .border(2.dp, Color(0xFFE6D9BF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 36.dp, vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Daybook",
                            style = MaterialTheme.typography.displaySmall,
                            color = InkBlue
                        )
                        Text(
                            "diary & money",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = InkSoft
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(CoinGold, CircleShape)
                        .border(4.dp, CoinEdge, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(24.dp)
                            .background(CoinEdge, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBehindPath(path: Path) {
    drawPath(
        path = path,
        color = MarginRed,
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )
}
