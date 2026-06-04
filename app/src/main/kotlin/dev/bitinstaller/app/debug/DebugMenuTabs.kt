package dev.bitinstaller.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
internal fun DebugToolsTab(
    debug: DebugState,
    scenarioRunner: DebugScenarioRunner,
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        item { DebugSectionLabel(text = "Quick Tests") }
        item { DebugQuickTestsSection(debug = debug, scenarioRunner = scenarioRunner, scope = scope) }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { DebugSectionLabel(text = "Recording") }
        item { DebugRecordingSection(debug = debug) }
        item { DebugSectionLabel(text = "Monitoring") }
        item { DebugMonitoringSection(debug = debug) }
        item { DebugSectionLabel(text = "Snapshots") }
        item { DebugSnapshotSection(debug = debug) }
    }
}

@Composable
private fun DebugQuickTestsSection(
    debug: DebugState,
    scenarioRunner: DebugScenarioRunner,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                scope.launch { scenarioRunner.toastSpam { debug.logEvent("toast: $it") } }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        ) {
            Text(text = "Toast Spam · ${TOAST_SPAM_REPETITIONS}x", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = {
                scope.launch { scenarioRunner.saveScanStress { debug.logEvent("save_scan_batch") } }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        ) {
            Text(text = "Save Scan Stress · ${SAVE_SCAN_REPETITIONS}x", fontWeight = FontWeight.Bold)
        }
        FilledTonalButton(
            onClick = {
                scenarioRunner.stop()
                debug.logEvent("all_scenarios_stopped")
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp),
        ) {
            Text(text = "Stop All Scenarios")
        }
    }
}

@Composable
private fun DebugRecordingSection(debug: DebugState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(
            onClick = { debug.startRecording() },
            enabled = !debug.isRecording.value,
            modifier = Modifier.weight(1f).heightIn(min = 42.dp),
        ) {
            Text(text = "Start Recording")
        }
        FilledTonalButton(
            onClick = { debug.stopRecording() },
            enabled = debug.isRecording.value,
            modifier = Modifier.weight(1f).heightIn(min = 42.dp),
        ) {
            Text(text = "Stop Recording")
        }
    }
}

@Composable
private fun DebugMonitoringSection(debug: DebugState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DebugStatRow(label = "Recompositions", value = debug.recompositionCounter.intValue.toString())
        DebugStatRow(label = "Frames/sec", value = debug.fps.intValue.toString())
        DebugStatRow(
            label = "Memory",
            value = "${debug.usedMemoryMb.intValue} / ${debug.maxMemoryMb.intValue} MB",
        )
        DebugStatRow(label = "Text Scale", value = "%.2fx".format(debug.textScale.floatValue))
        DebugStatRow(label = "Anim Speed", value = "%.2fx".format(debug.animationSpeed.floatValue))
    }
}

@Composable
private fun DebugSnapshotSection(debug: DebugState) {
    TextButton(
        onClick = {
            val snapshot =
                buildString {
                    appendLine("=== State Snapshot ===")
                    appendLine("recomp=${debug.recompositionCounter.intValue}")
                    appendLine("fps=${debug.fps.intValue}")
                    appendLine("mem=${debug.usedMemoryMb.intValue}/${debug.maxMemoryMb.intValue} MB")
                    appendLine("scale=${debug.textScale.floatValue}")
                    appendLine("anim=${debug.animationSpeed.floatValue}")
                    appendLine("theme=${if (debug.darkTheme.value) "dark" else "light"}")
                    appendLine("recording=${debug.isRecording.value}")
                    appendLine("scenario=${debug.activeScenario.value ?: "none"}")
                }
            debug.eventLog.add("snapshot: ${snapshot.take(SNAPSHOT_PREVIEW_LENGTH)}...")
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Capture State Snapshot")
    }
}

@Composable
internal fun DebugLogTab(debug: DebugState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
        ) {
            Text(
                text = "Event Log (${debug.eventLog.size})",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = DEBUG_LOG_LABEL_ALPHA),
            )
            TextButton(onClick = { debug.clearLog() }) {
                Text(text = "Clear", color = Color.White.copy(alpha = DEBUG_LOG_CLEAR_ALPHA))
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        ) {
            items(debug.eventLog.reversed()) { entry ->
                Text(
                    text = entry,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White.copy(alpha = DEBUG_LOG_ENTRY_ALPHA),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
internal fun DebugOverridesTab(debug: DebugState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        item { DebugSectionLabel(text = "Animation Speed") }
        item { DebugAnimationSpeedSection(debug = debug) }
        item { DebugSectionLabel(text = "Text Scale") }
        item { DebugTextScaleSection(debug = debug) }
        item { DebugSectionLabel(text = "Theme") }
        item { DebugThemeSection(debug = debug) }
    }
}

@Composable
private fun DebugAnimationSpeedSection(debug: DebugState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(OVERRIDE_SPEED_SLOW, OVERRIDE_SPEED_NORMAL, OVERRIDE_SPEED_FAST).forEach { speed ->
            val label =
                when (speed) {
                    OVERRIDE_SPEED_SLOW -> "0.5x"
                    OVERRIDE_SPEED_NORMAL -> "1x"
                    OVERRIDE_SPEED_FAST -> "2x"
                    else -> "??"
                }
            FilledTonalButton(
                onClick = { debug.animationSpeed.floatValue = speed },
                modifier = Modifier.weight(1f).heightIn(min = 42.dp),
            ) {
                Text(text = label)
            }
        }
    }
}

@Composable
private fun DebugTextScaleSection(debug: DebugState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(OVERRIDE_SCALE_SMALL, OVERRIDE_SCALE_NORMAL, OVERRIDE_SCALE_LARGE).forEach { scale ->
            val label =
                when (scale) {
                    OVERRIDE_SCALE_SMALL -> "0.8x"
                    OVERRIDE_SCALE_NORMAL -> "1x"
                    OVERRIDE_SCALE_LARGE -> "1.3x"
                    else -> "??"
                }
            FilledTonalButton(
                onClick = { debug.textScale.floatValue = scale },
                modifier = Modifier.weight(1f).heightIn(min = 42.dp),
            ) {
                Text(text = label)
            }
        }
    }
}

@Composable
private fun DebugThemeSection(debug: DebugState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        FilledTonalButton(
            onClick = { debug.darkTheme.value = true },
            modifier = Modifier.weight(1f).heightIn(min = 42.dp),
        ) {
            Text(text = "Dark")
        }
        FilledTonalButton(
            onClick = { debug.darkTheme.value = false },
            modifier = Modifier.weight(1f).heightIn(min = 42.dp),
        ) {
            Text(text = "Light")
        }
    }
}

private const val DEBUG_LOG_LABEL_ALPHA = 0.4f
private const val DEBUG_LOG_CLEAR_ALPHA = 0.5f
private const val DEBUG_LOG_ENTRY_ALPHA = 0.6f
private const val OVERRIDE_SPEED_SLOW = 0.5f
private const val OVERRIDE_SPEED_NORMAL = 1f
private const val OVERRIDE_SPEED_FAST = 2f
private const val OVERRIDE_SCALE_SMALL = 0.8f
private const val OVERRIDE_SCALE_NORMAL = 1f
private const val OVERRIDE_SCALE_LARGE = 1.3f
internal const val TOAST_SPAM_REPETITIONS = 20
internal const val SAVE_SCAN_REPETITIONS = 3
private const val SNAPSHOT_PREVIEW_LENGTH = 120
