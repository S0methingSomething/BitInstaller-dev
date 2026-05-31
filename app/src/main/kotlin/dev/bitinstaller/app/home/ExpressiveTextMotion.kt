package dev.bitinstaller.app.home

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun animateExpressiveFontWeight(
    isActive: Boolean,
    restWeight: Int = FontWeight.SemiBold.weight,
    activeWeight: Int = FontWeight.Black.weight,
): State<Int> =
    animateIntAsState(
        targetValue = if (isActive) activeWeight else restWeight,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "expressive_font_weight",
    )
