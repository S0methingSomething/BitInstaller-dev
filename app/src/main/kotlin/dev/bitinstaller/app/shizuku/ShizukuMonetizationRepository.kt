package dev.bitinstaller.app.shizuku

import android.content.pm.PackageManager
import android.util.Log
import dev.bitinstaller.app.targets.PatchTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.IOException

private const val TAG = "ShizukuRepo"

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

    /**
     * Probe current Shizuku binder and permission state.
     *
     * Performs Binder IPC ([Shizuku.checkSelfPermission], [Shizuku.getUid]),
     * so callers on Main should wrap this in a coroutine on [Dispatchers.IO].
     */
    fun checkStatus(): ShizukuSnapshot {
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

    suspend fun readMonetizationVars(target: PatchTarget): MonetizationVarsFile {
        requireReady()
        requireLiveDictionaryDirectory(target)

        val path = target.monetizationVarsPath
        val result = runShell(command = "cat ${shellQuote(path)}")
        if (result.isSuccess) {
            return MonetizationVarsFile(path = path, content = result.output)
        }

        throw IOException(
            buildString {
                append("Could not read ${target.displayName} MonetizationVars. ")
                append("Open ${target.displayName} once, then ensure Shizuku can access Android/data. ")
                append("$path: ${result.errorSummary()}")
            },
        )
    }

    suspend fun writeMonetizationVars(
        path: String,
        content: String,
    ): MonetizationVarsWriteResult {
        requireReady()

        val backupPath = "$path.bitinstaller.bak"
        val command =
            "cp -f ${shellQuote(path)} ${shellQuote(backupPath)} && cat > ${shellQuote(path)}"
        val result = runShell(command = command, stdin = content)

        if (!result.isSuccess) {
            throw IOException("Could not write MonetizationVars: ${result.errorSummary()}")
        }

        return MonetizationVarsWriteResult(path = path, backupPath = backupPath)
    }

    suspend fun liveDictionaryState(target: PatchTarget): LiveDictionaryState {
        requireReady()
        val result = runShell(command = liveDictionaryStateCommand(target))
        if (!result.isSuccess) {
            throw IOException("Could not check ${target.displayName} LiveDictionary: ${result.errorSummary()}")
        }

        val status = when (result.output.trim()) {
            "directory" -> LiveDictionaryStatus.DIRECTORY
            "not_directory" -> LiveDictionaryStatus.NOT_DIRECTORY
            else -> LiveDictionaryStatus.MISSING
        }

        return LiveDictionaryState(status = status)
    }

    suspend fun replaceLiveDictionary(target: PatchTarget): LiveDictionaryRepairResult {
        requireReady()
        val backupPath = "${target.liveDictionaryPath}.bitinstaller.bak"
        val result = runShell(command = replaceLiveDictionaryCommand(target, backupPath))
        if (!result.isSuccess) {
            throw IOException("Could not prepare ${target.displayName} LiveDictionary: ${result.errorSummary()}")
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

    /**
     * Read the BitInstaller patch manifest from the target app's external storage.
     * Returns null (with a warning log) if the manifest does not exist or is unreadable.
     */
    suspend fun readManifest(target: PatchTarget): String? {
        requireReady()
        val result = runShell(command = "cat ${shellQuote(target.manifestPath)}")
        if (!result.isSuccess) {
            Log.w(TAG, "Manifest unreadable for ${target.packageName}: ${result.errorSummary()}")
        }
        return if (result.isSuccess) result.output else null
    }

    suspend fun writeManifest(target: PatchTarget, content: String) {
        requireReady()
        val result = runShell(command = "cat > ${shellQuote(target.manifestPath)}", stdin = content)
        if (!result.isSuccess) {
            throw IOException("Could not write manifest: ${result.errorSummary()}")
        }
    }

    private suspend fun requireLiveDictionaryDirectory(target: PatchTarget) {
        val state = liveDictionaryState(target)
        val failureMessage = when (state.status) {
            LiveDictionaryStatus.DIRECTORY -> null
            LiveDictionaryStatus.NOT_DIRECTORY -> {
                "${target.displayName} LiveDictionary exists but is not a folder. Fix LiveDictionary before saving, " +
                    "or ${target.displayName} can reset MonetizationVars back to defaults."
            }
            LiveDictionaryStatus.MISSING -> {
                "${target.displayName} LiveDictionary folder is missing. Open ${target.displayName} once and make " +
                    "sure LiveDictionary exists before patching, or MonetizationVars can reset to defaults."
            }
        }

        if (failureMessage != null) {
            throw IOException(failureMessage)
        }
    }

    /**
     * Assert Shizuku is [ShizukuAccessStatus.READY].
     *
     * Performs Binder IPC via [checkStatus], so all call sites must already
     * be on [Dispatchers.IO] (enforced by being private to suspend functions
     * that dispatch to IO in [runShell]).
     */
    private fun requireReady() {
        val current = checkStatus()
        check(current.status == ShizukuAccessStatus.READY) {
            "Shizuku is not ready. Current status: ${current.status.name.lowercase()}"
        }
    }
}

private fun liveDictionaryStateCommand(target: PatchTarget): String =
    buildString {
        append("if [ -d ${shellQuote(target.liveDictionaryPath)} ]; then printf directory; ")
        append("elif [ -e ${shellQuote(target.liveDictionaryPath)} ]; then printf not_directory; ")
        append("else printf missing; fi")
    }

private fun replaceLiveDictionaryCommand(target: PatchTarget, backupPath: String): String =
    buildString {
        append("if [ -d ${shellQuote(target.liveDictionaryPath)} ]; then printf ready; ")
        append("elif [ -e ${shellQuote(target.liveDictionaryPath)} ]; then ")
        append("rm -rf ${shellQuote(backupPath)} && ")
        append("mv -f ${shellQuote(target.liveDictionaryPath)} ${shellQuote(backupPath)} && ")
        append("mkdir -p ${shellQuote(target.liveDictionaryPath)} && printf replaced; ")
        append("else mkdir -p ${shellQuote(target.liveDictionaryPath)} && printf created; fi")
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

/**
 * Execute a shell command via Shizuku with concurrent stdout/stderr capture.
 *
 * Reads stdout and stderr in separate threads to prevent pipe deadlock —
 * if stderr's OS buffer fills while we block on stdout.read(), the child
 * process would stall writing to stderr, and we'd never finish reading
 * stdout.
 */
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

        // Read stderr on a background thread to avoid pipe deadlock.
        val errorFuture = java.util.concurrent.CompletableFuture.supplyAsync {
            process.errorStream.bufferedReader().readText()
        }
        val output = process.inputStream.bufferedReader().readText()
        val error = errorFuture.get()
        ShellResult(exitCode = process.waitFor(), output = output, error = error)
    }

private fun shellQuote(value: String): String =
    "'${value.replace("'", "'\"'\"'")}'"

/**
 * Launch a shell via Shizuku's internal `newProcess` method.
 *
 * The public Shizuku API does not expose a stable process-creation method —
 * `Shizuku.newProcess` is `@hide`. We use reflection to reach it because no
 * supported alternative exists for running arbitrary shell commands with
 * Shizuku-elevated privileges. If the Shizuku internals change, this will
 * throw [NoSuchMethodException] at call time rather than silently failing.
 */
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
