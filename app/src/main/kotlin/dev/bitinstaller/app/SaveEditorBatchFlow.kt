package dev.bitinstaller.app

import android.content.Context
import dev.bitinstaller.app.home.SaveTargetUiState
import dev.bitinstaller.app.save.BitLifeSaveEditor
import dev.bitinstaller.app.save.BitLifeSaveParser
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveFieldEdit
import dev.bitinstaller.app.save.SaveScanCache
import dev.bitinstaller.app.shizuku.LifeSaveFile
import dev.bitinstaller.app.shizuku.OperationLock
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.readLifeSaveFile
import dev.bitinstaller.app.shizuku.writeLifeSaveFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_RECENT_BATCH_EDIT_FIELDS = 20

internal fun CoroutineScope.launchSaveFieldEdits(
    request: SaveFieldEditBatchRequest,
    repository: ShizukuMonetizationRepository,
    operationLock: OperationLock,
    appState: BitInstallerAppState,
    saveCache: SaveScanCache,
) {
    if (request.edits.isEmpty()) return
    if (!operationLock.tryAcquire()) {
        appState.showBusyNotice(appState.busyMessageForSaveEdit(request.save.heroName))
        return
    }
    launch {
        try {
            appState.editSaveFields(request = request, repository = repository, saveCache = saveCache)
        } finally {
            operationLock.release()
        }
    }
}

internal data class SaveFieldEditBatchRequest(
    val context: Context,
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val edits: List<SaveFieldEdit>,
)

private suspend fun BitInstallerAppState.editSaveFields(
    request: SaveFieldEditBatchRequest,
    repository: ShizukuMonetizationRepository,
    saveCache: SaveScanCache,
) {
    val save = request.save
    saveEditTargetPath = save.path
    saveEditErrors = saveEditErrors - save.path
    saveEditMessages = saveEditMessages - save.path
    runCatching {
        withContext(Dispatchers.IO) { request.applyBatchEdit(repository = repository) }
    }.onSuccess { (updatedSave, backupPath) ->
        saveScanResults =
            saveScanResults +
            (request.target.packageName to saveScanResults[request.target.packageName].replaceBatchSave(updatedSave))
        saveRecentEditFieldIds =
            saveRecentEditFieldIds +
            (
                save.path to
                    saveRecentEditFieldIds[save.path].promoteBatchEdits(request.edits.map { edit -> edit.field.id })
            )
        saveEditMessages =
            saveEditMessages +
            (save.path to "Saved ${request.edits.size} changes. Backup: ${backupPath.substringAfterLast('/')}")
        saveEditMessageTokens = saveEditMessageTokens.incrementBatchToken(save.path)
        saveScanResults[request.target.packageName]?.let { saves -> saveCache.write(request.target.packageName, saves) }
    }.onFailure { error ->
        saveEditErrors = saveEditErrors + (save.path to (error.message ?: "Could not save edits"))
    }
    saveEditTargetPath = null
}

private suspend fun SaveFieldEditBatchRequest.applyBatchEdit(
    repository: ShizukuMonetizationRepository,
): Pair<BitLifeSaveSummary, String> {
    val original = repository.readLifeSaveFile(LifeSaveFile(path = save.path, sizeBytes = save.sizeBytes.toLong()))
    val tempFile = File.createTempFile("bitinstaller-save-edit", ".data", context.cacheDir)
    return try {
        val edited = BitLifeSaveEditor.applyEdits(bytes = original, edits = edits, outputFile = tempFile)
        val writeResult = repository.writeLifeSaveFile(path = save.path, bytes = edited)
        val parsed = BitLifeSaveParser.parse(path = save.path, bytes = edited, collectAdvancedFields = false)
        parsed.copy(advancedFields = save.advancedFields) to writeResult.backupPath
    } finally {
        tempFile.delete()
    }
}

private fun List<BitLifeSaveSummary>?.replaceBatchSave(updatedSave: BitLifeSaveSummary): List<BitLifeSaveSummary> =
    orEmpty().map { save -> if (save.path == updatedSave.path) updatedSave else save }

private fun List<String>?.promoteBatchEdits(fieldIds: List<String>): List<String> =
    fieldIds.plus(orEmpty().filterNot { it in fieldIds }).take(MAX_RECENT_BATCH_EDIT_FIELDS)

private fun Map<String, Int>.incrementBatchToken(path: String): Map<String, Int> =
    this + (path to ((this[path] ?: 0) + 1))
