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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.bitinstaller.app.save.BitLifeSaveSummary

private val SaveEditorShape = RoundedCornerShape(12.dp)
private val SaveEditorHeroShape = RoundedCornerShape(22.dp)
private val SaveEditorButtonShape = RoundedCornerShape(6.dp)
private val SaveEditorInset = 112.dp

@Composable
internal fun SaveEditorSection(
    state: SaveEditorUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
    onBackClick: () -> Unit,
) {
    val selectedTarget = state.selectedTarget
    if (selectedTarget != null) {
        SaveTargetDetail(
            target = selectedTarget,
            onTargetClick = onTargetClick,
            onBackClick = onBackClick,
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        SaveEditorIntro()
        if (state.targets.isEmpty()) {
            EmptySaveTargetsCard()
        } else {
            state.targets.forEach { target ->
                SaveTargetCard(target = target, onTargetClick = onTargetClick, showSaves = false)
            }
        }
    }
}

@Composable
private fun SaveTargetDetail(
    target: SaveTargetUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${target.name} saves",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Current savedLife.data from every sg* slot",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onBackClick) {
                Text(text = "Change app")
            }
        }
        SaveTargetCard(target = target, onTargetClick = onTargetClick, showSaves = true, isFocused = true)
    }
}

@Composable
private fun SaveEditorIntro() {
    Surface(
        shape = SaveEditorShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Save Editor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text =
                    "Pick an installed BitLife app, then scan each sg* slot's current " +
                        "savedLife.data for names, money, stats, and characters.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptySaveTargetsCard() {
    Surface(
        shape = SaveEditorShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "No installed BitLife apps found on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun SaveTargetCard(
    target: SaveTargetUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
    showSaves: Boolean,
    isFocused: Boolean = false,
) {
    Surface(
        shape = if (isFocused) SaveEditorHeroShape else SaveEditorShape,
        color =
            if (isFocused) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
            },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isFocused) 0.55f else 1f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(end = SaveEditorInset),
                ) {
                    SaveAppGlyph(icon = target.icon, name = target.name)
                    SaveTargetTextBlock(target = target)
                }
                Button(
                    enabled = target.actionEnabled,
                    onClick = { onTargetClick(target) },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        ),
                    shape = SaveEditorButtonShape,
                    modifier = Modifier.align(Alignment.BottomEnd),
                ) {
                    Text(text = target.actionLabel)
                }
            }

            if (showSaves) {
                if (target.saves == null) {
                    SaveScanPrompt()
                } else {
                    SaveFileList(saves = target.saves)
                }
            }
        }
    }
}

@Composable
private fun SaveTargetTextBlock(target: SaveTargetUiState) {
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
        if (target.versionLabel.isNotEmpty()) {
            Text(
                text = "v${target.versionLabel}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(5.dp),
            ) {}
            Text(
                text = target.statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SaveAppGlyph(
    icon: TargetIcon,
    name: String,
) {
    if (icon.drawable != null) {
        Image(
            painter = rememberDrawablePainter(drawable = icon.drawable),
            contentDescription = name,
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
        )
    } else {
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
            Text(
                text = icon.monogram,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SaveFileList(saves: List<BitLifeSaveSummary>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        saves.forEach { save -> SaveFileCard(save = save) }
    }
}

@Composable
private fun SaveFileCard(save: BitLifeSaveSummary) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(11.dp),
            modifier = Modifier.padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SaveSlotBadge(slotName = save.slotName)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = save.heroName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SaveFileMetaLine(save = save)
            if (save.errorMessage != null) {
                Text(
                    text = save.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                SaveFactRows(save = save)
                SaveAttributeRows(attributes = save.attributes)
                SaveCharacterRows(characters = save.characters)
            }
        }
    }
}
