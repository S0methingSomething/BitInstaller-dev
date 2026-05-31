package dev.bitinstaller.app.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.bitinstaller.app.save.BitLifeSaveSummary

internal data class SaveEditorSharedTransitionState(
    val sharedTransitionScope: SharedTransitionScope?,
    val animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    companion object {
        val Empty = SaveEditorSharedTransitionState(sharedTransitionScope = null, animatedVisibilityScope = null)
    }
}

internal fun saveTargetSharedKey(packageName: String): String = "save-target-$packageName"

private fun saveSlotSharedKey(path: String): String = "save-slot-$path"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.saveTargetSharedBounds(
    target: SaveTargetUiState,
    transitionState: SaveEditorSharedTransitionState,
): Modifier = withTransitionState(transitionState, saveTargetSharedKey(target.packageName))

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.saveSlotSharedBounds(
    save: BitLifeSaveSummary,
    transitionState: SaveEditorSharedTransitionState,
): Modifier = withTransitionState(transitionState, saveSlotSharedKey(save.path))

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.withTransitionState(
    transitionState: SaveEditorSharedTransitionState,
    sharedKey: String,
): Modifier {
    val sharedTransitionScope = transitionState.sharedTransitionScope
    val animatedVisibilityScope = transitionState.animatedVisibilityScope
    return if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            this@withTransitionState.sharedBounds(
                sharedContentState = rememberSharedContentState(key = sharedKey),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        this
    }
}
