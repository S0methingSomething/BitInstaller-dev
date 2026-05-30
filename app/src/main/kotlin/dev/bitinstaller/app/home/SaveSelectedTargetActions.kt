package dev.bitinstaller.app.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

internal data class SaveSelectedTargetActions(
    val onTargetClick: (SaveTargetUiState) -> Unit,
    val onSaveOpen: (BitLifeSaveSummary) -> Unit,
    val onFieldClick: (BitLifeSaveSummary, SaveEditableField) -> Unit,
    val onAdvancedClick: (BitLifeSaveSummary) -> Unit,
    val onSaveRevert: (BitLifeSaveSummary) -> Unit,
    val onChangeApp: () -> Unit,
)

@Composable
internal fun SaveSelectedTargetContent(
    target: SaveTargetUiState,
    selectedSave: BitLifeSaveSummary?,
    onSaveBackClick: () -> Unit,
    actions: SaveSelectedTargetActions,
    modifier: Modifier = Modifier,
) {
    if (selectedSave == null) {
        SaveTargetDetail(
            target = target,
            actions =
                SaveTargetCardActions(
                    onTargetClick = actions.onTargetClick,
                    onSaveOpen = actions.onSaveOpen,
                ),
            onBackClick = actions.onChangeApp,
            modifier = modifier,
        )
        return
    }

    SaveSlotEditorDetail(
        target = target,
        save = selectedSave,
        actions =
            SaveSlotEditorDetailActions(
                onBackClick = onSaveBackClick,
                onFieldClick = { field -> actions.onFieldClick(selectedSave, field) },
                onAdvancedClick = { actions.onAdvancedClick(selectedSave) },
                onSaveRevert = { actions.onSaveRevert(selectedSave) },
            ),
        modifier = modifier,
    )
}
