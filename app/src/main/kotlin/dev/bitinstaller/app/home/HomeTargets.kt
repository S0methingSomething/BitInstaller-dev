package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

private val TargetCardShape = RoundedCornerShape(12.dp)
private val TargetButtonShape = RoundedCornerShape(6.dp)
private const val INSTALLED_TARGET_CARD_ALPHA: Float = 0.02f
private const val MISSING_TARGET_CARD_ALPHA: Float = 0.012f

@Composable
internal fun PatchTargetsSection(
    targets: List<PatchTargetUiState>,
    onPatchClick: (PatchTargetUiState) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Games",
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
    val accent = targetAccentColor(target.patchState.supportState)
    val cardAlpha = if (target.isInstalled) INSTALLED_TARGET_CARD_ALPHA else MISSING_TARGET_CARD_ALPHA

    Surface(
        shape = TargetCardShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = cardAlpha),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppGlyph(icon = target.icon, name = target.name, accent = accent)
                TargetTextBlock(target = target)
            }

            if (target.isInstalled) {
                Button(
                    enabled = target.patchState.actionEnabled,
                    onClick = { onPatchClick(target) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        ),
                    shape = TargetButtonShape,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    if (!target.patchState.actionEnabled && target.patchState.actionLabel == "Opening") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = target.patchState.actionLabel)
                }
            }
        }
    }
}

@Composable
private fun TargetTextBlock(target: PatchTargetUiState) {
    val statusAccent = patchPresenceColor(target.patchState.presenceState)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = target.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (target.versionLabel.isNotEmpty()) {
                Text(
                    text = "v${target.versionLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PatchStateChip(
                label = target.patchState.presenceLabel,
                patchPresenceState = target.patchState.presenceState,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = statusAccent,
                shape = CircleShape,
                modifier = Modifier.size(5.dp),
            ) {}
            Text(
                text = target.patchState.statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AppGlyph(
    icon: TargetIcon,
    name: String,
    accent: Color,
) {
    if (icon.drawable != null) {
        val painter =
            remember(icon.drawable) {
                BitmapPainter(icon.drawable.toBitmap().asImageBitmap())
            }
        Image(
            painter = painter,
            contentDescription = name,
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
        )
    } else {
        MonogramGlyph(monogram = icon.monogram, accent = accent)
    }
}

@Composable
private fun MonogramGlyph(
    monogram: String,
    accent: Color,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                ),
    ) {
        Surface(
            color = accent.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(8.dp),
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(999.dp),
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
        PatchPresenceState.NOT_PATCHED -> Color.Transparent
        PatchPresenceState.PATCHED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        PatchPresenceState.UNKNOWN -> Color.Transparent
    }
