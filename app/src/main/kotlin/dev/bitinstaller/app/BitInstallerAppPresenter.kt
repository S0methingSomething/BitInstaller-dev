package dev.bitinstaller.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.bitinstaller.app.home.BackendStatus
import dev.bitinstaller.app.home.HomeUiState
import dev.bitinstaller.app.home.PatchManifestPresence
import dev.bitinstaller.app.home.PatchManifestStore
import dev.bitinstaller.app.home.PatchPresenceState
import dev.bitinstaller.app.home.PatchSupportState
import dev.bitinstaller.app.home.PatchTargetUiState
import dev.bitinstaller.app.home.TargetIcon
import dev.bitinstaller.app.home.TargetPatchState
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

internal class BitInstallerAppPresenter {
    val repository = ShizukuMonetizationRepository()
    val manifestStore = PatchManifestStore(repository)
    val operationLock = OperationLock()
    val appState =
        BitInstallerAppState(
            initialSnapshot = ShizukuSnapshot(ShizukuAccessStatus.UNAVAILABLE, null),
        )

    private var appInfoMap by mutableStateOf(emptyMap<String, InstalledAppInfo>())

    suspend fun initialize(context: Context) {
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

    fun buildHomeUiState(): HomeUiState {
        val snapshot = appState.snapshot
        val isReady = snapshot.status == ShizukuAccessStatus.READY
        val canRequest = appState.binderReady

        return HomeUiState(
            title = "BitInstaller",
            summary = "MonetizationVars editor",
            backendStatus =
                when {
                    isReady -> {
                        BackendStatus.Ready
                    }

                    canRequest -> {
                        when (snapshot.status) {
                            ShizukuAccessStatus.UNAVAILABLE -> BackendStatus.ShizukuUnavailable
                            ShizukuAccessStatus.PERMISSION_REQUIRED -> BackendStatus.PermissionRequired
                            ShizukuAccessStatus.READY -> BackendStatus.Ready
                        }
                    }

                    else -> {
                        BackendStatus.ShizukuUnavailable
                    }
                },
            patchTargets =
                ALL_TARGETS
                    .map { target ->
                        val info = appInfoMap[target.packageName]
                        buildTargetUiState(target, info, isReady)
                    }.sortedWith(
                        compareByDescending<PatchTargetUiState> { it.isInstalled }.thenBy { it.name },
                    ),
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
                    statusLabel = statusLabelFor(isReady, isLoading, loadError, installed),
                    actionLabel = if (isLoading) "Loading" else "Patch",
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
        !isInstalled -> "Not installed"
        !isReady -> "Locked"
        presence != null -> presence.label
        else -> "Unknown"
    }

private fun statusLabelFor(
    isReady: Boolean,
    isLoading: Boolean,
    loadError: String?,
    isInstalled: Boolean,
): String =
    when {
        !isInstalled -> "App not installed on device"
        isLoading -> "Reading MonetizationVars"
        loadError != null -> loadError
        isReady -> "Ready to load data"
        else -> "Grant Shizuku access first"
    }
