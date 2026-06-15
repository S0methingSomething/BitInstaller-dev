package dev.bitinstaller.app.home

import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveFieldEdit

internal data class SaveSlotEditorState(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val selectedTab: String,
    val showDiscardPrompt: Boolean,
    val navigateBack: Boolean,
    val editsToSave: List<SaveFieldEdit>?,
)

internal sealed interface SaveSlotEditorEvent {
    data class TabSelected(
        val tab: String,
    ) : SaveSlotEditorEvent

    data class SaveRequested(
        val edits: List<SaveFieldEdit>,
    ) : SaveSlotEditorEvent

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

        SaveSlotEditorEvent.DiscardRequested -> {
            state.copy(showDiscardPrompt = true)
        }

        SaveSlotEditorEvent.DismissDiscardPrompt -> {
            state.copy(showDiscardPrompt = false)
        }

        SaveSlotEditorEvent.ConfirmDiscard -> {
            state.copy(
                showDiscardPrompt = false,
                navigateBack = true,
            )
        }

        SaveSlotEditorEvent.BackRequested -> {
            state.copy(navigateBack = true)
        }

        is SaveSlotEditorEvent.SaveRequested -> {
            state.copy(editsToSave = event.edits)
        }

        SaveSlotEditorEvent.NavigateBackHandled -> {
            state.copy(navigateBack = false)
        }
    }

internal fun SnapshotStateMap<String, String>.collectSaveEdits(save: BitLifeSaveSummary): List<SaveFieldEdit> {
    val fieldsById = save.editableFields().associateBy { field -> field.id }
    return entries.mapNotNull { (fieldId, rawValue) ->
        fieldsById[fieldId]?.let { field -> SaveFieldEdit(field = field, rawValue = rawValue) }
    }
}

private fun valuesEqual(
    a: String,
    b: String,
): Boolean =
    a == b || a.toFloatOrNull()?.let { fa ->
        b.toFloatOrNull()?.let { fb -> fa == fb }
    } == true

internal fun SnapshotStateMap<String, String>.draftDirtyCount(save: BitLifeSaveSummary): Int {
    val fieldsById = save.editableFields().associateBy { field -> field.id }
    return entries.count { (fieldId, rawValue) ->
        fieldsById[fieldId]?.let { field -> !valuesEqual(field.value, rawValue) } ?: true
    }
}

internal fun SnapshotStateMap<String, String>.isDraftDirty(save: BitLifeSaveSummary): Boolean =
    draftDirtyCount(save) > 0
