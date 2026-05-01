package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val TargetCardShape = RoundedCornerShape(16.dp)
private val TargetButtonShape = RoundedCornerShape(12.dp)
private val TargetButtonInset = 112.dp
private val TargetMinHeight = 132.dp

@Composable
internal fun PatchTargetsSection(
    targets: List<PatchTargetUiState>,
    onPatchClick: (PatchTargetUiState) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Apps",
            style = MaterialTheme.typography.titleLarge,
        )
        targets.forEach { target ->
            PatchTargetCard(
                target = target,
                onPatchClick = onPatchClick,
            )
        }
    }
}

@Composable
private fun PatchTargetCard(
    target: PatchTargetUiState,
    onPatchClick: (PatchTargetUiState) -> Unit,
) {
    val accent = targetAccentColor(target.supportState)

    Surface(
        shape = TargetCardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TargetMinHeight)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = TargetButtonInset),
            ) {
                AppGlyph(monogram = target.iconMonogram, accent = accent)
                TargetTextBlock(target = target)
            }

            Button(
                enabled = target.patchEnabled,
                onClick = { onPatchClick(target) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = TargetButtonShape,
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                Text(text = target.patchLabel)
            }
        }
    }
}

@Composable
private fun TargetTextBlock(target: PatchTargetUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = target.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "v${target.versionLabel}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PatchStateChip(
                label = target.patchPresenceLabel,
                patchPresenceState = target.patchPresenceState,
            )
        }
        Text(
            text = target.statusLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppGlyph(
    monogram: String,
    accent: Color,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
            ),
    ) {
        Surface(
            color = accent.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = monogram,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
            }
        }
    }
}

@Composable
private fun PatchStateChip(
    label: String,
    patchPresenceState: PatchPresenceState,
) {
    val contentColor = patchPresenceColor(patchPresenceState)
    val containerColor = patchPresenceContainerColor(patchPresenceState)

    Surface(
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun targetAccentColor(supportState: PatchSupportState): Color =
    when (supportState) {
        PatchSupportState.READY -> MaterialTheme.colorScheme.primary
        PatchSupportState.BACKEND_REQUIRED -> MaterialTheme.colorScheme.secondary
        PatchSupportState.UNSUPPORTED -> MaterialTheme.colorScheme.outline
    }

@Composable
private fun patchPresenceColor(patchPresenceState: PatchPresenceState): Color =
    when (patchPresenceState) {
        PatchPresenceState.NOT_PATCHED -> MaterialTheme.colorScheme.onSurfaceVariant
        PatchPresenceState.PATCHED -> MaterialTheme.colorScheme.tertiary
        PatchPresenceState.UNKNOWN -> MaterialTheme.colorScheme.outline
    }

@Composable
private fun patchPresenceContainerColor(patchPresenceState: PatchPresenceState): Color =
    when (patchPresenceState) {
        PatchPresenceState.NOT_PATCHED -> MaterialTheme.colorScheme.surfaceVariant
        PatchPresenceState.PATCHED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        PatchPresenceState.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
    }
