package dev.bitinstaller.app.home

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

internal data class SaveEditorModalState(
    val selectedTarget: SaveTargetUiState?,
    val selectedSavePath: String?,
    val advancedSave: BitLifeSaveSummary?,
    val editDraft: SaveFieldEditDraft?,
    val revertSave: BitLifeSaveSummary?,
)

internal data class SaveEditorModalActions(
    val closeAdvanced: () -> Unit,
    val closeEdit: () -> Unit,
    val closeRevert: () -> Unit,
    val openEditFromAdvanced: (BitLifeSaveSummary, SaveEditableField) -> Unit,
    val submitEdit: (SaveFieldEditDraft, String) -> Unit,
    val confirmRevert: (SaveTargetUiState, BitLifeSaveSummary) -> Unit,
    val backToSaves: () -> Unit,
    val backToTargets: () -> Unit,
)

@Composable
internal fun SaveEditorBackHandler(
    state: SaveEditorModalState,
    actions: SaveEditorModalActions,
) {
    PredictiveBackHandler(enabled = state.hasVisibleModalOrDetail()) { progress ->
        progress.collect { /* allow system animation */ }
        when {
            state.editDraft != null -> actions.closeEdit()
            state.advancedSave != null -> actions.closeAdvanced()
            state.revertSave != null -> actions.closeRevert()
            state.selectedSavePath != null -> actions.backToSaves()
            state.selectedTarget != null -> actions.backToTargets()
        }
    }
}

@Composable
internal fun SaveEditorModals(
    state: SaveEditorModalState,
    actions: SaveEditorModalActions,
) {
    state.advancedSave?.let { save ->
        SaveAdvancedFieldsDialog(
            targetName = state.selectedTarget?.name ?: "BitLife",
            save = save,
            recentFieldIds =
                state.selectedTarget
                    ?.recentEditFieldIds
                    ?.get(save.path)
                    .orEmpty(),
            onDismissRequest = actions.closeAdvanced,
            onFieldClick = { field -> actions.openEditFromAdvanced(save, field) },
        )
    }
    state.editDraft?.let { draft ->
        SaveFieldEditDialog(
            draft = draft,
            onDismissRequest = actions.closeEdit,
            onConfirm = { value -> actions.submitEdit(draft, value) },
        )
    }
    state.revertSave?.let { save ->
        SaveRevertDialog(
            save = save,
            target = state.selectedTarget,
            onDismissRequest = actions.closeRevert,
            onConfirm = actions.confirmRevert,
        )
    }
}

private fun SaveEditorModalState.hasVisibleModalOrDetail(): Boolean =
    editDraft != null ||
        advancedSave != null ||
        revertSave != null ||
        selectedSavePath != null ||
        selectedTarget != null
