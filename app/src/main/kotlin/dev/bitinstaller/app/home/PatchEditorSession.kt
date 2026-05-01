package dev.bitinstaller.app.home

import dev.bitinstaller.app.crypto.MonetizationData

data class PatchEditorSession(
    val target: PatchTargetUiState,
    val filePath: String,
    val initialData: MonetizationData,
)
