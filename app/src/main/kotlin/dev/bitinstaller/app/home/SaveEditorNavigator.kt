package dev.bitinstaller.app.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveFieldEdit

private const val SAVE_EDITOR_ROUTE_TARGETS = "save-targets"
private const val SAVE_EDITOR_ROUTE_SLOTS = "save-slots"
private const val SAVE_EDITOR_ROUTE_SLOT_EDITOR = "save-slot-editor"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SaveEditorNavigator(
    state: SaveEditorUiState,
    selectedSave: BitLifeSaveSummary?,
    actions: SaveEditorSectionActions,
    callbacks: SaveEditorNavigatorCallbacks,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    BackHandler(enabled = state.selectedTarget != null) {
        when {
            selectedSave != null -> {
                callbacks.onSaveBackClick()
                navController.popBackStack(SAVE_EDITOR_ROUTE_SLOTS, inclusive = false)
            }

            else -> {
                actions.onBackClick()
                navController.popBackStack(SAVE_EDITOR_ROUTE_TARGETS, inclusive = false)
            }
        }
    }
    SaveEditorRouteGraph(
        routeState = SaveEditorRouteState(navController, state, selectedSave, actions, callbacks),
        modifier = modifier,
    )
}

private data class SaveEditorRouteState(
    val navController: NavHostController,
    val state: SaveEditorUiState,
    val selectedSave: BitLifeSaveSummary?,
    val actions: SaveEditorSectionActions,
    val callbacks: SaveEditorNavigatorCallbacks,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SaveEditorRouteGraph(
    routeState: SaveEditorRouteState,
    modifier: Modifier = Modifier,
) {
    val spatialIntSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsFloatSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    NavHost(
        navController = routeState.navController,
        startDestination = SAVE_EDITOR_ROUTE_TARGETS,
        modifier = modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(animationSpec = spatialIntSpec) { fullWidth ->
                fullWidth / BitInstallerAnimations.ROUTE_SLIDE_DIVISOR
            } +
                fadeIn(animationSpec = effectsFloatSpec)
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = spatialIntSpec) { fullWidth ->
                -fullWidth / BitInstallerAnimations.ROUTE_SLIDE_DIVISOR
            } +
                fadeOut(animationSpec = effectsFloatSpec)
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = spatialIntSpec) { fullWidth ->
                -fullWidth / BitInstallerAnimations.ROUTE_SLIDE_DIVISOR
            } +
                fadeIn(animationSpec = effectsFloatSpec)
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = spatialIntSpec) { fullWidth ->
                fullWidth / BitInstallerAnimations.ROUTE_SLIDE_DIVISOR
            } +
                fadeOut(animationSpec = effectsFloatSpec)
        },
    ) {
        saveEditorRoutes(routeState)
    }
}

private fun NavGraphBuilder.saveEditorRoutes(routeState: SaveEditorRouteState) {
    composable(SAVE_EDITOR_ROUTE_TARGETS) {
        SaveEditorTargetRoute(
            state = routeState.state,
            onTargetClick = { target ->
                routeState.actions.onTargetClick(target)
                routeState.navController.navigateSaveEditorRoute(SAVE_EDITOR_ROUTE_SLOTS)
            },
        )
    }
    composable(SAVE_EDITOR_ROUTE_SLOTS) {
        SaveEditorSlotsRoute(
            state = routeState.state,
            actions = routeState.actions,
            onBackClick = {
                routeState.actions.onBackClick()
                routeState.navController.popBackStack(SAVE_EDITOR_ROUTE_TARGETS, inclusive = false)
            },
            onSaveOpen = { save ->
                routeState.callbacks.onSaveOpen(save)
                routeState.navController.navigateSaveEditorRoute(SAVE_EDITOR_ROUTE_SLOT_EDITOR)
            },
        )
    }
    composable(SAVE_EDITOR_ROUTE_SLOT_EDITOR) {
        SaveEditorSlotRoute(
            state = routeState.state,
            selectedSave = routeState.selectedSave,
            actions = routeState.actions,
            callbacks = routeState.callbacks,
            onBackClick = {
                routeState.callbacks.onSaveBackClick()
                routeState.navController.popBackStack(SAVE_EDITOR_ROUTE_SLOTS, inclusive = false)
            },
        )
    }
}

@Composable
private fun SaveEditorTargetRoute(
    state: SaveEditorUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
) {
    SaveEditorTargetList(
        targets = state.targets,
        onTargetClick = onTargetClick,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun SaveEditorSlotsRoute(
    state: SaveEditorUiState,
    actions: SaveEditorSectionActions,
    onBackClick: () -> Unit,
    onSaveOpen: (BitLifeSaveSummary) -> Unit,
) {
    val selectedTarget = state.selectedTarget
    if (selectedTarget == null) {
        SaveEditorTargetRoute(state = state, onTargetClick = actions.onTargetClick)
        return
    }

    SaveTargetDetail(
        target = selectedTarget,
        actions =
            SaveTargetCardActions(
                onTargetClick = actions.onTargetClick,
                onSaveOpen = onSaveOpen,
            ),
        onBackClick = onBackClick,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun SaveEditorSlotRoute(
    state: SaveEditorUiState,
    selectedSave: BitLifeSaveSummary?,
    actions: SaveEditorSectionActions,
    callbacks: SaveEditorNavigatorCallbacks,
    onBackClick: () -> Unit,
) {
    val selectedTarget = state.selectedTarget
    if (selectedTarget == null || selectedSave == null) {
        SaveEditorSlotsRoute(
            state = state,
            actions = actions,
            onBackClick = actions.onBackClick,
            onSaveOpen = callbacks.onSaveOpen,
        )
        return
    }

    SaveSlotEditorDetail(
        target = selectedTarget,
        save = selectedSave,
        actions =
            SaveSlotEditorDetailActions(
                onBackClick = onBackClick,
                onSaveChanges = { edits -> actions.onFieldEdits(selectedTarget, selectedSave, edits) },
                onSaveRevert = { callbacks.onSaveRevert(selectedSave) },
                onLoadAdvancedFields = { actions.onLoadAdvancedFields(selectedTarget.packageName, selectedSave) },
            ),
        modifier = Modifier.fillMaxSize(),
    )
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
    val onLoadAdvancedFields: (String, BitLifeSaveSummary) -> Unit,
    val onBackClick: () -> Unit,
)

internal data class SaveEditorNavigatorCallbacks(
    val onSaveOpen: (BitLifeSaveSummary) -> Unit,
    val onSaveBackClick: () -> Unit,
    val onSaveRevert: (BitLifeSaveSummary) -> Unit,
)
