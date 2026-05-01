package dev.bitinstaller.app.home

import android.content.Context
import dev.bitinstaller.app.crypto.MonetizationCodec
import dev.bitinstaller.app.crypto.MonetizationData
import dev.bitinstaller.app.crypto.MonetizationValue

enum class EditorMode {
    SIMPLIFIED,
    RAW,
}

data class PatchEditorUiState(
    val currentData: MonetizationData,
    val draftValues: Map<String, String>,
    val rawJson: String,
    val editorMode: EditorMode,
    val statusMessage: String?,
    val errorMessage: String?,
    val isSaving: Boolean,
)

data class PatchEditorActions(
    val onDismissRequest: () -> Unit,
    val onModeSelected: (EditorMode) -> Unit,
    val onBooleanChanged: (String, Boolean) -> Unit,
    val onTextChanged: (String, String) -> Unit,
    val onRawJsonChanged: (String) -> Unit,
    val onExportRawJson: () -> Unit,
    val onSave: () -> Unit,
)

data class PatchEditorSaveCallbacks(
    val onSaveStarted: () -> Unit,
    val onSaveSuccess: () -> Unit,
    val onSaveFailure: () -> Unit,
)

fun interface PatchEditorSaveHandler {
    fun save(
        data: MonetizationData,
        onSuccess: (String) -> Unit,
        onError: (String?) -> Unit,
    )
}

data class PatchEditorStateMutations(
    val onCurrentDataChanged: (MonetizationData) -> Unit,
    val onDraftValuesChanged: (Map<String, String>) -> Unit,
    val onRawJsonChanged: (String) -> Unit,
    val onEditorModeChanged: (EditorMode) -> Unit,
    val onStatusMessageChanged: (String?) -> Unit,
    val onErrorMessageChanged: (String?) -> Unit,
    val onSavingChanged: (Boolean) -> Unit,
)

data class PatchEditorActionConfig(
    val context: Context,
    val onDismissRequest: () -> Unit,
    val saveCallbacks: PatchEditorSaveCallbacks = PatchEditorSaveCallbacks({}, {}, {}),
    val saveHandler: PatchEditorSaveHandler = previewSaveHandler(),
)

fun buildPatchEditorActions(
    uiState: PatchEditorUiState,
    mutations: PatchEditorStateMutations,
    config: PatchEditorActionConfig,
): PatchEditorActions =
    PatchEditorActions(
        onDismissRequest = config.onDismissRequest,
        onModeSelected = { mode ->
            mutations.onEditorModeChanged(mode)
            mutations.onErrorMessageChanged(null)
        },
        onBooleanChanged = { key, value ->
            val updatedData = uiState.currentData.updated(key = key, value = value)
            mutations.onCurrentDataChanged(updatedData)
            mutations.onDraftValuesChanged(
                uiState.draftValues.updated(key = key, value = value.toString()),
            )
            mutations.onRawJsonChanged(MonetizationCodec.toPrettyJson(updatedData))
            mutations.onErrorMessageChanged(null)
        },
        onTextChanged = { key, value ->
            mutations.onDraftValuesChanged(uiState.draftValues.updated(key = key, value = value))
        },
        onRawJsonChanged = { updatedJson ->
            mutations.onRawJsonChanged(updatedJson)
            mutations.onErrorMessageChanged(null)
        },
        onExportRawJson = {
            exportRawJson(
                context = config.context,
                uiState = uiState,
                onStatus = mutations.onStatusMessageChanged,
                onError = mutations.onErrorMessageChanged,
            )
        },
        onSave = {
            saveEditorData(uiState = uiState, mutations = mutations, config = config)
        },
    )

private fun previewSaveHandler(): PatchEditorSaveHandler =
    PatchEditorSaveHandler { data, onSuccess, _ ->
        onSuccess("Saved and re-encrypted file (${MonetizationCodec.encrypt(data).length} chars).")
    }

private fun saveEditorData(
    uiState: PatchEditorUiState,
    mutations: PatchEditorStateMutations,
    config: PatchEditorActionConfig,
) {
    mutations.onSavingChanged(true)
    config.saveCallbacks.onSaveStarted()

    resolveEditorData(
        editorMode = uiState.editorMode,
        rawJson = uiState.rawJson,
        draftValues = uiState.draftValues,
        currentData = uiState.currentData,
    ).onSuccess { data ->
        config.saveHandler.save(
            data,
            { statusMessage -> handleSaveSuccess(data, statusMessage, mutations, config) },
            { error -> handleSaveFailure(error, mutations, config) },
        )
    }.onFailure { error ->
        handleSaveFailure(error.message, mutations, config)
    }
}

private fun handleSaveSuccess(
    data: MonetizationData,
    statusMessage: String,
    mutations: PatchEditorStateMutations,
    config: PatchEditorActionConfig,
) {
    mutations.onCurrentDataChanged(data)
    mutations.onDraftValuesChanged(data.toDraftValues())
    mutations.onRawJsonChanged(MonetizationCodec.toPrettyJson(data))
    mutations.onStatusMessageChanged(statusMessage)
    mutations.onErrorMessageChanged(null)
    mutations.onSavingChanged(false)
    config.saveCallbacks.onSaveSuccess()
}

private fun handleSaveFailure(
    error: String?,
    mutations: PatchEditorStateMutations,
    config: PatchEditorActionConfig,
) {
    mutations.onErrorMessageChanged(error)
    mutations.onStatusMessageChanged(null)
    mutations.onSavingChanged(false)
    config.saveCallbacks.onSaveFailure()
}

fun resolveEditorData(
    editorMode: EditorMode,
    rawJson: String,
    draftValues: Map<String, String>,
    currentData: MonetizationData,
): Result<MonetizationData> =
    runCatching {
        when (editorMode) {
            EditorMode.RAW -> MonetizationCodec.parseJsonObject(rawJson)
            EditorMode.SIMPLIFIED -> buildDataFromDrafts(draftValues = draftValues, currentData = currentData)
        }
    }

fun MonetizationData.toDraftValues(): Map<String, String> =
    entries.associate { (key, value) ->
        key to value.toString()
    }

fun MonetizationData.updated(
    key: String,
    value: MonetizationValue,
): MonetizationData {
    val updated = linkedMapOf<String, MonetizationValue>()
    forEach { (existingKey, existingValue) ->
        updated[existingKey] = if (existingKey == key) value else existingValue
    }
    return updated
}

fun Map<String, String>.updated(
    key: String,
    value: String,
): Map<String, String> =
    toMutableMap().apply {
        this[key] = value
    }

private fun buildDataFromDrafts(
    draftValues: Map<String, String>,
    currentData: MonetizationData,
): MonetizationData {
    val updated = linkedMapOf<String, MonetizationValue>()

    currentData.forEach { (key, value) ->
        val draftValue = draftValues[key].orEmpty()
        updated[key] =
            when (value) {
                is Boolean -> {
                    draftValue.toBooleanStrictOrNull() ?: error("'$key' must stay a boolean")
                }

                is Int -> draftValue.toIntOrNull() ?: error("'$key' must stay an Int32")
                is String -> draftValue
                else -> error("Unsupported type for '$key'")
            }
    }

    return updated
}
