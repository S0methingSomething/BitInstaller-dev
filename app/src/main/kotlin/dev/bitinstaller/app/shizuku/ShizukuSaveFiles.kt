package dev.bitinstaller.app.shizuku

import dev.bitinstaller.app.targets.PatchTarget
import java.io.IOException

private const val MAX_LIFE_SAVE_COUNT: Int = 64
private const val BYTES_PER_MIB: Long = 1024L * 1024L
private const val MAX_LIFE_SAVE_BYTES: Long = 8L * BYTES_PER_MIB
private const val MAX_LIFE_SAVE_READ_BYTES: Long = MAX_LIFE_SAVE_BYTES + 1L

suspend fun ShizukuMonetizationRepository.listLifeSaveFiles(
    target: PatchTarget,
    filesDirectory: String,
): List<LifeSaveFile> {
    requireReadyForSaveFiles()
    val result = runShellBytes(command = listLifeSaveFilesCommand(filesDirectory))
    if (!result.isSuccess) {
        throw IOException("Could not list ${target.displayName} saves: ${result.errorSummary()}")
    }

    return result.output
        .decodeToString()
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map(::parseLifeSaveFileLine)
        .sortedBy { it.path }
        .take(MAX_LIFE_SAVE_COUNT)
        .toList()
}

suspend fun ShizukuMonetizationRepository.readLifeSaveFile(file: LifeSaveFile): ByteArray {
    requireReadyForSaveFiles()
    file.requireSafePreviewSize()

    val result = runShellBytes(command = "head -c $MAX_LIFE_SAVE_READ_BYTES ${shellQuote(file.path)}")
    result.requireLifeSaveSuccess(file)
    result.output.requireSafePreviewBytes()

    return result.output
}

suspend fun ShizukuMonetizationRepository.writeLifeSaveFile(
    path: String,
    bytes: ByteArray,
): LifeSaveWriteResult {
    requireReadyForSaveFiles()
    bytes.requireSafePreviewBytes()

    val backupPath = "$path.bitinstaller.bak"
    val tmpPath = "$path.bitinstaller.tmp"
    val result =
        runShellBytes(
            command = writeLifeSaveFileCommand(path = path, tmpPath = tmpPath, backupPath = backupPath),
            stdin = bytes,
        )
    if (!result.isSuccess) {
        throw IOException("Could not write ${path.substringAfterLast('/')}: ${result.errorSummary()}")
    }

    return LifeSaveWriteResult(path = path, backupPath = backupPath)
}

private fun ShizukuMonetizationRepository.requireReadyForSaveFiles() {
    val current = checkStatus()
    check(current.status == ShizukuAccessStatus.READY) {
        "Shizuku is not ready. Current status: ${current.status.name.lowercase()}"
    }
}

private fun listLifeSaveFilesCommand(filesDirectory: String): String =
    buildString {
        append("dir=${shellQuote(filesDirectory)}; ")
        append("if [ -d \"\$dir\" ]; then ")
        append("for slot in \"\$dir\"/sg*; do ")
        append("[ -d \"\$slot\" ] || continue; ")
        append("file=\"\$slot/savedLife.data\"; ")
        append("[ -f \"\$file\" ] || continue; ")
        append("size=$(wc -c < \"\$file\" 2>/dev/null) || continue; ")
        append("printf '%s\\t%s\\n' \"\$size\" \"\$file\"; ")
        append("done; ")
        append("fi")
    }

private fun writeLifeSaveFileCommand(
    path: String,
    tmpPath: String,
    backupPath: String,
): String =
    buildString {
        append("path=${shellQuote(path)}; ")
        append("tmp=${shellQuote(tmpPath)}; ")
        append("bak=${shellQuote(backupPath)}; ")
        append("cp -p \"\$path\" \"\$tmp\" && ")
        append("cat > \"\$tmp\" && ")
        append("cp -p \"\$path\" \"\$bak\" && ")
        append("mv \"\$tmp\" \"\$path\"; ")
        append("status=\$?; ")
        append("if [ \$status -ne 0 ]; then rm -f \"\$tmp\"; fi; ")
        append("exit \$status")
    }

private fun parseLifeSaveFileLine(line: String): LifeSaveFile {
    val size = line.substringBefore('\t').toLongOrNull() ?: 0L
    val path = line.substringAfter('\t', line)
    return LifeSaveFile(path = path, sizeBytes = size)
}

private fun LifeSaveFile.requireSafePreviewSize() {
    if (sizeBytes > MAX_LIFE_SAVE_BYTES) {
        throw IOException("Save is too large to preview safely ($sizeBytes bytes)")
    }
}

private fun ShellBytesResult.requireLifeSaveSuccess(file: LifeSaveFile) {
    if (!isSuccess) {
        throw IOException("Could not read ${file.path.substringAfterLast('/')}: ${errorSummary()}")
    }
}

private fun ByteArray.requireSafePreviewBytes() {
    if (size > MAX_LIFE_SAVE_BYTES) {
        throw IOException("Save grew beyond the safe preview limit ($size bytes)")
    }
}
