package dev.bitinstaller.app.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

private const val OVERLAY_ENTER_DURATION_MS = 250
private const val OVERLAY_EXIT_DURATION_MS = 200
private const val DESTINATION_CROSSFADE_DURATION_MS = 300
private const val SLIDE_IN_OFFSET_DIVISOR = 4
private const val SLIDE_OUT_OFFSET_DIVISOR = 2

@Composable
fun HomeRoute(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    activeSession: PatchEditorSession? = null,
    liveDictionaryPrompt: LiveDictionaryPromptUiState? = null,
    callbacks: HomeRouteCallbacks = HomeRouteCallbacks(),
) {
    BackHandler(enabled = activeSession != null) {
        callbacks.onDismissSession()
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeContent(state = state, callbacks = callbacks)

        AnimatedVisibility(
            visible = activeSession != null,
            enter =
                fadeIn(animationSpec = tween(durationMillis = OVERLAY_ENTER_DURATION_MS)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = OVERLAY_ENTER_DURATION_MS),
                    ) { it / SLIDE_IN_OFFSET_DIVISOR },
            exit =
                fadeOut(animationSpec = tween(durationMillis = OVERLAY_EXIT_DURATION_MS)) +
                    slideOutVertically(
                        animationSpec = tween(durationMillis = OVERLAY_EXIT_DURATION_MS),
                    ) { it / SLIDE_OUT_OFFSET_DIVISOR },
        ) {
            activeSession?.let { session ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.52f)),
                ) {
                    PatchEditorScene(
                        target = session.target,
                        contentAlpha = 1f,
                        onDismissRequest = callbacks.onDismissSession,
                        config =
                            PatchEditorSceneConfig(
                                initialData = session.initialData,
                                saveData = { data -> callbacks.onSaveSession(session, data) },
                            ),
                    )
                }
            }
        }

        liveDictionaryPrompt?.let { prompt ->
            LiveDictionaryPrompt(
                prompt = prompt,
                onDismissRequest = callbacks.onDismissLiveDictionaryPrompt,
                onConfirm = callbacks.onConfirmLiveDictionaryFix,
            )
        }
    }
}

@Composable
private fun LiveDictionaryPrompt(
    prompt: LiveDictionaryPromptUiState,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = prompt.title) },
        text = { Text(text = prompt.message) },
        confirmButton = {
            Button(onClick = onConfirm) { Text(text = prompt.confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(text = "Cancel") }
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    callbacks: HomeRouteCallbacks,
) {
    val isFocusedSaveEditor =
        state.selectedDestination == BitInstallerDestination.SaveEditor &&
            state.saveEditor.selectedTarget != null

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val navigationType =
        if (!adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            NavigationSuiteType.NavigationBar
        } else {
            NavigationSuiteType.NavigationRail
        }

    NavigationSuiteScaffold(
        layoutType = navigationType,
        navigationSuiteItems = {
            item(
                selected = state.selectedDestination == BitInstallerDestination.MonetizationVars,
                onClick = {
                    callbacks.onDestinationSelected(BitInstallerDestination.MonetizationVars)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "MonetizationVars",
                    )
                },
                label = { Text(text = "Patches") },
            )
            item(
                selected = state.selectedDestination == BitInstallerDestination.SaveEditor,
                onClick = {
                    callbacks.onDestinationSelected(BitInstallerDestination.SaveEditor)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Save Editor",
                    )
                },
                label = { Text(text = "Saves") },
            )
        },
    ) {
        DestinationPane(
            state = state,
            isFocusedSaveEditor = isFocusedSaveEditor,
            callbacks = callbacks,
        )
    }
}

@Composable
private fun DestinationPane(
    state: HomeUiState,
    isFocusedSaveEditor: Boolean,
    callbacks: HomeRouteCallbacks,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        if (!isFocusedSaveEditor) {
            HeroSection(state = state)
            Spacer(modifier = Modifier.height(8.dp))
            DashboardSection(
                status = state.backendStatus,
                onActionClick = callbacks.onDashboardActionClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Crossfade(
            targetState = state.selectedDestination,
            animationSpec = tween(durationMillis = DESTINATION_CROSSFADE_DURATION_MS),
            label = "destination",
            modifier = Modifier.weight(1f),
        ) { destination ->
            when (destination) {
                BitInstallerDestination.MonetizationVars -> {
                    PatchTargetsSection(
                        targets = state.patchTargets,
                        onPatchClick = callbacks.onPatchClick,
                    )
                }

                BitInstallerDestination.SaveEditor -> {
                    SaveEditorSection(
                        state = state.saveEditor,
                        onTargetClick = callbacks.onSaveTargetClick,
                        onFieldEdit = callbacks.onSaveFieldEdit,
                        onSaveRevert = callbacks.onSaveRevert,
                        onBackClick = callbacks.onSaveEditorBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSection(state: HomeUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
    ) {
        Text(
            text = state.title,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = state.summary,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
