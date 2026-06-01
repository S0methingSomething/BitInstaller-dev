package dev.bitinstaller.app

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dev.bitinstaller.app.home.BackendStatus
import dev.bitinstaller.app.home.HomeNoticeUiState
import dev.bitinstaller.app.home.HomeUiState
import dev.bitinstaller.app.home.PATCH_PRESENCE_NOT_PATCHED_LABEL
import dev.bitinstaller.app.home.PATCH_PRESENCE_PATCHED_LABEL
import dev.bitinstaller.app.home.PatchManifestPresence
import dev.bitinstaller.app.home.PatchManifestStore
import dev.bitinstaller.app.home.PatchPresenceState
import dev.bitinstaller.app.home.PatchSupportState
import dev.bitinstaller.app.home.PatchTargetUiState
import dev.bitinstaller.app.home.SaveEditorUiState
import dev.bitinstaller.app.home.SaveTargetUiState
import dev.bitinstaller.app.home.TargetIcon
import dev.bitinstaller.app.home.TargetPatchState
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.shizuku.OperationLock
import dev.bitinstaller.app.shizuku.ShizukuAccessStatus
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.ShizukuSnapshot
import dev.bitinstaller.app.targets.ALL_TARGETS
import dev.bitinstaller.app.targets.InstalledAppInfo
import dev.bitinstaller.app.targets.PatchTarget
import dev.bitinstaller.app.targets.resolveAllAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class BitInstallerAppPresenter(
    application: Application,
) : AndroidViewModel(application) {
    val repository = ShizukuMonetizationRepository()
    val manifestStore = PatchManifestStore(repository)
    val operationLock = OperationLock()
    val appState =
        BitInstallerAppState(
            initialSnapshot = ShizukuSnapshot(ShizukuAccessStatus.UNAVAILABLE, null),
        )

    private var appInfoMap by mutableStateOf(emptyMap<String, InstalledAppInfo>())

    suspend fun initialize() {
        val context = getApplication<Application>()
        appInfoMap = withContext(Dispatchers.IO) { resolveAllAppInfo(context) }
        appState.snapshot = withContext(Dispatchers.IO) { repository.checkStatus() }
        recoverPresencesIfReady()
    }

    /**
     * Recover patch presences from remote manifests when Shizuku is ready.
     *
     * Called from [LaunchedEffect] keyed on snapshot status so that it
     * fires after a permission grant as well as on cold start.
     */
    suspend fun recoverPresencesIfReady() {
        if (appState.snapshot.status != ShizukuAccessStatus.READY) return
        if (appState.patchPresences.isNotEmpty()) return
        val installed =
            ALL_TARGETS.filter {
                appInfoMap[it.packageName]?.isInstalled == true
            }
        appState.patchPresences = manifestStore.recoverPresences(installed)
    }

    val homeUiState: State<HomeUiState> =
        derivedStateOf {
            val snapshot = appState.snapshot
            val isReady = snapshot.status == ShizukuAccessStatus.READY

            HomeUiState(
                title = "BitInstaller",
                summary = "MonetizationVars editor",
                backendStatus =
                    when (snapshot.status) {
                        ShizukuAccessStatus.UNAVAILABLE -> BackendStatus.ShizukuUnavailable
                        ShizukuAccessStatus.PERMISSION_REQUIRED -> BackendStatus.PermissionRequired
                        ShizukuAccessStatus.READY -> BackendStatus.Ready
                    },
                patchTargets =
                    ALL_TARGETS
                        .map { target ->
                            val info = appInfoMap[target.packageName]
                            buildTargetUiState(target, info, isReady)
                        }.sortedWith(
                            compareByDescending<PatchTargetUiState> { it.isInstalled }.thenBy { it.name },
                        ),
                selectedDestination = appState.selectedDestination,
                saveEditor = buildSaveEditorUiState(isReady),
                notice =
                    appState.noticeMessage?.let { message ->
                        HomeNoticeUiState(token = appState.noticeToken, message = message)
                    },
            )
        }

    private fun buildSaveEditorUiState(isReady: Boolean): SaveEditorUiState =
        ALL_TARGETS
            .mapNotNull { target ->
                val info = appInfoMap[target.packageName]
                if (info?.isInstalled != true) return@mapNotNull null
                buildSaveTargetUiState(target = target, info = info, isReady = isReady)
            }.sortedBy { it.name }
            .let { targets ->
                SaveEditorUiState(
                    targets = targets,
                    selectedTarget = targets.firstOrNull { it.packageName == appState.selectedSaveTargetId },
                )
            }

    private fun buildSaveTargetUiState(
        target: PatchTarget,
        info: InstalledAppInfo,
        isReady: Boolean,
    ): SaveTargetUiState {
        val targetId = target.packageName
        val isLoading = appState.saveScanTargetId == targetId
        val isSelected = appState.selectedSaveTargetId == targetId
        val saves = appState.saveScanResults[targetId]
        val error = appState.saveScanErrors[targetId]

        return SaveTargetUiState(
            name = info.appName,
            packageName = targetId,
            icon = TargetIcon(monogram = target.monogram, drawable = info.icon),
            versionLabel = info.versionName,
            isLoading = isLoading,
            statusLabel = saveStatusLabelFor(isReady, isLoading, error, saves),
            actionLabel = saveActionLabelFor(isLoading, saves, isSelected),
            actionEnabled = isReady && !isLoading,
            saves = saves,
            editingSavePath = appState.saveEditTargetPath,
            editErrors = appState.saveEditErrors,
            editMessages = appState.saveEditMessages,
            editMessageTokens = appState.saveEditMessageTokens,
            recentEditFieldIds = appState.saveRecentEditFieldIds,
        )
    }

    private fun buildTargetUiState(
        target: PatchTarget,
        info: InstalledAppInfo?,
        isReady: Boolean,
    ): PatchTargetUiState {
        val installed = info?.isInstalled == true
        val targetId = target.packageName
        val isLoading = appState.isLoading && appState.loadingTargetId == targetId
        val loadError = if (appState.loadingTargetId == targetId) appState.loadError else null
        val presence = appState.patchPresences[targetId]

        return PatchTargetUiState(
            name = info?.appName ?: target.displayName,
            packageName = targetId,
            icon = TargetIcon(monogram = target.monogram, drawable = info?.icon),
            versionLabel = info?.versionName.orEmpty(),
            isInstalled = installed,
            patchState =
                TargetPatchState(
                    supportState = supportStateFor(isReady, installed),
                    presenceState = presenceStateFor(isReady, presence),
                    presenceLabel = presenceLabelFor(isReady, installed, presence),
                    statusLabel = statusLabelFor(isReady, isLoading, loadError, installed, presence),
                    actionLabel = actionLabelFor(isReady, isLoading, installed, presence),
                    actionEnabled = isReady && installed && !isLoading,
                ),
        )
    }
}

private fun supportStateFor(
    isReady: Boolean,
    isInstalled: Boolean,
): PatchSupportState =
    when {
        !isInstalled -> PatchSupportState.UNSUPPORTED
        isReady -> PatchSupportState.READY
        else -> PatchSupportState.BACKEND_REQUIRED
    }

private fun presenceStateFor(
    isReady: Boolean,
    presence: PatchManifestPresence?,
): PatchPresenceState =
    when {
        !isReady -> PatchPresenceState.UNKNOWN
        presence != null -> presence.state
        else -> PatchPresenceState.UNKNOWN
    }

private fun presenceLabelFor(
    isReady: Boolean,
    isInstalled: Boolean,
    presence: PatchManifestPresence?,
): String =
    when {
        !isInstalled -> "Not on device"
        !isReady -> "Waiting"
        presence?.state == PatchPresenceState.PATCHED -> PATCH_PRESENCE_PATCHED_LABEL
        presence?.state == PatchPresenceState.NOT_PATCHED -> PATCH_PRESENCE_NOT_PATCHED_LABEL
        else -> "Ready"
    }

private fun statusLabelFor(
    isReady: Boolean,
    isLoading: Boolean,
    loadError: String?,
    isInstalled: Boolean,
    presence: PatchManifestPresence?,
): String =
    when {
        !isInstalled -> "Install it to patch later"
        isLoading -> "Opening editor"
        loadError != null -> loadError
        !isReady -> "Connect Shizuku first"
        presence?.state == PatchPresenceState.PATCHED -> "Saved patch found"
        presence?.state == PatchPresenceState.NOT_PATCHED -> "No saved patch yet"
        else -> "Tap Patch to begin"
    }

private fun actionLabelFor(
    isReady: Boolean,
    isLoading: Boolean,
    isInstalled: Boolean,
    presence: PatchManifestPresence?,
): String =
    when {
        !isInstalled -> "Install first"
        !isReady -> "Connect first"
        isLoading -> "Opening"
        presence?.state == PatchPresenceState.PATCHED -> "Review"
        else -> "Patch"
    }

private fun saveStatusLabelFor(
    isReady: Boolean,
    isLoading: Boolean,
    error: String?,
    saves: List<BitLifeSaveSummary>?,
): String =
    when {
        !isReady -> "Connect Shizuku first"
        isLoading -> "Scanning current savedLife.data slots"
        error != null -> error
        saves == null -> "Open to scan save slots"
        saves.isEmpty() -> "No savedLife.data files found"
        else -> "${saves.size} ${"save".pluralize(saves.size)} processed"
    }

private fun saveActionLabelFor(
    isLoading: Boolean,
    saves: List<BitLifeSaveSummary>?,
    isSelected: Boolean,
): String =
    when {
        isLoading -> "Scanning"
        !isSelected -> "Open"
        saves == null -> "Scan"
        else -> "Rescan"
    }

private fun String.pluralize(count: Int): String = if (count == 1) this else "${this}s"
