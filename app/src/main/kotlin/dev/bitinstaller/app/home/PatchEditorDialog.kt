package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.crypto.MonetizationCodec
import dev.bitinstaller.app.crypto.MonetizationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PATCH_EDITOR_CONTAINER_COLOR_ARGB = 0xF5050505
private const val PATCH_EDITOR_HEIGHT_FRACTION = 0.88f
private val PatchEditorShape = RoundedCornerShape(24.dp)

data class PatchEditorSceneConfig(
    val initialData: MonetizationData = buildPreviewData(),
    val saveData: suspend (MonetizationData) -> String = { data ->
        val length = withContext(Dispatchers.Default) { MonetizationCodec.encrypt(data).length }
        "Saved and re-encrypted file ($length chars)."
    },
)

@Composable
fun PatchEditorScene(
    target: PatchTargetUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
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
        uiState = uiState,
        actions =
            rememberPatchEditorActions(
                uiState = uiState,
                mutations = mutations,
                onDismissRequest = onDismissRequest,
                config = config,
            ),
        modifier = modifier,
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
    uiState: PatchEditorUiState,
    actions: PatchEditorActions,
    modifier: Modifier = Modifier,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val maxScreenHeight =
        remember(windowInfo.containerSize.height, density) {
            with(density) {
                (windowInfo.containerSize.height * PATCH_EDITOR_HEIGHT_FRACTION).toDp()
            }
        }
    val maxHeight =
        remember(maxScreenHeight) {
            if (maxScreenHeight > 760.dp) 760.dp else maxScreenHeight
        }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        Surface(
            shape = PatchEditorShape,
            tonalElevation = 0.dp,
            color = Color(PATCH_EDITOR_CONTAINER_COLOR_ARGB),
            modifier =
                modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PatchEditorHeaderSection(uiState = uiState, actions = actions)
                PatchEditorBodySection(
                    uiState = uiState,
                    actions = actions,
                    modifier = Modifier.weight(1f, fill = false),
                )
                PatchEditorFooter(
                    isSaving = uiState.isSaving,
                    onDismissRequest = actions.onDismissRequest,
                    onExportRawJson = actions.onExportRawJson,
                    onSave = actions.onSave,
                )
            }
        }
    }
}

@Composable
private fun PatchEditorHeaderSection(
    uiState: PatchEditorUiState,
    actions: PatchEditorActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        PatchEditorToolbar(editorMode = uiState.editorMode, onModeSelected = actions.onModeSelected)
        BulkPatchPanel(onUnlockAll = actions.onUnlockAll)
    }
}

@Composable
private fun PatchEditorBodySection(
    uiState: PatchEditorUiState,
    actions: PatchEditorActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
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
    }
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
