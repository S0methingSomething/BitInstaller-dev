package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.OutlinedButton
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

private val DashboardShape = RoundedCornerShape(12.dp)
private val DashboardActionShape = RoundedCornerShape(6.dp)
private val DashboardButtonInset = 124.dp
private val DashboardMinHeight = 154.dp

@Composable
internal fun DashboardSection(
    status: BackendStatus,
    onActionClick: () -> Unit,
) {
    val card = dashboardCardState(status)

    Surface(
        shape = DashboardShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = DashboardMinHeight)
                .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = DashboardButtonInset),
        ) {
            ShizukuMark(accent = card.accent)
            DashboardTextBlock(card = card)
        }

        DashboardActionButton(card = card, onActionClick = onActionClick)
    }
}

@Composable
private fun BoxScope.DashboardActionButton(
    card: DashboardCardState,
    onActionClick: () -> Unit,
) {
    if (card.isQuietAction) {
        OutlinedButton(
            onClick = onActionClick,
            shape = DashboardActionShape,
            modifier = Modifier.align(Alignment.BottomEnd).heightIn(min = 44.dp),
        ) {
            Icon(imageVector = card.icon, contentDescription = card.action)
            Text(text = card.action)
        }
    } else {
        Button(
            onClick = onActionClick,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            shape = DashboardActionShape,
            modifier = Modifier.align(Alignment.BottomEnd).heightIn(min = 44.dp),
        ) {
            Icon(imageVector = card.icon, contentDescription = card.action)
            Text(text = card.action)
        }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = card.headline,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = card.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StatusLine(label = card.supporting, accent = card.accent)
    }
}

@Composable
private fun ShizukuMark(accent: Color) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.size(52.dp),
        ) {}
        Surface(
            color = accent.copy(alpha = 0.16f),
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
