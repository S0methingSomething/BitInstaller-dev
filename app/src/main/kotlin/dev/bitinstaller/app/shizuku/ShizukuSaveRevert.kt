package dev.bitinstaller.app.shizuku

import java.io.IOException

private const val REVERT_BYTES_PER_MIB: Long = 1024L * 1024L
private const val MAX_REVERT_LIFE_SAVE_BYTES: Long = 8L * REVERT_BYTES_PER_MIB
private const val MAX_REVERT_LIFE_SAVE_READ_BYTES: Long = MAX_REVERT_LIFE_SAVE_BYTES + 1L

suspend fun ShizukuMonetizationRepository.revertLifeSaveFile(path: String): ByteArray {
    val current = checkStatus()
    check(current.status == ShizukuAccessStatus.READY) {
        "Shizuku is not ready. Current status: ${current.status.name.lowercase()}"
    }

    val backupPath = "$path.bitinstaller.bak"
    requireBackupExists(path = path, backupPath = backupPath)
    restoreBackup(path = path, backupPath = backupPath)
    return readRestoredLifeSaveFile(path)
}

private suspend fun ShizukuMonetizationRepository.requireBackupExists(
    path: String,
    backupPath: String,
) {
    val result = runShellBytes(command = "[ -f ${shellQuote(backupPath)} ] && echo exists")
    if (result.output.decodeToString().trim() != "exists") {
        throw IOException("No backup found for ${path.substringAfterLast('/')}")
    }
}

private suspend fun ShizukuMonetizationRepository.restoreBackup(
    path: String,
    backupPath: String,
) {
    val result = runShellBytes(command = "cp -p ${shellQuote(backupPath)} ${shellQuote(path)} && echo ok")
    if (result.output.decodeToString().trim() != "ok") {
        throw IOException("Could not restore backup: ${result.errorSummary()}")
    }
}

private suspend fun ShizukuMonetizationRepository.readRestoredLifeSaveFile(path: String): ByteArray {
    val result = runShellBytes(command = "head -c $MAX_REVERT_LIFE_SAVE_READ_BYTES ${shellQuote(path)}")
    if (!result.isSuccess) {
        throw IOException("Restored but could not re-read: ${result.errorSummary()}")
    }
    if (result.output.size > MAX_REVERT_LIFE_SAVE_BYTES) {
        throw IOException("Restored save is too large to preview safely (${result.output.size} bytes)")
    }
    return result.output
}
