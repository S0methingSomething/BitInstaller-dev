package dev.bitinstaller.app.home

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

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
    modifier: Modifier = Modifier,
) {
    val effectsDestinationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    NavHost(
        navController = navigationManager.navController,
        startDestination = BitInstallerDestination.MonetizationVars.route,
        modifier = modifier,
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
                actions =
                    SaveEditorSectionActions(
                        onTargetClick = callbacks.onSaveTargetClick,
                        onFieldEdits = callbacks.onSaveFieldEdits,
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
