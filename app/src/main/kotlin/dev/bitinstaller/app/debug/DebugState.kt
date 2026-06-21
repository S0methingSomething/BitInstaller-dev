package dev.bitinstaller.app.debug

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class DebugState {
    val visible = mutableStateOf(false)
    val eventLog = SnapshotStateList<String>()
    val isRecording = mutableStateOf(false)

    val animationSpeed = mutableFloatStateOf(1f)
    val darkTheme = mutableStateOf(true)
    val textScale = mutableFloatStateOf(1f)
    val recompositionCounter = mutableIntStateOf(0)
    val fps = mutableIntStateOf(0)
    val usedMemoryMb = mutableIntStateOf(0)
    val maxMemoryMb = mutableIntStateOf(0)
    val activeScenario = mutableStateOf<String?>(null)

    fun logEvent(message: String) {
        if (isRecording.value || visible.value) {
            eventLog.add(message)
            if (eventLog.size > MAX_EVENT_LOG_ENTRIES) {
                eventLog.removeAt(0)
            }
        }
    }

    fun startRecording() {
        isRecording.value = true
        eventLog.clear()
        logEvent("recording_started")
    }

    fun stopRecording() {
        logEvent("recording_stopped")
        isRecording.value = false
    }

    fun clearLog() {
        eventLog.clear()
    }

    companion object {
        private const val MAX_EVENT_LOG_ENTRIES = 200
    }
}
