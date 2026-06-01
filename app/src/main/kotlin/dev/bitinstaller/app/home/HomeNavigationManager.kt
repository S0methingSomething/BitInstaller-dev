package dev.bitinstaller.app.home

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private const val SAVE_ROUTE_SLIDE_DIVISOR = 4

internal data class HomeNavigationManager(
    val navController: NavHostController,
    val selectedDestination: BitInstallerDestination,
) {
    fun navigateTo(
        destination: BitInstallerDestination,
        onDestinationSelected: (BitInstallerDestination) -> Unit,
    ) {
        onDestinationSelected(destination)
        navController.navigateToDestination(destination)
    }
}

@Composable
internal fun rememberHomeNavigationManager(selectedDestination: BitInstallerDestination): HomeNavigationManager {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(selectedDestination, currentRoute) {
        val targetRoute = selectedDestination.route
        if (currentRoute != null && currentRoute != targetRoute) {
            navController.navigateToDestination(selectedDestination)
        }
    }

    return HomeNavigationManager(
        navController = navController,
        selectedDestination = currentRoute.toBitInstallerDestination() ?: selectedDestination,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeDestinationHost(
    navigationManager: HomeNavigationManager,
    state: HomeUiState,
    callbacks: HomeRouteCallbacks,
    sharedTransitionScope: SharedTransitionScope?,
) {
    val effectsDestinationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatialDestinationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    NavHost(
        navController = navigationManager.navController,
        startDestination = BitInstallerDestination.MonetizationVars.route,
        enterTransition = {
            if (targetState.destination.route == BitInstallerDestination.SaveEditor.route) {
                slideInVertically(animationSpec = spatialDestinationSpec) { it / SAVE_ROUTE_SLIDE_DIVISOR } +
                    fadeIn(animationSpec = effectsDestinationSpec)
            } else {
                fadeIn(animationSpec = effectsDestinationSpec)
            }
        },
        exitTransition = {
            if (initialState.destination.route == BitInstallerDestination.SaveEditor.route) {
                slideOutVertically(animationSpec = spatialDestinationSpec) { it / SAVE_ROUTE_SLIDE_DIVISOR } +
                    fadeOut(animationSpec = effectsDestinationSpec)
            } else {
                fadeOut(animationSpec = effectsDestinationSpec)
            }
        },
        popEnterTransition = { fadeIn(animationSpec = effectsDestinationSpec) },
        popExitTransition = {
            if (initialState.destination.route == BitInstallerDestination.SaveEditor.route) {
                slideOutVertically(animationSpec = spatialDestinationSpec) { it / SAVE_ROUTE_SLIDE_DIVISOR } +
                    fadeOut(animationSpec = effectsDestinationSpec)
            } else {
                fadeOut(animationSpec = effectsDestinationSpec)
            }
        },
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

private fun NavHostController.navigateToDestination(destination: BitInstallerDestination) {
    navigate(destination.route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(BitInstallerDestination.MonetizationVars.route) {
            saveState = true
        }
    }
}

private fun String?.toBitInstallerDestination(): BitInstallerDestination? =
    BitInstallerDestination.entries.firstOrNull { destination -> destination.route == this }
