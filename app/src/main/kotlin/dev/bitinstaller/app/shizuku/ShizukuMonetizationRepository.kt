package dev.bitinstaller.app.shizuku

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.IOException

private const val BITLIFE_PACKAGE_NAME: String = "com.candywriter.bitlife"
private const val MONETIZATION_VARS_FILE_NAME: String = "MonetizationVars"
private const val LIVE_DICTIONARY_DIRECTORY_NAME: String = "LiveDictionary"

private val bitLifeFilesDirectory: String = "/storage/emulated/0/Android/data/$BITLIFE_PACKAGE_NAME/files"
private val liveDictionaryDirectory: String = "$bitLifeFilesDirectory/$LIVE_DICTIONARY_DIRECTORY_NAME"
val bitLifeMonetizationVarsPaths: List<String> = listOf(
    "$bitLifeFilesDirectory/$MONETIZATION_VARS_FILE_NAME",
)

enum class ShizukuAccessStatus {
    UNAVAILABLE,
    PERMISSION_REQUIRED,
    READY,
}

data class ShizukuSnapshot(
    val status: ShizukuAccessStatus,
    val uid: Int?,
)

data class MonetizationVarsFile(
    val path: String,
    val content: String,
)

data class MonetizationVarsWriteResult(
    val path: String,
    val backupPath: String,
)

enum class LiveDictionaryStatus {
    DIRECTORY,
    MISSING,
    NOT_DIRECTORY,
}

data class LiveDictionaryState(
    val status: LiveDictionaryStatus,
)

enum class LiveDictionaryRepairAction {
    READY,
    CREATED,
    REPLACED,
}

data class LiveDictionaryRepairResult(
    val action: LiveDictionaryRepairAction,
    val backupPath: String?,
)

class ShizukuMonetizationRepository {
    fun snapshot(): ShizukuSnapshot {
        if (!isBinderAlive()) {
            return ShizukuSnapshot(status = ShizukuAccessStatus.UNAVAILABLE, uid = null)
        }

        val hasPermission =
            runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }
                .getOrDefault(false)

        return if (hasPermission) {
            ShizukuSnapshot(
                status = ShizukuAccessStatus.READY,
                uid = runCatching { Shizuku.getUid() }.getOrNull(),
            )
        } else {
            ShizukuSnapshot(status = ShizukuAccessStatus.PERMISSION_REQUIRED, uid = null)
        }
    }

    suspend fun readMonetizationVars(): MonetizationVarsFile {
        requireReady()
        requireLiveDictionaryDirectory()

        val failures = mutableListOf<String>()
        bitLifeMonetizationVarsPaths.forEach { path ->
            val result = runShell(command = "cat ${shellQuote(path)}")
            if (result.isSuccess) {
                return MonetizationVarsFile(path = path, content = result.output)
            }
            failures += "$path: ${result.errorSummary()}"
        }

        throw IOException(
            buildString {
                append("Could not read BitLife MonetizationVars. ")
                append("Open BitLife once, then make sure Shizuku has storage access for Android/data. ")
                append(failures.joinToString(separator = " | "))
            },
        )
    }

    suspend fun writeMonetizationVars(
        path: String,
        content: String,
    ): MonetizationVarsWriteResult {
        requireReady()
        requireLiveDictionaryDirectory()

        val backupPath = "$path.bitinstaller.bak"
        val command =
            "cp -f ${shellQuote(path)} ${shellQuote(backupPath)} && cat > ${shellQuote(path)}"
        val result = runShell(command = command, stdin = content)

        if (!result.isSuccess) {
            throw IOException("Could not write MonetizationVars: ${result.errorSummary()}")
        }

        return MonetizationVarsWriteResult(path = path, backupPath = backupPath)
    }

    suspend fun liveDictionaryState(): LiveDictionaryState {
        requireReady()
        val result = runShell(command = liveDictionaryStateCommand())
        if (!result.isSuccess) {
            throw IOException("Could not check BitLife LiveDictionary: ${result.errorSummary()}")
        }

        val status = when (result.output.trim()) {
            "directory" -> LiveDictionaryStatus.DIRECTORY
            "not_directory" -> LiveDictionaryStatus.NOT_DIRECTORY
            else -> LiveDictionaryStatus.MISSING
        }

        return LiveDictionaryState(status = status)
    }

    suspend fun replaceLiveDictionary(): LiveDictionaryRepairResult {
        requireReady()
        val backupPath = "$liveDictionaryDirectory.bitinstaller.bak"
        val result = runShell(command = replaceLiveDictionaryCommand(backupPath = backupPath))
        if (!result.isSuccess) {
            throw IOException("Could not prepare BitLife LiveDictionary: ${result.errorSummary()}")
        }

        return when (result.output.trim()) {
            "ready" -> LiveDictionaryRepairResult(action = LiveDictionaryRepairAction.READY, backupPath = null)
            "replaced" -> LiveDictionaryRepairResult(
                action = LiveDictionaryRepairAction.REPLACED,
                backupPath = backupPath,
            )
            else -> LiveDictionaryRepairResult(action = LiveDictionaryRepairAction.CREATED, backupPath = null)
        }
    }

    private suspend fun requireLiveDictionaryDirectory() {
        val state = liveDictionaryState()
        val failureMessage = when (state.status) {
            LiveDictionaryStatus.DIRECTORY -> null
            LiveDictionaryStatus.NOT_DIRECTORY -> {
                "BitLife LiveDictionary exists but is not a folder. Fix LiveDictionary before saving, " +
                    "or BitLife can reset MonetizationVars back to defaults."
            }
            LiveDictionaryStatus.MISSING -> {
                "BitLife LiveDictionary folder is missing. Open BitLife once and make sure " +
                    "LiveDictionary exists before patching, or MonetizationVars can reset to defaults."
            }
        }

        if (failureMessage != null) {
            throw IOException(failureMessage)
        }
    }

    private fun requireReady() {
        val snapshot = snapshot()
        check(snapshot.status == ShizukuAccessStatus.READY) {
            "Shizuku is not ready. Current status: ${snapshot.status.name.lowercase()}"
        }
    }
}

private fun liveDictionaryStateCommand(): String =
    buildString {
        append("if [ -d ${shellQuote(liveDictionaryDirectory)} ]; then printf directory; ")
        append("elif [ -e ${shellQuote(liveDictionaryDirectory)} ]; then printf not_directory; ")
        append("else printf missing; fi")
    }

private fun replaceLiveDictionaryCommand(backupPath: String): String =
    buildString {
        append("if [ -d ${shellQuote(liveDictionaryDirectory)} ]; then printf ready; ")
        append("elif [ -e ${shellQuote(liveDictionaryDirectory)} ]; then ")
        append("rm -rf ${shellQuote(backupPath)} && ")
        append("mv -f ${shellQuote(liveDictionaryDirectory)} ${shellQuote(backupPath)} && ")
        append("mkdir -p ${shellQuote(liveDictionaryDirectory)} && printf replaced; ")
        append("else mkdir -p ${shellQuote(liveDictionaryDirectory)} && printf created; fi")
    }

private data class ShellResult(
    val exitCode: Int,
    val output: String,
    val error: String,
) {
    val isSuccess: Boolean = exitCode == 0

    fun errorSummary(): String =
        error.trim().ifEmpty {
            "exit $exitCode"
        }
}

private fun isBinderAlive(): Boolean =
    runCatching { Shizuku.pingBinder() }.getOrDefault(false)

private suspend fun runShell(
    command: String,
    stdin: String? = null,
): ShellResult =
    withContext(Dispatchers.IO) {
        val process = newShizukuShellProcess(command)
        if (stdin == null) {
            process.outputStream.close()
        } else {
            process.outputStream.bufferedWriter().use { writer ->
                writer.write(stdin)
            }
        }

        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        ShellResult(exitCode = process.waitFor(), output = output, error = error)
    }

private fun shellQuote(value: String): String =
    "'${value.replace("'", "'\"'\"'")}'"

private fun newShizukuShellProcess(command: String): ShizukuRemoteProcess {
    val newProcess = Shizuku::class.java.getDeclaredMethod(
        "newProcess",
        Array<String>::class.java,
        Array<String>::class.java,
        String::class.java,
    )
    newProcess.isAccessible = true
    return newProcess.invoke(null, arrayOf("sh", "-c", command), null, null) as ShizukuRemoteProcess
}
