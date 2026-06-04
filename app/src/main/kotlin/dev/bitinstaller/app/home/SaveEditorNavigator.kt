package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

@Composable
internal fun SaveEditorNavigator(
    state: SaveEditorUiState,
    selectedSave: BitLifeSaveSummary?,
    actions: SaveEditorSectionActions,
    callbacks: SaveEditorNavigatorCallbacks,
    modifier: Modifier = Modifier,
) {
    val selectedTarget = state.selectedTarget
    if (selectedTarget == null) {
        SaveEditorTargetList(
            targets = state.targets,
            onTargetClick = actions.onTargetClick,
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    if (selectedSave == null) {
        SaveTargetDetail(
            target = selectedTarget,
            actions =
                SaveTargetCardActions(
                    onTargetClick = actions.onTargetClick,
                    onSaveOpen = callbacks.onSaveOpen,
                ),
            onBackClick = actions.onBackClick,
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    val onFieldChange: (SaveEditableField, String) -> Unit = { field, value ->
        actions.onFieldEdit(selectedTarget, selectedSave, field, value)
    }
    val onAttributeChange: (SaveEditableField, Float) -> Unit = { field, value ->
        actions.onFieldEdit(selectedTarget, selectedSave, field, value.toString())
    }

    SaveSlotEditorDetail(
        target = selectedTarget,
        save = selectedSave,
        actions =
            SaveSlotEditorDetailActions(
                onBackClick = callbacks.onSaveBackClick,
                onFieldChange = onFieldChange,
                onAttributeChange = onAttributeChange,
                onAdvancedClick = { callbacks.onAdvancedClick(selectedSave) },
                onSaveRevert = { callbacks.onSaveRevert(selectedSave) },
            ),
        modifier = modifier.fillMaxSize(),
    )
}

internal data class SaveEditorSectionActions(
    val onTargetClick: (SaveTargetUiState) -> Unit,
    val onFieldEdit: (SaveTargetUiState, BitLifeSaveSummary, SaveEditableField, String) -> Unit,
    val onSaveRevert: (SaveTargetUiState, BitLifeSaveSummary) -> Unit,
    val onBackClick: () -> Unit,
)

internal data class SaveEditorNavigatorCallbacks(
    val onSaveOpen: (BitLifeSaveSummary) -> Unit,
    val onSaveBackClick: () -> Unit,
    val onAdvancedClick: (BitLifeSaveSummary) -> Unit,
    val onSaveRevert: (BitLifeSaveSummary) -> Unit,
)
