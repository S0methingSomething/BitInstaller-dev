package dev.bitinstaller.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.bitinstaller.app.save.BitLifeSaveSummary

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
        SaveEditorModalState(selectedTarget, selectedSavePath, refs.advancedSave, refs.revertSave)
    val modalActions =
        SaveEditorModalActions(
            closeAdvanced = { refs.advancedSave = null },
            closeRevert = { refs.revertSave = null },
            confirmRevert = { target, save ->
                actions.onSaveRevert(target, save)
                refs.revertSave = null
            },
            backToSaves = { selectedSavePath = null },
            backToTargets = actions.onBackClick,
        )
    val navigatorCallbacks =
        SaveEditorNavigatorCallbacks(
            onSaveOpen = { save -> selectedSavePath = save.path },
            onSaveBackClick = { selectedSavePath = null },
            onAdvancedClick = { save -> refs.advancedSave = save },
            onSaveRevert = { save -> refs.revertSave = save },
        )
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
        SaveEditorNavigator(state, selectedSave, actions, navigatorCallbacks, Modifier.weight(1f))
    }
}

private class SectionRefs {
    var advancedSave: BitLifeSaveSummary? by mutableStateOf(null)
    var revertSave: BitLifeSaveSummary? by mutableStateOf(null)
}
