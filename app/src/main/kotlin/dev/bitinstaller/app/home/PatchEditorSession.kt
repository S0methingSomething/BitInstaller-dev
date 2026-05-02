package dev.bitinstaller.app.home

import dev.bitinstaller.app.crypto.MonetizationData

/**
 * Active editor session state.
 *
 * [packageName] is the domain identity key — used by [savePatchSession] to
 * look up the [PatchTarget] without re-deriving it from the UI model.
 * [target] carries the UI state for display in the editor overlay.
 */
data class PatchEditorSession(
    val packageName: String,
    val target: PatchTargetUiState,
    val filePath: String,
    val initialData: MonetizationData,
)
