package dev.bitinstaller.app.home

import dev.bitinstaller.app.crypto.MonetizationCodec
import dev.bitinstaller.app.crypto.MonetizationData

data class HomeRouteCallbacks(
    val onDestinationSelected: (BitInstallerDestination) -> Unit = {},
    val onDashboardActionClick: () -> Unit = {},
    val onPatchClick: (PatchTargetUiState) -> Unit = {},
    val onSaveTargetClick: (SaveTargetUiState) -> Unit = {},
    val onDismissSession: () -> Unit = {},
    val onDismissLiveDictionaryPrompt: () -> Unit = {},
    val onConfirmLiveDictionaryFix: () -> Unit = {},
    val onSaveSession: suspend (PatchEditorSession, MonetizationData) -> String = { _, data ->
        "Saved and re-encrypted file (${MonetizationCodec.encrypt(data).length} chars)."
    },
)
