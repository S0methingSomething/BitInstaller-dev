package dev.bitinstaller.app.home

import dev.bitinstaller.app.crypto.MonetizationCodec
import dev.bitinstaller.app.crypto.MonetizationData
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

data class HomeRouteCallbacks(
    val onDestinationSelected: (BitInstallerDestination) -> Unit = {},
    val onDashboardActionClick: () -> Unit = {},
    val onPatchClick: (PatchTargetUiState) -> Unit = {},
    val onSaveTargetClick: (SaveTargetUiState) -> Unit = {},
    val onSaveFieldEdit: (SaveTargetUiState, BitLifeSaveSummary, SaveEditableField, String) -> Unit = { _, _, _, _ -> },
    val onSaveRevert: (SaveTargetUiState, BitLifeSaveSummary) -> Unit = { _, _ -> },
    val onSaveEditorBack: () -> Unit = {},
    val onDismissSession: () -> Unit = {},
    val onDismissNotice: () -> Unit = {},
    val onDismissLiveDictionaryPrompt: () -> Unit = {},
    val onConfirmLiveDictionaryFix: () -> Unit = {},
    val onSaveSession: suspend (PatchEditorSession, MonetizationData) -> String = { _, data ->
        "Saved and re-encrypted file (${MonetizationCodec.encrypt(data).length} chars)."
    },
)
