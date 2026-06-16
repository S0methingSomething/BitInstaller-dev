package dev.bitinstaller.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bitinstaller.app.crypto.MonetizationCodec
import dev.bitinstaller.app.crypto.MonetizationData
import dev.bitinstaller.app.home.BitInstallerDestination
import dev.bitinstaller.app.home.HomeRouteCallbacks
import dev.bitinstaller.app.home.LiveDictionaryPromptUiState
import dev.bitinstaller.app.home.PATCH_PRESENCE_PATCHED_LABEL
import dev.bitinstaller.app.home.PatchEditorSession
import dev.bitinstaller.app.home.PatchManifestPresence
import dev.bitinstaller.app.home.PatchManifestStore
import dev.bitinstaller.app.home.PatchPresenceState
import dev.bitinstaller.app.home.PatchTargetUiState
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveScanCache
import dev.bitinstaller.app.shizuku.LiveDictionaryStatus
import dev.bitinstaller.app.shizuku.MonetizationVarsFile
import dev.bitinstaller.app.shizuku.OperationLock
import dev.bitinstaller.app.shizuku.ShizukuAccessStatus
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.ShizukuSnapshot
import dev.bitinstaller.app.targets.PatchTarget
import dev.bitinstaller.app.targets.findTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

internal const val SHIZUKU_PERMISSION_REQUEST_CODE: Int = 6207

internal class BitInstallerAppState(
    initialSnapshot: ShizukuSnapshot,
) {
    var snapshot by mutableStateOf(initialSnapshot)
    var activeSession by mutableStateOf<PatchEditorSession?>(null)
    var isLoading by mutableStateOf(false)
    var loadingTargetId by mutableStateOf<String?>(null)
    var loadError by mutableStateOf<String?>(null)
    var patchPresences by mutableStateOf(mapOf<String, PatchManifestPresence>())
    var pendingLiveDictionaryTarget by mutableStateOf<PatchTargetUiState?>(null)
    var liveDictionaryPrompt by mutableStateOf<LiveDictionaryPromptUiState?>(null)
    var selectedDestination by mutableStateOf(BitInstallerDestination.MonetizationVars)
    var selectedSaveTargetId by mutableStateOf<String?>(null)
    var saveScanTargetId by mutableStateOf<String?>(null)
    var saveEditTargetPath by mutableStateOf<String?>(null)
    var saveScanErrors by mutableStateOf(mapOf<String, String>())
    var saveEditErrors by mutableStateOf(mapOf<String, String>())
    var saveEditMessages by mutableStateOf(mapOf<String, String>())
    var saveEditMessageTokens by mutableStateOf(mapOf<String, Int>())
    var saveRecentEditFieldIds by mutableStateOf(mapOf<String, List<String>>())
    var saveScanResults by mutableStateOf(mapOf<String, List<BitLifeSaveSummary>>())
    var noticeMessage by mutableStateOf<String?>(null)
    var noticeToken by mutableIntStateOf(0)
}

internal class AppFlowDeps(
    val repository: ShizukuMonetizationRepository,
    val manifestStore: PatchManifestStore,
    val operationLock: OperationLock,
    val coroutineScope: CoroutineScope,
    val appState: BitInstallerAppState,
    val saveCache: SaveScanCache,
)

internal fun buildHomeRouteCallbacks(
    context: Context,
    deps: AppFlowDeps,
): HomeRouteCallbacks =
    with(deps) {
        HomeRouteCallbacks(
            onDestinationSelected = { destination -> appState.selectedDestination = destination },
            onDashboardActionClick = { handleDashboardAction(context = context, appState = appState) },
            onPatchClick = { target ->
                coroutineScope.launchPatchSession(target, repository, manifestStore, operationLock, appState)
            },
            onSaveTargetClick = { target ->
                appState.selectedSaveTargetId = target.packageName
                coroutineScope.launchSaveScan(target, repository, operationLock, appState, saveCache)
            },
            onSaveFieldEdits = { target, save, edits ->
                coroutineScope.launchSaveFieldEdits(
                    request = SaveFieldEditBatchRequest(context, target, save, edits),
                    repository = repository,
                    operationLock = operationLock,
                    appState = appState,
                    saveCache = saveCache,
                )
            },
            onSaveRevert = { target, save ->
                coroutineScope.launchSaveRevert(
                    request = SaveRevertRequest(target = target, save = save),
                    repository = repository,
                    operationLock = operationLock,
                    appState = appState,
                    saveCache = saveCache,
                )
            },
            onSaveEditorBack = { appState.selectedSaveTargetId = null },
            onDismissSession = { appState.activeSession = null },
            onDismissNotice = { appState.noticeMessage = null },
            onDismissLiveDictionaryPrompt = {
                appState.liveDictionaryPrompt = null
                appState.pendingLiveDictionaryTarget = null
            },
            onConfirmLiveDictionaryFix = {
                coroutineScope.launchLiveDictionaryFix(repository, manifestStore, operationLock, appState)
            },
            onSaveSession = { session, data ->
                savePatchSession(session, data, repository, manifestStore, appState)
            },
        )
    }

private fun handleDashboardAction(
    context: Context,
    appState: BitInstallerAppState,
) {
    when (appState.snapshot.status) {
        ShizukuAccessStatus.UNAVAILABLE,
        ShizukuAccessStatus.READY,
        -> {
            openShizukuApp(context = context, onError = { error -> appState.loadError = error })
        }

        ShizukuAccessStatus.PERMISSION_REQUIRED -> {
            requestShizukuPermission(
                onError = { error -> appState.loadError = error },
            )
        }
    }
}

/**
 * Launch a patch session for [target], guarded against concurrent operations.
 *
 * Uses [OperationLock] to prevent race conditions when tapping Patch
 * rapidly on multiple targets.
 */
private fun CoroutineScope.launchPatchSession(
    target: PatchTargetUiState,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    operationLock: OperationLock,
    appState: BitInstallerAppState,
) {
    if (!operationLock.tryAcquire()) {
        appState.showBusyNotice(appState.busyMessageForPatch(target.name))
        return
    }
    launch {
        try {
            appState.loadSession(
                target = target,
                repository = repository,
                manifestStore = manifestStore,
            )
        } finally {
            operationLock.release()
        }
    }
}

private fun CoroutineScope.launchLiveDictionaryFix(
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    operationLock: OperationLock,
    appState: BitInstallerAppState,
) {
    val target = appState.pendingLiveDictionaryTarget ?: return
    if (!operationLock.tryAcquire()) {
        appState.showBusyNotice("Patch setup is already running. Wait for it to finish before fixing LiveDictionary.")
        return
    }
    launch {
        try {
            appState.isLoading = true
            appState.loadingTargetId = target.packageName
            appState.loadError = null
            appState.liveDictionaryPrompt = null
            runCatching {
                val patchTarget =
                    findTarget(target.packageName)
                        ?: error("Unknown target: ${target.packageName}")
                repository.replaceLiveDictionary(patchTarget)
                appState.loadSession(
                    target = target,
                    repository = repository,
                    manifestStore = manifestStore,
                )
            }.onFailure { error -> appState.loadError = error.message }
            appState.pendingLiveDictionaryTarget = null
            appState.isLoading = false
            appState.loadingTargetId = null
        } finally {
            operationLock.release()
        }
    }
}

private suspend fun BitInstallerAppState.loadSession(
    target: PatchTargetUiState,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
) {
    isLoading = true
    loadingTargetId = target.packageName
    loadError = null
    runCatching {
        loadPatchSession(
            target = target,
            repository = repository,
            manifestStore = manifestStore,
            onPatchPresenceChanged = { pkg, presence ->
                patchPresences = patchPresences + (pkg to presence)
            },
            onLiveDictionaryIssue = { prompt ->
                pendingLiveDictionaryTarget = target
                liveDictionaryPrompt = prompt
            },
        )
    }.onSuccess { session -> activeSession = session }
        .onFailure { error -> loadError = error.message }
    isLoading = false
    loadingTargetId = null
}

private suspend fun savePatchSession(
    session: PatchEditorSession,
    data: MonetizationData,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    appState: BitInstallerAppState,
): String {
    val patchTarget =
        findTarget(session.packageName)
            ?: error("Unknown target: ${session.packageName}")
    val encrypted = withContext(Dispatchers.Default) { MonetizationCodec.encrypt(data) }
    val writeResult = repository.writeMonetizationVars(path = session.filePath, content = encrypted)
    manifestStore.recordPatched(target = patchTarget, encryptedContent = encrypted)
    appState.patchPresences = appState.patchPresences + (
        patchTarget.packageName to
            PatchManifestPresence(
                state = PatchPresenceState.PATCHED,
                label = PATCH_PRESENCE_PATCHED_LABEL,
            )
    )
    return "Saved to ${patchTarget.displayName}. Backup: ${writeResult.backupPath.substringAfterLast('/')}"
}

private suspend fun loadPatchSession(
    target: PatchTargetUiState,
    repository: ShizukuMonetizationRepository,
    manifestStore: PatchManifestStore,
    onPatchPresenceChanged: (String, PatchManifestPresence) -> Unit,
    onLiveDictionaryIssue: (LiveDictionaryPromptUiState) -> Unit,
): PatchEditorSession? {
    val patchTarget =
        findTarget(target.packageName)
            ?: error("Unknown target: ${target.packageName}")

    val liveDictionaryState = repository.liveDictionaryState(patchTarget)
    if (liveDictionaryState.status != LiveDictionaryStatus.DIRECTORY) {
        onLiveDictionaryIssue(liveDictionaryState.status.toPromptUiState(patchTarget))
        return null
    }

    val file = repository.readMonetizationVars(patchTarget)
    val presence = manifestStore.presenceFor(target = patchTarget, encryptedContent = file.content)
    onPatchPresenceChanged(patchTarget.packageName, presence)
    return file.toPatchEditorSession(target = target, patchPresence = presence)
}

private fun LiveDictionaryStatus.toPromptUiState(target: PatchTarget): LiveDictionaryPromptUiState =
    when (this) {
        LiveDictionaryStatus.MISSING -> {
            LiveDictionaryPromptUiState(
                title = "Create LiveDictionary?",
                message =
                    "${target.displayName} can reset MonetizationVars to defaults when LiveDictionary is missing. " +
                        "Create the LiveDictionary folder before patching?",
                confirmLabel = "Create folder",
            )
        }

        LiveDictionaryStatus.NOT_DIRECTORY -> {
            LiveDictionaryPromptUiState(
                title = "Replace LiveDictionary?",
                message =
                    "LiveDictionary exists but is not a folder. ${target.displayName} can reset MonetizationVars " +
                        "to defaults in this state. Replace it with a folder and keep a .bitinstaller.bak backup?",
                confirmLabel = "Replace",
            )
        }

        LiveDictionaryStatus.DIRECTORY -> {
            LiveDictionaryPromptUiState(
                title = "LiveDictionary ready",
                message = "LiveDictionary is already a folder.",
                confirmLabel = "Continue",
            )
        }
    }

internal fun requestShizukuPermission(onError: (String?) -> Unit) {
    runCatching {
        if (Shizuku.pingBinder()) {
            Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
        } else {
            onError("Start Shizuku first")
        }
    }.onFailure { error ->
        onError(error.message ?: "Could not request Shizuku permission")
    }
}

private suspend fun MonetizationVarsFile.toPatchEditorSession(
    target: PatchTargetUiState,
    patchPresence: PatchManifestPresence,
): PatchEditorSession =
    PatchEditorSession(
        packageName = target.packageName,
        target =
            target.copy(
                patchState =
                    target.patchState.copy(
                        presenceState = patchPresence.state,
                        presenceLabel = patchPresence.label,
                        statusLabel = "Loaded from ${target.name} files",
                    ),
            ),
        filePath = path,
        initialData = withContext(Dispatchers.Default) { MonetizationCodec.decrypt(content) },
    )
