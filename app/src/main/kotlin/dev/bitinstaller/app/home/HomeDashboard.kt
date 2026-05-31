package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DashboardShape = RoundedCornerShape(24.dp)
private val DashboardActionShape = RoundedCornerShape(12.dp)
private const val DASHBOARD_CONTAINER_COLOR_ARGB = 0x0EFFFFFF
private const val DASHBOARD_QUIET_ACTION_COLOR_ARGB = 0x08FFFFFF
private const val DASHBOARD_EYEBROW_ALPHA = 0.42f
private const val DASHBOARD_BODY_ALPHA = 0.52f
private const val DASHBOARD_MARK_CONTAINER_ALPHA = 0.08f
private const val DASHBOARD_MARK_ACCENT_ALPHA = 0.16f
private val DashboardContainerColor = Color(DASHBOARD_CONTAINER_COLOR_ARGB)
private val DashboardQuietActionColor = Color(DASHBOARD_QUIET_ACTION_COLOR_ARGB)

@Composable
internal fun DashboardSection(
    status: BackendStatus,
    onActionClick: () -> Unit,
) {
    val card = dashboardCardState(status)

    Surface(
        shape = DashboardShape,
        color = DashboardContainerColor,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        DashboardCardBody(card = card, onActionClick = onActionClick)
    }
}

@Composable
private fun DashboardCardBody(
    card: DashboardCardState,
    onActionClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ShizukuMark(accent = card.accent)
            DashboardTextBlock(card = card)
        }

        DashboardActionButton(card = card, onActionClick = onActionClick)
    }
}

@Composable
private fun DashboardActionButton(
    card: DashboardCardState,
    onActionClick: () -> Unit,
) {
    val isSolid = !card.isQuietAction

    Button(
        onClick = onActionClick,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (isSolid) Color.White else DashboardQuietActionColor,
                contentColor = if (isSolid) Color.Black else Color.White,
            ),
        shape = DashboardActionShape,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        Icon(imageVector = card.icon, contentDescription = card.action)
        Text(text = card.action)
    }
}

@Composable
private fun DashboardTextBlock(card: DashboardCardState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = card.eyebrow.uppercase(),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                ),
            color = Color.White.copy(alpha = DASHBOARD_EYEBROW_ALPHA),
        )
        Text(
            text = card.headline,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = card.description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = DASHBOARD_BODY_ALPHA),
        )
        StatusLine(label = card.supporting, accent = card.accent)
    }
}

@Composable
private fun ShizukuMark(accent: Color) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
        Surface(
            color = Color.White.copy(alpha = DASHBOARD_MARK_CONTAINER_ALPHA),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.size(52.dp),
        ) {}
        Surface(
            color = accent.copy(alpha = DASHBOARD_MARK_ACCENT_ALPHA),
            shape = RoundedCornerShape(7.dp),
            modifier = Modifier.size(30.dp),
        ) {}
        Surface(
            color = accent,
            shape = CircleShape,
            modifier = Modifier.size(7.dp),
        ) {}
    }
}

private data class DashboardCardState(
    val eyebrow: String,
    val headline: String,
    val description: String,
    val supporting: String,
    val action: String,
    val accent: Color,
    val icon: ImageVector,
    val isQuietAction: Boolean = false,
)

@Composable
private fun dashboardCardState(status: BackendStatus): DashboardCardState =
    when (status) {
        BackendStatus.ShizukuUnavailable -> {
            DashboardCardState(
                eyebrow = "Shizuku",
                headline = "Shizuku is off",
                description = "Start the service, then return to BitInstaller.",
                supporting = "Not connected",
                action = "Open Shizuku",
                accent = MaterialTheme.colorScheme.secondary,
                icon = Icons.Outlined.Key,
            )
        }

        BackendStatus.PermissionRequired -> {
            DashboardCardState(
                eyebrow = "Shizuku",
                headline = "Allow BitInstaller",
                description = "Approve once to patch your games.",
                supporting = "Needs approval",
                action = "Allow",
                accent = MaterialTheme.colorScheme.primary,
                icon = Icons.Outlined.Shield,
            )
        }

        BackendStatus.Ready -> {
            DashboardCardState(
                eyebrow = "Shizuku",
                headline = "Ready to patch",
                description = "Choose a game below.",
                supporting = "Connected",
                action = "Manage",
                accent = MaterialTheme.colorScheme.primary,
                icon = Icons.Outlined.Settings,
                isQuietAction = true,
            )
        }
    }

@Composable
private fun StatusLine(
    label: String,
    accent: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = accent,
            shape = CircleShape,
            modifier = Modifier.size(6.dp),
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = DASHBOARD_BODY_ALPHA),
        )
    }
}
