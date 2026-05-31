package dev.bitinstaller.app.home

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class HomeNavigationState(
    val navController: NavHostController,
    val selectedDestination: BitInstallerDestination,
)

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
internal fun HomeContent(
    state: HomeUiState,
    callbacks: HomeRouteCallbacks,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val isFocusedSaveEditor =
        state.selectedDestination == BitInstallerDestination.SaveEditor &&
            state.saveEditor.selectedTarget != null

    val navigationState = rememberHomeNavigationState(state.selectedDestination)

    Box(modifier = Modifier.fillMaxSize()) {
        HomeAmbientGlow()
        DestinationPane(
            navController = navigationState.navController,
            state = state,
            isFocusedSaveEditor = isFocusedSaveEditor,
            callbacks = callbacks,
            sharedTransitionScope = sharedTransitionScope,
        )

        HomeBottomNavigation(
            selectedDestination = navigationState.selectedDestination,
            onDestinationSelected = { destination ->
                callbacks.onDestinationSelected(destination)
                navigationState.navController.navigateToDestination(destination)
            },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun rememberHomeNavigationState(selectedDestination: BitInstallerDestination): HomeNavigationState {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(selectedDestination, currentRoute) {
        val targetRoute = selectedDestination.route
        if (currentRoute != null && currentRoute != targetRoute) {
            navController.navigateToDestination(selectedDestination)
        }
    }

    return HomeNavigationState(
        navController = navController,
        selectedDestination = currentRoute.toBitInstallerDestination() ?: selectedDestination,
    )
}

private fun NavHostController.navigateToDestination(destination: BitInstallerDestination) {
    navigate(destination.route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(BitInstallerDestination.MonetizationVars.route) {
            saveState = true
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DestinationPane(
    navController: NavHostController,
    state: HomeUiState,
    isFocusedSaveEditor: Boolean,
    callbacks: HomeRouteCallbacks,
    sharedTransitionScope: SharedTransitionScope?,
) {
    val effectsDestinationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
    ) {
        if (!isFocusedSaveEditor) {
            HomeHeader(state = state)
            Spacer(modifier = Modifier.height(12.dp))
            DashboardSection(
                status = state.backendStatus,
                onActionClick = callbacks.onDashboardActionClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        NavHost(
            navController = navController,
            startDestination = BitInstallerDestination.MonetizationVars.route,
            modifier = Modifier.weight(1f),
            enterTransition = { fadeIn(animationSpec = effectsDestinationSpec) },
            exitTransition = { fadeOut(animationSpec = effectsDestinationSpec) },
            popEnterTransition = { fadeIn(animationSpec = effectsDestinationSpec) },
            popExitTransition = { fadeOut(animationSpec = effectsDestinationSpec) },
        ) {
            composable(BitInstallerDestination.MonetizationVars.route) {
                PatchTargetsSection(
                    targets = state.patchTargets,
                    onPatchClick = callbacks.onPatchClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = this,
                )
            }

            composable(BitInstallerDestination.SaveEditor.route) {
                SaveEditorSection(
                    state = state.saveEditor,
                    sharedTransitionState =
                        SaveEditorSharedTransitionState(
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = this,
                        ),
                    actions =
                        SaveEditorSectionActions(
                            onTargetClick = callbacks.onSaveTargetClick,
                            onFieldEdit = callbacks.onSaveFieldEdit,
                            onSaveRevert = callbacks.onSaveRevert,
                            onBackClick = callbacks.onSaveEditorBack,
                        ),
                )
            }
        }
    }
}

private fun String?.toBitInstallerDestination(): BitInstallerDestination? =
    BitInstallerDestination.entries.firstOrNull { destination -> destination.route == this }

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
