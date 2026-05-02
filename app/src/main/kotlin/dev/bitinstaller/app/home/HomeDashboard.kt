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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DashboardShape = RoundedCornerShape(12.dp)
private val DashboardActionShape = RoundedCornerShape(6.dp)
private val DashboardButtonInset = 96.dp
private val DashboardMinHeight = 144.dp

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
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DashboardMinHeight)
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
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
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Text(text = card.action)
        }
    } else {
        Button(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = DashboardActionShape,
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Text(text = card.action)
        }
    }
}

@Composable
private fun DashboardTextBlock(card: DashboardCardState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Shizuku",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = card.headline,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
        )
        StatusLine(label = card.supporting, accent = card.accent)
    }
}

@Composable
private fun ShizukuMark(accent: Color) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.size(48.dp),
        ) {}
        Surface(
            color = accent.copy(alpha = 0.16f),
            shape = CircleShape,
            modifier = Modifier.size(28.dp),
        ) {}
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = CircleShape,
            modifier = Modifier.size(10.dp),
        ) {}
    }
}

private data class DashboardCardState(
    val headline: String,
    val supporting: String,
    val action: String,
    val accent: Color,
    val isQuietAction: Boolean = false,
)

@Composable
private fun dashboardCardState(status: BackendStatus): DashboardCardState =
    when (status) {
        BackendStatus.ShizukuUnavailable -> DashboardCardState(
            headline = "Shizuku offline",
            supporting = "Status: not running",
            action = "Start",
            accent = MaterialTheme.colorScheme.secondary,
        )

        BackendStatus.PermissionRequired -> DashboardCardState(
            headline = "Permission needed",
            supporting = "Status: permission required",
            action = "Grant",
            accent = MaterialTheme.colorScheme.primary,
        )

        BackendStatus.Ready -> DashboardCardState(
            headline = "Ready",
            supporting = "Status: connected",
            action = "Open",
            accent = MaterialTheme.colorScheme.primary,
            isQuietAction = true,
        )
    }

@Composable
private fun StatusLine(label: String, accent: Color) {
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
