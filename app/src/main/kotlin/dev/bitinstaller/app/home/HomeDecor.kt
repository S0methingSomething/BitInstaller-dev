package dev.bitinstaller.app.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private const val AMBIENT_GLOW_START_X = -120f
private const val AMBIENT_GLOW_END_X = 420f
private const val AMBIENT_GLOW_START_Y = -150f
private const val AMBIENT_GLOW_END_Y = 300f
private const val AMBIENT_GLOW_X_DURATION_MS = 14_000
private const val AMBIENT_GLOW_Y_DURATION_MS = 18_000
private const val AMBIENT_RADIUS_MULTIPLIER = 1.3f
private const val AMBIENT_GLOW_ALPHA = 0.055f
private const val CENTER_DIVISOR = 2f
private const val BEACON_ROTATION_DEGREES = 360f
private const val BEACON_ROTATION_DURATION_MS = 2400
private const val BEACON_PULSE_DURATION_MS = 1600
private const val BEACON_PULSE_START_SCALE = 0.5f
private const val BEACON_PULSE_END_SCALE = 1f
private const val BEACON_PULSE_ALPHA = 0.08f
private const val BEACON_RING_ALPHA = 0.15f
private const val BEACON_RING_RADIUS_SCALE = 0.7f
private const val BEACON_RING_STROKE_DP = 1.5f
private const val BEACON_ARC_ALPHA = 0.45f
private const val BEACON_ARC_SWEEP_DEGREES = 90f
private const val BEACON_CORE_RADIUS_SCALE = 0.25f
private const val STATIC_BEACON_ALPHA = 0.08f

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

@Composable
internal fun HomeBeacon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_beacon")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = BEACON_ROTATION_DEGREES,
        animationSpec =
            infiniteRepeatable(
                animation = tween(BEACON_ROTATION_DURATION_MS),
                repeatMode = RepeatMode.Restart,
            ),
        label = "home_beacon_rotation",
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = BEACON_PULSE_START_SCALE,
        targetValue = BEACON_PULSE_END_SCALE,
        animationSpec =
            infiniteRepeatable(
                animation = tween(BEACON_PULSE_DURATION_MS, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "home_beacon_pulse",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / CENTER_DIVISOR, size.height / CENTER_DIVISOR)
        val radius = size.minDimension / CENTER_DIVISOR
        drawCircle(
            color = Color.White.copy(alpha = BEACON_PULSE_ALPHA * (BEACON_PULSE_END_SCALE - pulseScale)),
            radius = radius * pulseScale,
            center = center,
        )
        drawCircle(
            color = Color.White.copy(alpha = BEACON_RING_ALPHA),
            radius = radius * BEACON_RING_RADIUS_SCALE,
            center = center,
            style = Stroke(width = BEACON_RING_STROKE_DP.dp.toPx()),
        )
        drawArc(
            color = Color.White.copy(alpha = BEACON_ARC_ALPHA),
            startAngle = rotation,
            sweepAngle = BEACON_ARC_SWEEP_DEGREES,
            useCenter = true,
            size = Size(size.width * BEACON_RING_RADIUS_SCALE, size.height * BEACON_RING_RADIUS_SCALE),
            topLeft =
                Offset(
                    (size.width - size.width * BEACON_RING_RADIUS_SCALE) / CENTER_DIVISOR,
                    (size.height - size.height * BEACON_RING_RADIUS_SCALE) / CENTER_DIVISOR,
                ),
        )
        drawCircle(color = Color.White, radius = radius * BEACON_CORE_RADIUS_SCALE, center = center)
    }
}

@Composable
internal fun StaticHomeBeacon(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = STATIC_BEACON_ALPHA)),
    )
}
