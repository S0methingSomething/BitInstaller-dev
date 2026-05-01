package dev.bitinstaller.app.home

import dev.bitinstaller.app.crypto.MonetizationCodec

internal fun applyUnlockAllPatch(
    uiState: PatchEditorUiState,
    mutations: PatchEditorStateMutations,
) {
    resolveEditorData(
        editorMode = uiState.editorMode,
        rawJson = uiState.rawJson,
        draftValues = uiState.draftValues,
        currentData = uiState.currentData,
    ).onSuccess { data ->
        val patchedData = MonetizationCodec.applyUnlockAllPatch(data)
        mutations.onCurrentDataChanged(patchedData)
        mutations.onDraftValuesChanged(patchedData.toDraftValues())
        mutations.onRawJsonChanged(MonetizationCodec.toPrettyJson(patchedData))
        mutations.onStatusMessageChanged("Unlock all applied. Review, then save the file.")
        mutations.onErrorMessageChanged(null)
    }.onFailure { error ->
        mutations.onErrorMessageChanged(error.message)
        mutations.onStatusMessageChanged(null)
    }
}
