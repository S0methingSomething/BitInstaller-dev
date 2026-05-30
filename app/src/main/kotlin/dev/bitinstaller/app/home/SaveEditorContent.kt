package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

internal data class SaveEditorSectionActions(
    val onTargetClick: (SaveTargetUiState) -> Unit,
    val onFieldEdit: (SaveTargetUiState, BitLifeSaveSummary, SaveEditableField, String) -> Unit,
    val onSaveRevert: (SaveTargetUiState, BitLifeSaveSummary) -> Unit,
    val onBackClick: () -> Unit,
)

internal data class SaveEditorContentCallbacks(
    val onSaveOpen: (BitLifeSaveSummary) -> Unit,
    val onSaveBackClick: () -> Unit,
    val onFieldClick: (BitLifeSaveSummary, SaveEditableField) -> Unit,
    val onAdvancedClick: (BitLifeSaveSummary) -> Unit,
    val onSaveRevert: (BitLifeSaveSummary) -> Unit,
)

@Composable
internal fun SaveEditorContent(
    state: SaveEditorUiState,
    selectedSave: BitLifeSaveSummary?,
    actions: SaveEditorSectionActions,
    callbacks: SaveEditorContentCallbacks,
) {
    val selectedTarget = state.selectedTarget
    if (selectedTarget == null) {
        SaveEditorTargetList(
            targets = state.targets,
            onTargetClick = actions.onTargetClick,
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SaveSelectedTargetContent(
            target = selectedTarget,
            selectedSave = selectedSave,
            onSaveBackClick = callbacks.onSaveBackClick,
            actions =
                SaveSelectedTargetActions(
                    onTargetClick = actions.onTargetClick,
                    onSaveOpen = callbacks.onSaveOpen,
                    onFieldClick = callbacks.onFieldClick,
                    onAdvancedClick = callbacks.onAdvancedClick,
                    onSaveRevert = callbacks.onSaveRevert,
                    onChangeApp = actions.onBackClick,
                ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
