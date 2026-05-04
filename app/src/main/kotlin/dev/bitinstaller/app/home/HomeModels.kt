package dev.bitinstaller.app.home

import android.graphics.drawable.Drawable

sealed interface BackendStatus {
    data object ShizukuUnavailable : BackendStatus

    data object PermissionRequired : BackendStatus

    data object Ready : BackendStatus
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

internal const val PATCH_PRESENCE_PATCHED_LABEL: String = "Patched"
internal const val PATCH_PRESENCE_NOT_PATCHED_LABEL: String = "No patch"

/** Icon representation for a target app — real drawable or fallback monogram. */
class TargetIcon(
    val monogram: String,
    val drawable: Drawable? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TargetIcon) return false
        return monogram == other.monogram
    }

    override fun hashCode(): Int = monogram.hashCode()
}

/** Derived presentation state for a target's patch action and status display. */
data class TargetPatchState(
    val supportState: PatchSupportState,
    val presenceState: PatchPresenceState,
    val presenceLabel: String,
    val statusLabel: String,
    val actionLabel: String,
    val actionEnabled: Boolean,
)

/**
 * UI state for a single patch target card.
 *
 * This is a plain class (not data class) because [TargetIcon] contains a
 * [Drawable] with no value-equality — including it in generated
 * equals/hashCode would break Compose recomposition skipping and list diffing.
 */
class PatchTargetUiState(
    val name: String,
    val packageName: String,
    val icon: TargetIcon,
    val versionLabel: String,
    val isInstalled: Boolean = true,
    val patchState: TargetPatchState,
) {
    /** Copy with overrides, similar to data class copy(). */
    fun copy(
        name: String = this.name,
        packageName: String = this.packageName,
        icon: TargetIcon = this.icon,
        patchState: TargetPatchState = this.patchState,
    ): PatchTargetUiState =
        PatchTargetUiState(
            name = name,
            packageName = packageName,
            icon = icon,
            versionLabel = versionLabel,
            isInstalled = isInstalled,
            patchState = patchState,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PatchTargetUiState) return false
        return name == other.name &&
            packageName == other.packageName &&
            icon == other.icon &&
            versionLabel == other.versionLabel &&
            isInstalled == other.isInstalled &&
            patchState == other.patchState
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + versionLabel.hashCode()
        result = 31 * result + isInstalled.hashCode()
        result = 31 * result + patchState.hashCode()
        return result
    }
}

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
        patchTargets =
            listOf(
                PatchTargetUiState(
                    name = "BitLife",
                    packageName = "com.candywriter.bitlife",
                    icon = TargetIcon(monogram = "BL"),
                    versionLabel = "3.27.7",
                    patchState =
                        TargetPatchState(
                            supportState = PatchSupportState.READY,
                            presenceState = PatchPresenceState.NOT_PATCHED,
                            presenceLabel = PATCH_PRESENCE_NOT_PATCHED_LABEL,
                            statusLabel = "Tap Patch to begin",
                            actionLabel = "Patch",
                            actionEnabled = true,
                        ),
                ),
            ),
    )
