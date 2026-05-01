package dev.bitinstaller.app.home

sealed interface BackendStatus {
    data object ShizukuUnavailable : BackendStatus

    data object PermissionRequired : BackendStatus

    data object Ready : BackendStatus

    data class Degraded(val message: String) : BackendStatus
}

enum class PatchSupportState {
    READY,
    BACKEND_REQUIRED,
    UNSUPPORTED,
}

enum class PatchPresenceState {
    NOT_PATCHED,
    PATCHED,
    UNKNOWN,
}

data class PatchTargetUiState(
    val name: String,
    val packageName: String,
    val iconMonogram: String,
    val versionLabel: String,
    val supportState: PatchSupportState,
    val patchPresenceState: PatchPresenceState,
    val patchPresenceLabel: String,
    val statusLabel: String,
    val patchLabel: String,
    val patchEnabled: Boolean,
)

data class HomeUiState(
    val title: String,
    val summary: String,
    val backendStatus: BackendStatus,
    val patchTargets: List<PatchTargetUiState>,
)

fun previewHomeUiState(): HomeUiState =
    HomeUiState(
        title = "BitInstaller",
        summary = "MonetizationVars editor",
        backendStatus = BackendStatus.PermissionRequired,
        patchTargets = listOf(
            PatchTargetUiState(
                name = "BitLife",
                packageName = "com.candywriter.bitlife",
                iconMonogram = "BL",
                versionLabel = "3.27.7",
                supportState = PatchSupportState.READY,
                patchPresenceState = PatchPresenceState.NOT_PATCHED,
                patchPresenceLabel = "Not patched",
                statusLabel = "Ready to load BitLife data",
                patchLabel = "Patch",
                patchEnabled = true,
            ),
        ),
    )
