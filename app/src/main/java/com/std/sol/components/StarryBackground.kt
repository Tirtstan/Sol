package com.std.sol.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlin.random.Random

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val initialAlpha: Float
)

@Composable
fun StarryBackground(modifier: Modifier = Modifier, starCount: Int = 150) {
    val stars = remember {
        List(starCount) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 1.5f + 0.5f,
                initialAlpha = Random.nextFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "starBlink")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "starProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        stars.forEach { star ->
            val phaseShiftedProgress = (animatedProgress + star.initialAlpha) % 1f
            val currentAlpha = phaseShiftedProgress * 0.7f
            drawCircle(
                color = Color.White.copy(alpha = currentAlpha),
                radius = star.radius,
                center = Offset(star.x * canvasWidth, star.y * canvasHeight)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF25315E)
@Composable
fun StarryBackgroundPreview() {
    StarryBackground(modifier = Modifier.fillMaxSize())
}
