package dev.bitinstaller.app.debug

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DebugScenarioRunner(
    private val debug: DebugState,
    private val scope: CoroutineScope,
) {
    private var activeJob: Job? = null

    fun start(
        label: String,
        action: suspend () -> Unit,
    ) {
        stop()
        debug.activeScenario.value = label
        debug.logEvent("scenario_started: $label")
        activeJob =
            scope.launch {
                action()
                if (isActive) {
                    debug.logEvent("scenario_finished: $label")
                    debug.activeScenario.value = null
                }
            }
    }

    fun stop() {
        activeJob?.cancel()
        activeJob = null
        debug.activeScenario.value?.let { debug.logEvent("scenario_cancelled: $it") }
        debug.activeScenario.value = null
    }

    fun activeLabel(): String? = debug.activeScenario.value
}

fun DebugScenarioRunner.navSwitchRapid(
    onMonetizationClick: () -> Unit,
    onSaveEditorClick: () -> Unit,
) {
    start("nav_switch_rapid") {
        repeat(NAV_SWITCH_REPETITIONS) {
            onMonetizationClick()
            delay(NAV_SWITCH_DELAY_MS)
            onSaveEditorClick()
            delay(NAV_SWITCH_DELAY_MS)
        }
    }
}

fun DebugScenarioRunner.toastSpam(showNotice: (String) -> Unit) {
    start("toast_spam") {
        repeat(TOAST_SPAM_REPETITIONS) {
            showNotice("Debug toast #$it · ${System.currentTimeMillis() % TOAST_SPAM_MODULUS}")
            delay(TOAST_SPAM_DELAY_MS)
        }
    }
}

fun DebugScenarioRunner.patchEditorOpenClose(
    onOpen: () -> Unit,
    onClose: () -> Unit,
) {
    start("patch_editor_stress") {
        repeat(PATCH_EDITOR_REPETITIONS) {
            onOpen()
            delay(PATCH_EDITOR_DELAY_MS)
            onClose()
            delay(PATCH_EDITOR_DELAY_MS)
        }
    }
}

fun DebugScenarioRunner.saveScanStress(onScan: suspend () -> Unit) {
    start("save_scan_stress") {
        repeat(SAVE_SCAN_REPETITIONS) {
            onScan()
            delay(SAVE_SCAN_DELAY_MS)
        }
    }
}

private const val NAV_SWITCH_REPETITIONS = 10
private const val NAV_SWITCH_DELAY_MS = 300L
private const val TOAST_SPAM_DELAY_MS = 200L
private const val TOAST_SPAM_MODULUS = 10000
private const val PATCH_EDITOR_REPETITIONS = 5
private const val PATCH_EDITOR_DELAY_MS = 600L
private const val SAVE_SCAN_DELAY_MS = 1200L
