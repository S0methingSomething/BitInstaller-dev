package dev.bitinstaller.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

@Composable
internal fun SaveEditorSection(
    state: SaveEditorUiState,
    actions: SaveEditorSectionActions,
) {
    val selectedTarget = state.selectedTarget
    val refs = remember { SectionRefs() }
    var selectedSavePath by rememberSaveable(selectedTarget?.packageName) { mutableStateOf<String?>(null) }
    var dismissedSuccessTokens by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val selectedSave = selectedTarget?.saves?.firstOrNull { it.path == selectedSavePath }
    val modalState =
        SaveEditorModalState(
            selectedTarget,
            selectedSavePath,
            refs.advancedSave,
            refs.editDraft,
            refs.revertSave,
        )
    val modalActions = buildModalActions(selectedTarget, actions, refs) { selectedSavePath = null }
    val navigatorCallbacks = buildNavigatorCallbacks(selectedTarget, refs) { selectedSavePath = it }
    val backProgressAnim = rememberSaveEditorBackHandler(modalState, modalActions)
    SaveEditorModals(modalState, modalActions)
    SaveEditorFullscreenFrame(
        config =
            SaveEditorFrameConfig(
                selectedTarget,
                selectedSave,
                selectedTarget?.saveSuccessPopup(dismissedSuccessTokens),
                backProgressAnim.value,
            ),
        onDismissPopup = { popup -> dismissedSuccessTokens = dismissedSuccessTokens + (popup.path to popup.token) },
    ) {
        SaveEditorNavigator(
            state,
            selectedSave,
            actions,
            navigatorCallbacks,
            Modifier.weight(1f),
        )
    }
}

private fun buildModalActions(
    selectedTarget: SaveTargetUiState?,
    actions: SaveEditorSectionActions,
    refs: SectionRefs,
    clearSelectedSavePath: () -> Unit,
) = SaveEditorModalActions(
    closeAdvanced = { refs.advancedSave = null },
    closeEdit = { refs.editDraft = null },
    closeRevert = { refs.revertSave = null },
    openEditFromAdvanced = { save, field ->
        selectedTarget?.let { target ->
            refs.editDraft = SaveFieldEditDraft(target = target, save = save, field = field)
            refs.advancedSave = null
        }
    },
    submitEdit = { draft, value ->
        actions.onFieldEdit(draft.target, draft.save, draft.field, value)
        refs.editDraft = null
    },
    confirmRevert = { target, save ->
        actions.onSaveRevert(target, save)
        refs.revertSave = null
    },
    backToSaves = clearSelectedSavePath,
    backToTargets = actions.onBackClick,
)

private fun buildNavigatorCallbacks(
    selectedTarget: SaveTargetUiState?,
    refs: SectionRefs,
    setSelectedSavePath: (String?) -> Unit,
) = SaveEditorNavigatorCallbacks(
    onSaveOpen = { save -> setSelectedSavePath(save.path) },
    onSaveBackClick = { setSelectedSavePath(null) },
    onFieldClick = { save, field ->
        selectedTarget?.let { target ->
            refs.editDraft = SaveFieldEditDraft(target = target, save = save, field = field)
        }
    },
    onAdvancedClick = { save -> refs.advancedSave = save },
    onSaveRevert = { save -> refs.revertSave = save },
)

private class SectionRefs {
    var advancedSave: BitLifeSaveSummary? by mutableStateOf(null)
    var editDraft: SaveFieldEditDraft? by mutableStateOf(null)
    var revertSave: BitLifeSaveSummary? by mutableStateOf(null)
}
