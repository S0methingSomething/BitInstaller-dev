package dev.bitinstaller.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
internal fun RecompositionCounter(debug: DebugState) {
    var count by remember { mutableIntStateOf(0) }
    count++

    DisposableEffect(Unit) {
        onDispose { }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            debug.recompositionCounter.intValue = count
            delay(RECOMPOSITION_COUNTER_INTERVAL_MS)
        }
    }
}

@Composable
internal fun FpsCounter(debug: DebugState) {
    val fps = remember { mutableIntStateOf(0) }
    var frameCount by remember { mutableIntStateOf(0) }

    frameCount++

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(FPS_COUNTER_INTERVAL_MS)
            fps.intValue = frameCount
            frameCount = 0
            debug.fps.intValue = fps.intValue
        }
    }
}

@Composable
internal fun MemoryMonitor(debug: DebugState) {
    val runtime = remember { Runtime.getRuntime() }

    LaunchedEffect(Unit) {
        while (isActive) {
            val used = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MIB
            val max = runtime.maxMemory() / BYTES_PER_MIB
            debug.usedMemoryMb.intValue = used.toInt()
            debug.maxMemoryMb.intValue = max.toInt()
            delay(MEMORY_MONITOR_INTERVAL_MS)
        }
    }
}

@Composable
internal fun DebugToolbar(
    debug: DebugState,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.78f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            DebugMetricRow(label = "RECOMP", value = "${debug.recompositionCounter.intValue}")
            DebugMetricRow(label = "FPS", value = "${debug.fps.intValue}")
            DebugMetricRow(
                label = "MEM",
                value = "${debug.usedMemoryMb.intValue}/${debug.maxMemoryMb.intValue} MB",
            )
        }
    }
}

@Composable
private fun DebugMetricRow(
    label: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
        )
    }
}

@Composable
internal fun DebugOverlays(
    debug: DebugState,
    modifier: Modifier = Modifier,
) {
    if (!debug.visible.value) return

    Box(modifier = modifier) {
        DebugToolbar(
            debug = debug,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
        )
    }
}

private const val RECOMPOSITION_COUNTER_INTERVAL_MS = 1000L
private const val FPS_COUNTER_INTERVAL_MS = 1000L
private const val MEMORY_MONITOR_INTERVAL_MS = 2000L
private const val BYTES_PER_KB = 1024L
private const val BYTES_PER_MIB = BYTES_PER_KB * BYTES_PER_KB
