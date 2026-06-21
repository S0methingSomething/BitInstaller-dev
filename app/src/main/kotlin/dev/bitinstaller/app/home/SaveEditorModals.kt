package dev.bitinstaller.app.home

import androidx.compose.runtime.Composable

internal data class SaveEditorModalState(
    val selectedTarget: SaveTargetUiState?,
    val selectedSavePath: String?,
    val revertSave: dev.bitinstaller.app.save.BitLifeSaveSummary?,
)

internal data class SaveEditorModalActions(
    val closeRevert: () -> Unit,
    val confirmRevert: (SaveTargetUiState, dev.bitinstaller.app.save.BitLifeSaveSummary) -> Unit,
    val backToSaves: () -> Unit,
    val backToTargets: () -> Unit,
)

@Composable
internal fun SaveEditorModals(
    state: SaveEditorModalState,
    actions: SaveEditorModalActions,
) {
    state.revertSave?.let { save ->
        SaveRevertDialog(
            save = save,
            target = state.selectedTarget,
            onDismissRequest = actions.closeRevert,
            onConfirm = actions.confirmRevert,
        )
    }
}
