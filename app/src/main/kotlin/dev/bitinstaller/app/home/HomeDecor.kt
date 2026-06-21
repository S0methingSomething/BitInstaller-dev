package dev.bitinstaller.app.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private const val AMBIENT_GLOW_START_X = -120f
private const val AMBIENT_GLOW_END_X = 420f
private const val AMBIENT_GLOW_START_Y = -150f
private const val AMBIENT_GLOW_END_Y = 300f
private const val AMBIENT_GLOW_X_DURATION_MS = 14_000
private const val AMBIENT_GLOW_Y_DURATION_MS = 18_000
private const val AMBIENT_RADIUS_MULTIPLIER = 1.3f
private const val AMBIENT_GLOW_ALPHA = 0.055f

@Composable
internal fun HomeAmbientGlow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_ambient")
    val glowX by infiniteTransition.animateFloat(
        initialValue = AMBIENT_GLOW_START_X,
        targetValue = AMBIENT_GLOW_END_X,
        animationSpec =
            infiniteRepeatable(
                animation = tween(AMBIENT_GLOW_X_DURATION_MS, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "home_glow_x",
    )
    val glowY by infiniteTransition.animateFloat(
        initialValue = AMBIENT_GLOW_START_Y,
        targetValue = AMBIENT_GLOW_END_Y,
        animationSpec =
            infiniteRepeatable(
                animation = tween(AMBIENT_GLOW_Y_DURATION_MS, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "home_glow_y",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = size.minDimension * AMBIENT_RADIUS_MULTIPLIER
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = AMBIENT_GLOW_ALPHA), Color.Transparent),
                    center = Offset(glowX, glowY),
                    radius = radius,
                ),
            radius = radius,
            center = Offset(glowX, glowY),
        )
    }
}
