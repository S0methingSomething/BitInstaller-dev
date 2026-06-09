package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveFieldEdit

internal data class SaveSlotEditorState(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val selectedTab: String,
    val draft: SaveSlotEditDraft,
    val showDiscardPrompt: Boolean,
    val navigateBack: Boolean,
    val editsToSave: List<SaveFieldEdit>?,
)

internal sealed interface SaveSlotEditorEvent {
    data class TabSelected(
        val tab: String,
    ) : SaveSlotEditorEvent

    data class FieldEdited(
        val field: SaveEditableField,
        val value: String,
    ) : SaveSlotEditorEvent

    data object SaveRequested : SaveSlotEditorEvent

    data object DiscardRequested : SaveSlotEditorEvent

    data object BackRequested : SaveSlotEditorEvent

    data object DismissDiscardPrompt : SaveSlotEditorEvent

    data object ConfirmDiscard : SaveSlotEditorEvent

    data object NavigateBackHandled : SaveSlotEditorEvent
}

internal fun saveSlotEditorReduce(
    state: SaveSlotEditorState,
    event: SaveSlotEditorEvent,
): SaveSlotEditorState =
    when (event) {
        is SaveSlotEditorEvent.TabSelected -> {
            state.copy(selectedTab = event.tab)
        }

        is SaveSlotEditorEvent.FieldEdited -> {
            state.copy(draft = state.draft.update(event.field, event.value))
        }

        SaveSlotEditorEvent.DiscardRequested -> {
            state.copy(showDiscardPrompt = true)
        }

        SaveSlotEditorEvent.DismissDiscardPrompt -> {
            state.copy(showDiscardPrompt = false)
        }

        SaveSlotEditorEvent.ConfirmDiscard -> {
            state.copy(
                draft = SaveSlotEditDraft(),
                showDiscardPrompt = false,
                navigateBack = true,
            )
        }

        SaveSlotEditorEvent.BackRequested -> {
            if (state.draft.isDirty) {
                state.copy(showDiscardPrompt = true)
            } else {
                state.copy(navigateBack = true)
            }
        }

        SaveSlotEditorEvent.SaveRequested -> {
            state.copy(editsToSave = state.draft.toEdits(state.save))
        }

        SaveSlotEditorEvent.NavigateBackHandled -> {
            state.copy(navigateBack = false)
        }
    }
