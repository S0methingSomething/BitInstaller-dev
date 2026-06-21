package dev.bitinstaller.app.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.patchTargetSharedBounds(
    target: PatchTargetUiState,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier =
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            this@patchTargetSharedBounds.sharedBounds(
                sharedContentState = rememberSharedContentState(key = patchEditorSharedKey(target.packageName)),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        this
    }
