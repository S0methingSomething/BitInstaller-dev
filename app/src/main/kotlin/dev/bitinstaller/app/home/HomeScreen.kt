package dev.bitinstaller.app.home

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val HOME_NAV_EXIT_SLIDE_DIVISOR = 2
private const val HOME_CHROME_SLIDE_DIVISOR = 3

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeRoute(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    activeSession: PatchEditorSession? = null,
    liveDictionaryPrompt: LiveDictionaryPromptUiState? = null,
    callbacks: HomeRouteCallbacks = HomeRouteCallbacks(),
) {
    PredictiveBackHandler(enabled = activeSession != null) { progress ->
        progress.collect { /* allow system animation */ }
        callbacks.onDismissSession()
    }

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            HomeBackground(
                activeSession = activeSession,
                state = state,
                callbacks = callbacks,
                sharedTransitionScope = this@SharedTransitionLayout,
            )
            PatchEditorOverlay(
                activeSession = activeSession,
                callbacks = callbacks,
                sharedTransitionScope = this@SharedTransitionLayout,
            )

            liveDictionaryPrompt?.let { prompt ->
                LiveDictionaryPrompt(
                    prompt = prompt,
                    onDismissRequest = callbacks.onDismissLiveDictionaryPrompt,
                    onConfirm = callbacks.onConfirmLiveDictionaryFix,
                )
            }
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

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun HomeContent(
    state: HomeUiState,
    callbacks: HomeRouteCallbacks,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val navigationManager = rememberHomeNavigationManager(state.selectedDestination)
    val isFocusedSaveEditor = navigationManager.selectedDestination == BitInstallerDestination.SaveEditor
    val effectsFloatSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatialIntSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    Box(modifier = Modifier.fillMaxSize()) {
        HomeAmbientGlow()
        DestinationPane(
            navigationManager = navigationManager,
            state = state,
            isFocusedSaveEditor = isFocusedSaveEditor,
            callbacks = callbacks,
            sharedTransitionScope = sharedTransitionScope,
        )

        AnimatedVisibility(
            visible = !isFocusedSaveEditor,
            enter =
                fadeIn(animationSpec = effectsFloatSpec) +
                    slideInVertically(animationSpec = spatialIntSpec) { it / HOME_NAV_EXIT_SLIDE_DIVISOR },
            exit =
                fadeOut(animationSpec = effectsFloatSpec) +
                    slideOutVertically(animationSpec = spatialIntSpec) { it / HOME_NAV_EXIT_SLIDE_DIVISOR },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
        ) {
            HomeBottomNavigation(
                selectedDestination = navigationManager.selectedDestination,
                onDestinationSelected = { destination ->
                    navigationManager.navigateTo(destination, callbacks.onDestinationSelected)
                },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun DestinationPane(
    navigationManager: HomeNavigationManager,
    state: HomeUiState,
    isFocusedSaveEditor: Boolean,
    callbacks: HomeRouteCallbacks,
    sharedTransitionScope: SharedTransitionScope?,
) {
    val effectsFloatSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatialIntSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val paneModifier =
        if (isFocusedSaveEditor) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        }

    Column(modifier = paneModifier) {
        AnimatedVisibility(
            visible = !isFocusedSaveEditor,
            enter =
                fadeIn(animationSpec = effectsFloatSpec) +
                    slideInVertically(animationSpec = spatialIntSpec) { -it / HOME_CHROME_SLIDE_DIVISOR },
            exit =
                fadeOut(animationSpec = effectsFloatSpec) +
                    slideOutVertically(animationSpec = spatialIntSpec) { -it / HOME_CHROME_SLIDE_DIVISOR },
        ) {
            Column {
                HomeHeader(state = state)
                Spacer(modifier = Modifier.height(12.dp))
                DashboardSection(
                    status = state.backendStatus,
                    onActionClick = callbacks.onDashboardActionClick,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Box(
            modifier = Modifier.weight(1f),
        ) {
            HomeDestinationHost(
                navigationManager = navigationManager,
                state = state,
                callbacks = callbacks,
                sharedTransitionScope = sharedTransitionScope,
            )
        }
    }
}

@Composable
private fun HomeHeader(state: HomeUiState) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start,
            )
            Text(
                text = state.summary.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
        }
        HomeBeacon(modifier = Modifier.size(36.dp))
    }
}
