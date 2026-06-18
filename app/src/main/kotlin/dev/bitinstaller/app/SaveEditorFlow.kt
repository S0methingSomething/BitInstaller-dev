package dev.bitinstaller.app

import androidx.compose.runtime.snapshots.Snapshot
import dev.bitinstaller.app.home.SaveTargetUiState
import dev.bitinstaller.app.save.BitLifeSaveParser
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveScanCache
import dev.bitinstaller.app.shizuku.LifeSaveFile
import dev.bitinstaller.app.shizuku.OperationLock
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.listLifeSaveFiles
import dev.bitinstaller.app.shizuku.readLifeSaveFile
import dev.bitinstaller.app.shizuku.revertLifeSaveFile
import dev.bitinstaller.app.shizuku.writeLifeSaveFile
import dev.bitinstaller.app.targets.findTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

private const val SCAN_CONCURRENCY = 4

internal fun CoroutineScope.launchSaveScan(
    target: SaveTargetUiState,
    repository: ShizukuMonetizationRepository,
    operationLock: OperationLock,
    appState: BitInstallerAppState,
    saveCache: SaveScanCache,
) {
    if (!operationLock.tryAcquire()) {
        appState.showBusyNotice(appState.busyMessageForSaveScan(target.name))
        return
    }
    launch {
        try {
            appState.scanSaveFiles(
                target = target,
                repository = repository,
                saveCache = saveCache,
            )
        } finally {
            operationLock.release()
        }
    }
}

internal fun CoroutineScope.launchSaveRevert(
    request: SaveRevertRequest,
    repository: ShizukuMonetizationRepository,
    operationLock: OperationLock,
    appState: BitInstallerAppState,
    saveCache: SaveScanCache,
) {
    if (!operationLock.tryAcquire()) {
        appState.showBusyNotice(appState.busyMessageForSaveEdit(request.save.heroName))
        return
    }
    launch {
        try {
            appState.revertSaveFile(request = request, repository = repository, saveCache = saveCache)
        } finally {
            operationLock.release()
        }
    }
}

internal data class SaveRevertRequest(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
)

private suspend fun BitInstallerAppState.scanSaveFiles(
    target: SaveTargetUiState,
    repository: ShizukuMonetizationRepository,
    saveCache: SaveScanCache,
) {
    Snapshot.withMutableSnapshot {
        saveScanTargetId = target.packageName
        saveScanErrors = saveScanErrors - target.packageName
    }
    val inMemory = saveScanResults[target.packageName]
    if (inMemory == null) {
        saveCache.read(target.packageName)?.let { diskSaves ->
            Snapshot.withMutableSnapshot {
                saveScanResults = saveScanResults + (target.packageName to diskSaves)
            }
        }
    }
    val cachedSaves =
        saveScanResults[target.packageName]
            .orEmpty()
            .associateBy { save -> save.path }
    val patchTarget =
        findTarget(target.packageName)
            ?: error("Unknown target: ${target.packageName}")
    runCatching {
        withContext(Dispatchers.IO) {
            val semaphore = Semaphore(SCAN_CONCURRENCY)
            coroutineScope {
                repository
                    .listLifeSaveFiles(patchTarget, patchTarget.filesDirectory)
                    .map { file ->
                        async {
                            val cached = cachedSaves[file.path]
                            if (cached != null && cached.sizeBytes.toLong() == file.sizeBytes) return@async cached
                            semaphore.withPermit {
                                runCatching {
                                    val bytes = repository.readLifeSaveFile(file)
                                    BitLifeSaveParser.parse(path = file.path, bytes = bytes)
                                }.getOrElse { error ->
                                    BitLifeSaveParser.failure(
                                        path = file.path,
                                        sizeBytes = file.sizeBytes.toIntSize(),
                                        error = error,
                                    )
                                }
                            }
                        }
                    }.awaitAll()
            }
        }
    }.onSuccess { summaries ->
        Snapshot.withMutableSnapshot {
            saveScanResults = saveScanResults + (target.packageName to summaries)
            saveScanTargetId = null
        }
        saveCache.write(target.packageName, summaries)
    }.onFailure { error ->
        Snapshot.withMutableSnapshot {
            saveScanErrors = saveScanErrors + (target.packageName to (error.message ?: "Could not scan saves"))
            saveScanTargetId = null
        }
    }
}

private fun Long.toIntSize(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

private suspend fun BitInstallerAppState.revertSaveFile(
    request: SaveRevertRequest,
    repository: ShizukuMonetizationRepository,
    saveCache: SaveScanCache,
) {
    val save = request.save
    Snapshot.withMutableSnapshot {
        saveEditTargetPath = save.path
        saveEditErrors = saveEditErrors - save.path
        saveEditMessages = saveEditMessages - save.path
    }
    runCatching {
        withContext(Dispatchers.IO) {
            val restored = repository.revertLifeSaveFile(save.path)
            BitLifeSaveParser
                .parse(path = save.path, bytes = restored, collectAdvancedFields = false)
                .copy(advancedFields = save.advancedFields)
        }
    }.onSuccess { updatedSave ->
        val updatedResults =
            saveScanResults +
                (request.target.packageName to saveScanResults[request.target.packageName].replaceSave(updatedSave))
        val updatedMessages = saveEditMessages + (save.path to "Reverted to backup")
        val updatedTokens = saveEditMessageTokens.increment(save.path)
        Snapshot.withMutableSnapshot {
            saveScanResults = updatedResults
            saveEditMessages = updatedMessages
            saveEditMessageTokens = updatedTokens
            saveEditTargetPath = null
        }
        updatedResults[request.target.packageName]?.let { saves -> saveCache.write(request.target.packageName, saves) }
    }.onFailure { error ->
        Snapshot.withMutableSnapshot {
            saveEditErrors = saveEditErrors + (save.path to (error.message ?: "Could not revert"))
            saveEditTargetPath = null
        }
    }
}

private fun List<BitLifeSaveSummary>?.replaceSave(updatedSave: BitLifeSaveSummary): List<BitLifeSaveSummary> =
    orEmpty().map { save -> if (save.path == updatedSave.path) updatedSave else save }

private fun Map<String, Int>.increment(path: String): Map<String, Int> = this + (path to ((this[path] ?: 0) + 1))
