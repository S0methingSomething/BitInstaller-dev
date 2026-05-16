package dev.bitinstaller.app

import android.content.Context
import dev.bitinstaller.app.home.SaveTargetUiState
import dev.bitinstaller.app.save.BitLifeSaveEditor
import dev.bitinstaller.app.save.BitLifeSaveParser
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_RECENT_EDIT_FIELDS = 20

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

internal fun CoroutineScope.launchSaveFieldEdit(
    request: SaveFieldEditRequest,
    repository: ShizukuMonetizationRepository,
    operationLock: OperationLock,
    appState: BitInstallerAppState,
) {
    if (!operationLock.tryAcquire()) return
    launch {
        try {
            appState.editSaveField(
                request = request,
                repository = repository,
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
) {
    if (!operationLock.tryAcquire()) return
    launch {
        try {
            appState.revertSaveFile(request = request, repository = repository)
        } finally {
            operationLock.release()
        }
    }
}

internal data class SaveFieldEditRequest(
    val context: Context,
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val field: SaveEditableField,
    val rawValue: String,
)

internal data class SaveRevertRequest(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
)

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

private suspend fun BitInstallerAppState.editSaveField(
    request: SaveFieldEditRequest,
    repository: ShizukuMonetizationRepository,
) {
    val save = request.save
    saveEditTargetPath = save.path
    saveEditErrors = saveEditErrors - save.path
    saveEditMessages = saveEditMessages - save.path
    runCatching {
        withContext(Dispatchers.IO) {
            val original =
                repository.readLifeSaveFile(
                    LifeSaveFile(path = save.path, sizeBytes = save.sizeBytes.toLong()),
                )
            val tempFile = File.createTempFile("bitinstaller-save-edit", ".data", request.context.cacheDir)
            try {
                val edited =
                    BitLifeSaveEditor.applyEdit(
                        bytes = original,
                        field = request.field,
                        rawValue = request.rawValue,
                        outputFile = tempFile,
                    )
                val writeResult = repository.writeLifeSaveFile(path = save.path, bytes = edited)
                val parsed = BitLifeSaveParser.parse(path = save.path, bytes = edited)
                parsed.copy(advancedFields = save.advancedFields) to writeResult.backupPath
            } finally {
                tempFile.delete()
            }
        }
    }.onSuccess { (updatedSave, backupPath) ->
        saveScanResults =
            saveScanResults +
            (request.target.packageName to saveScanResults[request.target.packageName].replaceSave(updatedSave))
        saveRecentEditFieldIds =
            saveRecentEditFieldIds + (save.path to saveRecentEditFieldIds[save.path].promote(request.field.id))
        saveEditMessages = saveEditMessages + (save.path to "Saved. Backup: ${backupPath.substringAfterLast('/')}")
    }.onFailure { error ->
        saveEditErrors = saveEditErrors + (save.path to (error.message ?: "Could not save edit"))
    }
    saveEditTargetPath = null
}

private fun Long.toIntSize(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

private suspend fun BitInstallerAppState.revertSaveFile(
    request: SaveRevertRequest,
    repository: ShizukuMonetizationRepository,
) {
    val save = request.save
    saveEditTargetPath = save.path
    saveEditErrors = saveEditErrors - save.path
    saveEditMessages = saveEditMessages - save.path
    runCatching {
        withContext(Dispatchers.IO) {
            val restored = repository.revertLifeSaveFile(save.path)
            BitLifeSaveParser.parse(path = save.path, bytes = restored)
        }
    }.onSuccess { updatedSave ->
        saveScanResults =
            saveScanResults +
            (request.target.packageName to saveScanResults[request.target.packageName].replaceSave(updatedSave))
        saveEditMessages = saveEditMessages + (save.path to "Reverted to backup")
    }.onFailure { error ->
        saveEditErrors = saveEditErrors + (save.path to (error.message ?: "Could not revert"))
    }
    saveEditTargetPath = null
}

private fun List<BitLifeSaveSummary>?.replaceSave(updatedSave: BitLifeSaveSummary): List<BitLifeSaveSummary> =
    orEmpty().map { save -> if (save.path == updatedSave.path) updatedSave else save }

private fun List<String>?.promote(fieldId: String): List<String> =
    listOf(fieldId).plus(orEmpty().filterNot { it == fieldId }).take(MAX_RECENT_EDIT_FIELDS)
