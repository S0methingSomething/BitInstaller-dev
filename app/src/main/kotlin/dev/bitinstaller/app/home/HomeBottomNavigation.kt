package dev.bitinstaller.app.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val HOME_NAV_CONTAINER_COLOR_ARGB = 0xE6101012
private const val HOME_NAV_BORDER_COLOR_ARGB = 0x24FFFFFF
private const val HOME_NAV_UNSELECTED_TEXT_ALPHA = 0.42f
private val HomeNavContainerColor = Color(HOME_NAV_CONTAINER_COLOR_ARGB)
private val HomeNavBorderColor = Color(HOME_NAV_BORDER_COLOR_ARGB)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeBottomNavigation(
    selectedDestination: BitInstallerDestination,
    onDestinationSelected: (BitInstallerDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(HomeNavContainerColor, shape = CircleShape)
                .border(width = 1.dp, color = HomeNavBorderColor, shape = CircleShape)
                .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BitInstallerDestination.entries.forEach { destination ->
            HomeBottomNavigationItem(
                destination = destination,
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeBottomNavigationItem(
    destination: BitInstallerDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "home_nav_bg",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.White.copy(alpha = HOME_NAV_UNSELECTED_TEXT_ALPHA),
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "home_nav_content",
    )
    val textWeight by animateExpressiveFontWeight(
        isActive = selected,
        restWeight = FontWeight.Medium.weight,
        activeWeight = FontWeight.Black.weight,
    )

    Text(
        text = destination.label,
        color = contentColor,
        fontSize = 13.sp,
        fontWeight = FontWeight(textWeight),
        modifier =
            Modifier
                .clip(CircleShape)
                .background(backgroundColor, CircleShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 12.dp),
    )
}
