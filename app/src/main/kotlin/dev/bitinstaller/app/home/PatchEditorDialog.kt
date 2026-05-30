package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.crypto.MonetizationCodec
import dev.bitinstaller.app.crypto.MonetizationData
import kotlinx.coroutines.launch

private const val EDITOR_CONTENT_RISE_DP: Float = 42f

data class PatchEditorSceneConfig(
    val initialData: MonetizationData = buildPreviewData(),
    val saveData: suspend (MonetizationData) -> String = { data ->
        "Saved and re-encrypted file (${MonetizationCodec.encrypt(data).length} chars)."
    },
)

private data class PatchEditorContentChrome(
    val contentAlpha: Float,
)

@Composable
fun PatchEditorScene(
    target: PatchTargetUiState,
    contentAlpha: Float,
    onDismissRequest: () -> Unit,
    config: PatchEditorSceneConfig = PatchEditorSceneConfig(),
) {
    var currentData by remember(target.packageName, config.initialData) {
        mutableStateOf(config.initialData)
    }
    var draftValues by remember(target.packageName, config.initialData) {
        mutableStateOf(currentData.toDraftValues())
    }
    var rawJson by remember(target.packageName, config.initialData) {
        mutableStateOf(MonetizationCodec.toPrettyJson(currentData))
    }
    var editorMode by rememberSaveable { mutableStateOf(EditorMode.SIMPLIFIED) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val uiState =
        PatchEditorUiState(
            currentData = currentData,
            draftValues = draftValues,
            rawJson = rawJson,
            editorMode = editorMode,
            statusMessage = statusMessage,
            errorMessage = errorMessage,
            isSaving = isSaving,
        )
    val mutations =
        PatchEditorStateMutations(
            onCurrentDataChanged = { currentData = it },
            onDraftValuesChanged = { draftValues = it },
            onRawJsonChanged = { rawJson = it },
            onEditorModeChanged = { editorMode = it },
            onStatusMessageChanged = { statusMessage = it },
            onErrorMessageChanged = { errorMessage = it },
            onSavingChanged = { isSaving = it },
        )

    PatchEditorContent(
        chrome =
            PatchEditorContentChrome(
                contentAlpha = contentAlpha,
            ),
        uiState = uiState,
        actions =
            rememberPatchEditorActions(
                uiState = uiState,
                mutations = mutations,
                onDismissRequest = onDismissRequest,
                config = config,
            ),
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun rememberPatchEditorActions(
    uiState: PatchEditorUiState,
    mutations: PatchEditorStateMutations,
    onDismissRequest: () -> Unit,
    config: PatchEditorSceneConfig,
): PatchEditorActions {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    return buildPatchEditorActions(
        uiState = uiState,
        mutations = mutations,
        config =
            PatchEditorActionConfig(
                context = context,
                onDismissRequest = onDismissRequest,
                saveHandler =
                    PatchEditorSaveHandler { data, onSuccess, onError ->
                        coroutineScope.launch {
                            runCatching { config.saveData(data) }
                                .onSuccess(onSuccess)
                                .onFailure { error -> onError(error.message) }
                        }
                    },
            ),
    )
}

@Composable
private fun PatchEditorContent(
    chrome: PatchEditorContentChrome,
    uiState: PatchEditorUiState,
    actions: PatchEditorActions,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 0.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .sizeIn(maxHeight = 760.dp)
                    .graphicsLayer {
                        alpha = chrome.contentAlpha
                        translationY = (1f - chrome.contentAlpha) * EDITOR_CONTENT_RISE_DP
                    },
        ) {
            Column(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                PatchEditorToolbar(editorMode = uiState.editorMode, onModeSelected = actions.onModeSelected)
                Spacer(modifier = Modifier.height(14.dp))
                BulkPatchPanel(onUnlockAll = actions.onUnlockAll)
                Spacer(modifier = Modifier.height(16.dp))
                PatchEditorBody(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun PatchEditorBody(
    uiState: PatchEditorUiState,
    actions: PatchEditorActions,
) {
    if (uiState.editorMode == EditorMode.SIMPLIFIED) {
        EditorContentLabel()
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (uiState.editorMode == EditorMode.SIMPLIFIED) {
        SimplifiedEditor(
            draftValues = uiState.draftValues,
            originalData = uiState.currentData,
            onBooleanChanged = actions.onBooleanChanged,
            onTextChanged = actions.onTextChanged,
        )
    } else {
        RawEditor(rawJson = uiState.rawJson, onRawJsonChanged = actions.onRawJsonChanged)
    }

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(12.dp))
    PatchEditorStatus(errorMessage = uiState.errorMessage, statusMessage = uiState.statusMessage)
    PatchEditorFooter(
        isSaving = uiState.isSaving,
        onDismissRequest = actions.onDismissRequest,
        onExportRawJson = actions.onExportRawJson,
        onSave = actions.onSave,
    )
}

@Composable
private fun PatchEditorStatus(
    errorMessage: String?,
    statusMessage: String?,
) {
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (statusMessage != null) {
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
