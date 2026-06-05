package dev.bitinstaller.app.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.bitinstaller.app.save.BitLifeSaveSummary

internal data class SaveSlotSharedTransitionState(
    val sharedTransitionScope: SharedTransitionScope? = null,
    val animatedVisibilityScope: AnimatedVisibilityScope? = null,
)

private fun saveSlotSharedKey(savePath: String): String = "save-slot-$savePath"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.saveSlotSharedBounds(
    save: BitLifeSaveSummary,
    transitionState: SaveSlotSharedTransitionState,
): Modifier =
    if (transitionState.sharedTransitionScope != null && transitionState.animatedVisibilityScope != null) {
        with(transitionState.sharedTransitionScope) {
            this@saveSlotSharedBounds.sharedBounds(
                sharedContentState = rememberSharedContentState(key = saveSlotSharedKey(save.path)),
                animatedVisibilityScope = transitionState.animatedVisibilityScope,
            )
        }
    } else {
        this
    }
