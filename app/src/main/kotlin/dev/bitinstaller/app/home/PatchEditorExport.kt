package dev.bitinstaller.app.home

import android.content.Context
import android.content.Intent
import dev.bitinstaller.app.crypto.MonetizationCodec

fun exportRawJson(
    context: Context,
    uiState: PatchEditorUiState,
    onStatus: (String?) -> Unit,
    onError: (String?) -> Unit,
) {
    val resolvedData =
        resolveEditorData(
            editorMode = uiState.editorMode,
            rawJson = uiState.rawJson,
            draftValues = uiState.draftValues,
            currentData = uiState.currentData,
        )

    resolvedData
        .onSuccess { data ->
            shareRawJson(context = context, jsonText = MonetizationCodec.toPrettyJson(data))
            onStatus("Raw JSON exported to the Android share sheet.")
            onError(null)
        }.onFailure { error ->
            onError(error.message)
            onStatus(null)
        }
}

private fun shareRawJson(
    context: Context,
    jsonText: String,
) {
    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "MonetizationVars JSON")
            putExtra(Intent.EXTRA_TEXT, jsonText)
        }

    context.startActivity(Intent.createChooser(shareIntent, "Export raw JSON"))
}
