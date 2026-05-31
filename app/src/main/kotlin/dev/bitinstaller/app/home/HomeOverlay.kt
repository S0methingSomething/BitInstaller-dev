package dev.bitinstaller.app.home

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

private val PATCH_EDITOR_BACKGROUND_BLUR = 18.dp
private const val SLIDE_IN_OFFSET_DIVISOR = 4
private const val SLIDE_OUT_OFFSET_DIVISOR = 2
private const val PATCH_EDITOR_SCRIM_ALPHA = 0.52f

@Composable
internal fun HomeBackground(
    activeSession: PatchEditorSession?,
    state: HomeUiState,
    callbacks: HomeRouteCallbacks,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    Box(modifier = Modifier.fillMaxSize().patchEditorBlur(activeSession != null)) {
        HomeContent(state = state, callbacks = callbacks, sharedTransitionScope = sharedTransitionScope)
    }
}

@Composable
private fun Modifier.patchEditorBlur(isBlurred: Boolean): Modifier {
    val backgroundBlurPx = with(LocalDensity.current) { PATCH_EDITOR_BACKGROUND_BLUR.toPx() }
    return if (isBlurred) {
        graphicsLayer {
            renderEffect =
                RenderEffect
                    .createBlurEffect(backgroundBlurPx, backgroundBlurPx, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()
        }
    } else {
        this
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PatchEditorOverlay(
    activeSession: PatchEditorSession?,
    callbacks: HomeRouteCallbacks,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val effectsFloatSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatialIntSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    AnimatedVisibility(
        visible = activeSession != null,
        enter =
            fadeIn(animationSpec = effectsFloatSpec) +
                slideInVertically(animationSpec = spatialIntSpec) { it / SLIDE_IN_OFFSET_DIVISOR },
        exit =
            fadeOut(animationSpec = effectsFloatSpec) +
                slideOutVertically(animationSpec = spatialIntSpec) { it / SLIDE_OUT_OFFSET_DIVISOR },
    ) {
        activeSession?.let { session ->
            PatchEditorScrim(
                session = session,
                callbacks = callbacks,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = this,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PatchEditorScrim(
    session: PatchEditorSession,
    callbacks: HomeRouteCallbacks,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val sharedBoundsModifier =
        if (sharedTransitionScope != null) {
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = patchEditorSharedKey(session.packageName)),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        } else {
            Modifier
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = PATCH_EDITOR_SCRIM_ALPHA)),
    ) {
        PatchEditorScene(
            target = session.target,
            contentAlpha = 1f,
            onDismissRequest = callbacks.onDismissSession,
            modifier = sharedBoundsModifier,
            config =
                PatchEditorSceneConfig(
                    initialData = session.initialData,
                    saveData = { data -> callbacks.onSaveSession(session, data) },
                ),
        )
    }
}
