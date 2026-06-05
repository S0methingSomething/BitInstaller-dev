package dev.bitinstaller.app.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveFieldEdit

private const val SAVE_EDITOR_ROUTE_SLIDE_DIVISOR = 4
private const val SAVE_EDITOR_ROUTE_TARGETS = "save-targets"
private const val SAVE_EDITOR_ROUTE_SLOTS = "save-slots"
private const val SAVE_EDITOR_ROUTE_SLOT_EDITOR = "save-slot-editor"

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun SaveEditorNavigator(
    state: SaveEditorUiState,
    selectedSave: BitLifeSaveSummary?,
    actions: SaveEditorSectionActions,
    callbacks: SaveEditorNavigatorCallbacks,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(state.selectedTarget?.packageName, selectedSave?.path, currentBackStackEntry?.destination?.route) {
        val targetRoute = state.targetRoute(selectedSave)
        if (currentBackStackEntry?.destination?.route != targetRoute) {
            navController.navigateSaveEditorRoute(targetRoute)
        }
    }
    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        SaveEditorRouteGraph(
            navController = navController,
            state = state,
            selectedSave = selectedSave,
            actions = actions,
            callbacks = callbacks,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.SaveEditorRouteGraph(
    navController: NavHostController,
    state: SaveEditorUiState,
    selectedSave: BitLifeSaveSummary?,
    actions: SaveEditorSectionActions,
    callbacks: SaveEditorNavigatorCallbacks,
) {
    val spatialIntSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsFloatSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    NavHost(
        navController = navController,
        startDestination = SAVE_EDITOR_ROUTE_TARGETS,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(animationSpec = spatialIntSpec) { fullWidth ->
                fullWidth / SAVE_EDITOR_ROUTE_SLIDE_DIVISOR
            } +
                fadeIn(animationSpec = effectsFloatSpec)
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = spatialIntSpec) { fullWidth ->
                -fullWidth / SAVE_EDITOR_ROUTE_SLIDE_DIVISOR
            } +
                fadeOut(animationSpec = effectsFloatSpec)
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = spatialIntSpec) { fullWidth ->
                -fullWidth / SAVE_EDITOR_ROUTE_SLIDE_DIVISOR
            } +
                fadeIn(animationSpec = effectsFloatSpec)
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = spatialIntSpec) { fullWidth ->
                fullWidth / SAVE_EDITOR_ROUTE_SLIDE_DIVISOR
            } +
                fadeOut(animationSpec = effectsFloatSpec)
        },
    ) {
        composable(SAVE_EDITOR_ROUTE_TARGETS) {
            SaveEditorTargetRoute(state = state, actions = actions)
        }
        composable(SAVE_EDITOR_ROUTE_SLOTS) {
            SaveEditorSlotsRoute(
                state = state,
                actions = actions,
                callbacks = callbacks,
                transitionState = SaveSlotSharedTransitionState(this@SaveEditorRouteGraph, this),
            )
        }
        composable(SAVE_EDITOR_ROUTE_SLOT_EDITOR) {
            SaveEditorSlotRoute(
                state = state,
                selectedSave = selectedSave,
                actions = actions,
                callbacks = callbacks,
                transitionState = SaveSlotSharedTransitionState(this@SaveEditorRouteGraph, this),
            )
        }
    }
}

@Composable
private fun SaveEditorTargetRoute(
    state: SaveEditorUiState,
    actions: SaveEditorSectionActions,
) {
    SaveEditorTargetList(
        targets = state.targets,
        onTargetClick = actions.onTargetClick,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun SaveEditorSlotsRoute(
    state: SaveEditorUiState,
    actions: SaveEditorSectionActions,
    callbacks: SaveEditorNavigatorCallbacks,
    transitionState: SaveSlotSharedTransitionState,
) {
    val selectedTarget = state.selectedTarget
    if (selectedTarget == null) {
        SaveEditorTargetRoute(state = state, actions = actions)
        return
    }

    SaveTargetDetail(
        target = selectedTarget,
        actions =
            SaveTargetCardActions(
                onTargetClick = actions.onTargetClick,
                onSaveOpen = callbacks.onSaveOpen,
            ),
        onBackClick = actions.onBackClick,
        modifier = Modifier.fillMaxSize(),
        transitionState = transitionState,
    )
}

@Composable
private fun SaveEditorSlotRoute(
    state: SaveEditorUiState,
    selectedSave: BitLifeSaveSummary?,
    actions: SaveEditorSectionActions,
    callbacks: SaveEditorNavigatorCallbacks,
    transitionState: SaveSlotSharedTransitionState,
) {
    val selectedTarget = state.selectedTarget
    if (selectedTarget == null || selectedSave == null) {
        SaveEditorSlotsRoute(
            state = state,
            actions = actions,
            callbacks = callbacks,
            transitionState = transitionState,
        )
        return
    }

    SaveSlotEditorDetail(
        target = selectedTarget,
        save = selectedSave,
        actions =
            SaveSlotEditorDetailActions(
                onBackClick = callbacks.onSaveBackClick,
                onSaveChanges = { edits -> actions.onFieldEdits(selectedTarget, selectedSave, edits) },
                onSaveRevert = { callbacks.onSaveRevert(selectedSave) },
            ),
        modifier = Modifier.fillMaxSize(),
        transitionState = transitionState,
    )
}

private fun SaveEditorUiState.targetRoute(selectedSave: BitLifeSaveSummary?): String =
    when {
        selectedTarget == null -> SAVE_EDITOR_ROUTE_TARGETS
        selectedSave == null -> SAVE_EDITOR_ROUTE_SLOTS
        else -> SAVE_EDITOR_ROUTE_SLOT_EDITOR
    }

private fun NavHostController.navigateSaveEditorRoute(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(SAVE_EDITOR_ROUTE_TARGETS) { saveState = true }
    }
}

internal data class SaveEditorSectionActions(
    val onTargetClick: (SaveTargetUiState) -> Unit,
    val onFieldEdits: (SaveTargetUiState, BitLifeSaveSummary, List<SaveFieldEdit>) -> Unit,
    val onSaveRevert: (SaveTargetUiState, BitLifeSaveSummary) -> Unit,
    val onBackClick: () -> Unit,
)

internal data class SaveEditorNavigatorCallbacks(
    val onSaveOpen: (BitLifeSaveSummary) -> Unit,
    val onSaveBackClick: () -> Unit,
    val onSaveRevert: (BitLifeSaveSummary) -> Unit,
)
