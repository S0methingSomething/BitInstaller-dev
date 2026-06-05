package dev.bitinstaller.app.home

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal data class SaveEditorModalState(
    val selectedTarget: SaveTargetUiState?,
    val selectedSavePath: String?,
    val revertSave: dev.bitinstaller.app.save.BitLifeSaveSummary?,
)

internal data class SaveEditorModalActions(
    val closeRevert: () -> Unit,
    val confirmRevert: (SaveTargetUiState, dev.bitinstaller.app.save.BitLifeSaveSummary) -> Unit,
    val backToSaves: () -> Unit,
    val backToTargets: () -> Unit,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun rememberSaveEditorBackHandler(
    state: SaveEditorModalState,
    actions: SaveEditorModalActions,
): Animatable<Float, *> {
    val scope = rememberCoroutineScope()
    val backProgress = remember { Animatable(0f) }
    val spatialFloatSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val enabled = state.hasVisibleModalOrDetail()
    PredictiveBackHandler(enabled = enabled) { backEvent ->
        scope.launch { backEvent.collectLatest { backProgress.snapTo(it.progress) } }
        scope.launch { backProgress.animateTo(0f, animationSpec = spatialFloatSpec) }
        when {
            state.revertSave != null -> actions.closeRevert()
            state.selectedSavePath != null -> actions.backToSaves()
            state.selectedTarget != null -> actions.backToTargets()
        }
    }
    return backProgress
}

@Composable
internal fun SaveEditorModals(
    state: SaveEditorModalState,
    actions: SaveEditorModalActions,
) {
    state.revertSave?.let { save ->
        SaveRevertDialog(
            save = save,
            target = state.selectedTarget,
            onDismissRequest = actions.closeRevert,
            onConfirm = actions.confirmRevert,
        )
    }
}

private fun SaveEditorModalState.hasVisibleModalOrDetail(): Boolean =
    revertSave != null ||
        selectedSavePath != null ||
        selectedTarget != null
