package dev.bitinstaller.app.home

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

internal object BitInstallerAnimations {
    const val ROUTE_SLIDE_DIVISOR = 4

    private const val LIST_PLACEMENT_STIFFNESS = 400f
    private const val LIST_PLACEMENT_DAMPING_RATIO = 0.8f

    fun <T> listPlacementSpec(): FiniteAnimationSpec<T> =
        spring(stiffness = LIST_PLACEMENT_STIFFNESS, dampingRatio = LIST_PLACEMENT_DAMPING_RATIO)
}
