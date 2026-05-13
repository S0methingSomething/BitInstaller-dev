package dev.bitinstaller.app

import dev.bitinstaller.app.home.SaveTargetUiState
import dev.bitinstaller.app.save.BitLifeSaveParser
import dev.bitinstaller.app.shizuku.OperationLock
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.listLifeSaveFiles
import dev.bitinstaller.app.shizuku.readLifeSaveFile
import dev.bitinstaller.app.targets.findTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun CoroutineScope.launchSaveScan(
    target: SaveTargetUiState,
    repository: ShizukuMonetizationRepository,
    operationLock: OperationLock,
    appState: BitInstallerAppState,
) {
    if (!operationLock.tryAcquire()) return
    launch {
        try {
            appState.scanSaveFiles(target = target, repository = repository)
        } finally {
            operationLock.release()
        }
    }
}

private suspend fun BitInstallerAppState.scanSaveFiles(
    target: SaveTargetUiState,
    repository: ShizukuMonetizationRepository,
) {
    saveScanTargetId = target.packageName
    saveScanErrors = saveScanErrors - target.packageName
    saveScanResults = saveScanResults - target.packageName
    runCatching {
        withContext(Dispatchers.IO) {
            val patchTarget =
                findTarget(target.packageName)
                    ?: error("Unknown target: ${target.packageName}")
            repository.listLifeSaveFiles(patchTarget, patchTarget.filesDirectory).map { file ->
                runCatching {
                    val bytes = repository.readLifeSaveFile(file)
                    BitLifeSaveParser.parse(path = file.path, bytes = bytes)
                }.getOrElse { error ->
                    BitLifeSaveParser.failure(path = file.path, sizeBytes = file.sizeBytes.toIntSize(), error = error)
                }
            }
        }
    }.onSuccess { summaries ->
        saveScanResults = saveScanResults + (target.packageName to summaries)
    }.onFailure { error ->
        saveScanErrors = saveScanErrors + (target.packageName to (error.message ?: "Could not scan saves"))
    }
    saveScanTargetId = null
}

private fun Long.toIntSize(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
