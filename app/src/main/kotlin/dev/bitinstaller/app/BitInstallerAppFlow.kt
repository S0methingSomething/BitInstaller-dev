package dev.bitinstaller.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bitinstaller.app.crypto.MonetizationCodec
import dev.bitinstaller.app.crypto.MonetizationData
import dev.bitinstaller.app.home.HomeRouteCallbacks
import dev.bitinstaller.app.home.LiveDictionaryPromptUiState
import dev.bitinstaller.app.home.PatchEditorSession
import dev.bitinstaller.app.home.PatchManifestPresence
import dev.bitinstaller.app.home.PatchManifestStore
import dev.bitinstaller.app.home.PatchPresenceState
import dev.bitinstaller.app.home.PatchTargetUiState
import dev.bitinstaller.app.shizuku.LiveDictionaryStatus
import dev.bitinstaller.app.shizuku.MonetizationVarsFile
import dev.bitinstaller.app.shizuku.ShizukuAccessStatus
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.ShizukuSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

internal const val SHIZUKU_PERMISSION_REQUEST_CODE: Int = 6207

internal class BitInstallerAppState(initialSnapshot: ShizukuSnapshot) {
    var snapshot by mutableStateOf(initialSnapshot)
    var activeSession by mutableStateOf<PatchEditorSession?>(null)
    var isLoading by mutableStateOf(false)
    var loadError by mutableStateOf<String?>(null)
    var patchPresence by mutableStateOf(
        PatchManifestPresence(state = PatchPresenceState.NOT_PATCHED, label = "Not patched"),
    )
    var pendingLiveDictionaryTarget by mutableStateOf<PatchTargetUiState?>(null)
    var liveDictionaryPrompt by mutableStateOf<LiveDictionaryPromptUiState?>(null)
}

internal fun buildHomeRouteCallbacks(
    context: Context,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    coroutineScope: CoroutineScope,
    appState: BitInstallerAppState,
): HomeRouteCallbacks =
    HomeRouteCallbacks(
        onDashboardActionClick = {
            handleDashboardAction(context = context, repository = repository, appState = appState)
        },
        onPatchClick = { target ->
            coroutineScope.launchPatchSession(target, repository, manifestStore, appState)
        },
        onDismissSession = { appState.activeSession = null },
        onDismissLiveDictionaryPrompt = {
            appState.liveDictionaryPrompt = null
            appState.pendingLiveDictionaryTarget = null
        },
        onConfirmLiveDictionaryFix = {
            coroutineScope.launchLiveDictionaryFix(repository, manifestStore, appState)
        },
        onSaveSession = { session, data ->
            savePatchSession(session, data, repository, manifestStore, appState)
        },
    )

private fun handleDashboardAction(
    context: Context,
    repository: ShizukuMonetizationRepository,
    appState: BitInstallerAppState,
) {
    if (appState.snapshot.status == ShizukuAccessStatus.READY) {
        openShizukuApp(context = context, onError = { error -> appState.loadError = error })
    } else {
        requestShizukuPermission(
            refreshSnapshot = { appState.snapshot = repository.snapshot() },
            onError = { error -> appState.loadError = error },
        )
    }
}

private fun CoroutineScope.launchPatchSession(
    target: PatchTargetUiState,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    appState: BitInstallerAppState,
) {
    launch {
        appState.loadSession(
            target = target,
            repository = repository,
            manifestStore = manifestStore,
        )
    }
}

private fun CoroutineScope.launchLiveDictionaryFix(
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    appState: BitInstallerAppState,
) {
    val target = appState.pendingLiveDictionaryTarget ?: return
    launch {
        appState.isLoading = true
        appState.loadError = null
        appState.liveDictionaryPrompt = null
        runCatching {
            repository.replaceLiveDictionary()
            appState.loadSession(
                target = target,
                repository = repository,
                manifestStore = manifestStore,
            )
        }.onFailure { error -> appState.loadError = error.message }
        appState.pendingLiveDictionaryTarget = null
        appState.isLoading = false
    }
}

private suspend fun BitInstallerAppState.loadSession(
    target: PatchTargetUiState,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
) {
    isLoading = true
    loadError = null
    runCatching {
        loadPatchSession(
            target = target,
            repository = repository,
            manifestStore = manifestStore,
            onPatchPresenceChanged = { patchPresence = it },
            onLiveDictionaryIssue = { prompt ->
                pendingLiveDictionaryTarget = target
                liveDictionaryPrompt = prompt
            },
        )
    }
        .onSuccess { session -> activeSession = session }
        .onFailure { error -> loadError = error.message }
    isLoading = false
}

private suspend fun savePatchSession(
    session: PatchEditorSession,
    data: MonetizationData,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    appState: BitInstallerAppState,
): String {
    val encrypted = MonetizationCodec.encrypt(data)
    val writeResult = repository.writeMonetizationVars(path = session.filePath, content = encrypted)
    manifestStore.recordPatched(
        packageName = session.target.packageName,
        path = session.filePath,
        encryptedContent = encrypted,
    )
    appState.patchPresence = PatchManifestPresence(state = PatchPresenceState.PATCHED, label = "Patched")
    return "Saved to BitLife. Backup: ${writeResult.backupPath.substringAfterLast('/')}"
}

private suspend fun loadPatchSession(
    target: PatchTargetUiState,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    onPatchPresenceChanged: (PatchManifestPresence) -> Unit,
    onLiveDictionaryIssue: (LiveDictionaryPromptUiState) -> Unit,
): PatchEditorSession? {
    val liveDictionaryState = repository.liveDictionaryState()
    if (liveDictionaryState.status != LiveDictionaryStatus.DIRECTORY) {
        onLiveDictionaryIssue(liveDictionaryState.status.toPromptUiState())
        return null
    }

    val file = repository.readMonetizationVars()
    val presence = manifestStore.presenceFor(
        packageName = target.packageName,
        path = file.path,
        encryptedContent = file.content,
    )
    onPatchPresenceChanged(presence)
    return file.toPatchEditorSession(target = target, patchPresence = presence)
}

private fun LiveDictionaryStatus.toPromptUiState(): LiveDictionaryPromptUiState =
    when (this) {
        LiveDictionaryStatus.MISSING -> LiveDictionaryPromptUiState(
            title = "Create LiveDictionary?",
            message = "BitLife can reset MonetizationVars to defaults when LiveDictionary is missing. " +
                "Create the LiveDictionary folder before patching?",
            confirmLabel = "Create folder",
        )
        LiveDictionaryStatus.NOT_DIRECTORY -> LiveDictionaryPromptUiState(
            title = "Replace LiveDictionary?",
            message = "LiveDictionary exists but is not a folder. BitLife can reset MonetizationVars " +
                "to defaults in this state. Replace it with a folder and keep a .bitinstaller.bak backup?",
            confirmLabel = "Replace",
        )
        LiveDictionaryStatus.DIRECTORY -> LiveDictionaryPromptUiState(
            title = "LiveDictionary ready",
            message = "LiveDictionary is already a folder.",
            confirmLabel = "Continue",
        )
    }

internal fun requestShizukuPermission(
    refreshSnapshot: () -> Unit,
    onError: (String?) -> Unit,
) {
    runCatching {
        if (Shizuku.pingBinder()) {
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
        } else {
            onError("Start Shizuku first")
        }
    }.onFailure { error ->
        onError(error.message ?: "Could not request Shizuku permission")
    }
    refreshSnapshot()
}

private fun MonetizationVarsFile.toPatchEditorSession(
    target: PatchTargetUiState,
    patchPresence: PatchManifestPresence,
): PatchEditorSession =
    PatchEditorSession(
        target = target.copy(
            patchPresenceState = patchPresence.state,
            patchPresenceLabel = patchPresence.label,
            statusLabel = "Loaded from BitLife files",
        ),
        filePath = path,
        initialData = MonetizationCodec.decrypt(content),
    )
