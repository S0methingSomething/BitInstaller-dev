package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

private val SaveEditorShape = RoundedCornerShape(12.dp)
private val SaveEditorHeroShape = RoundedCornerShape(22.dp)
private val SaveEditorButtonShape = RoundedCornerShape(6.dp)
private val SaveEditorInset = 112.dp

@Composable
internal fun SaveEditorSection(
    state: SaveEditorUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
    onFieldEdit: (SaveTargetUiState, BitLifeSaveSummary, SaveEditableField, String) -> Unit,
    onSaveRevert: (SaveTargetUiState, BitLifeSaveSummary) -> Unit,
    onBackClick: () -> Unit,
) {
    var advancedSave by remember { mutableStateOf<BitLifeSaveSummary?>(null) }
    var editDraft by remember { mutableStateOf<SaveFieldEditDraft?>(null) }
    var revertSave by remember { mutableStateOf<BitLifeSaveSummary?>(null) }
    val selectedTarget = state.selectedTarget

    advancedSave?.let { save ->
        SaveAdvancedFieldsDialog(
            save = save,
            recentFieldIds = selectedTarget?.recentEditFieldIds?.get(save.path).orEmpty(),
            onDismissRequest = { advancedSave = null },
            onFieldClick = { field ->
                val target = selectedTarget ?: return@SaveAdvancedFieldsDialog
                editDraft = SaveFieldEditDraft(target = target, save = save, field = field)
                advancedSave = null
            },
        )
    }
    editDraft?.let { draft ->
        SaveFieldEditDialog(
            draft = draft,
            onDismissRequest = { editDraft = null },
            onConfirm = { value ->
                onFieldEdit(draft.target, draft.save, draft.field, value)
                editDraft = null
            },
        )
    }
    revertSave?.let { save ->
        SaveRevertDialog(
            save = save,
            target = selectedTarget,
            onDismissRequest = { revertSave = null },
            onConfirm = { target, targetSave ->
                onSaveRevert(target, targetSave)
                revertSave = null
            },
        )
    }

    if (selectedTarget != null) {
        SaveTargetDetail(
            target = selectedTarget,
            actions =
                SaveTargetCardActions(
                    onTargetClick = onTargetClick,
                    onFieldClick = { save, field ->
                        editDraft = SaveFieldEditDraft(target = selectedTarget, save = save, field = field)
                    },
                    onAdvancedClick = { save -> advancedSave = save },
                    onSaveRevert = { save -> revertSave = save },
                ),
            onBackClick = onBackClick,
        )
        return
    }

    SaveEditorTargetList(targets = state.targets, onTargetClick = onTargetClick)
}

@Composable
private fun SaveEditorTargetList(
    targets: List<SaveTargetUiState>,
    onTargetClick: (SaveTargetUiState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        SaveEditorIntro()
        if (targets.isEmpty()) {
            EmptySaveTargetsCard()
        } else {
            targets.forEach { target ->
                SaveTargetCard(
                    target = target,
                    showSaves = false,
                    actions = SaveTargetCardActions(onTargetClick = onTargetClick),
                )
            }
        }
    }
}

@Composable
private fun SaveTargetDetail(
    target: SaveTargetUiState,
    actions: SaveTargetCardActions,
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
        SaveTargetCard(
            target = target,
            showSaves = true,
            isFocused = true,
            actions = actions,
        )
    }
}

private data class SaveTargetCardActions(
    val onTargetClick: (SaveTargetUiState) -> Unit,
    val onFieldClick: (BitLifeSaveSummary, SaveEditableField) -> Unit = { _, _ -> },
    val onAdvancedClick: (BitLifeSaveSummary) -> Unit = {},
    val onSaveRevert: (BitLifeSaveSummary) -> Unit = {},
)

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
    showSaves: Boolean,
    isFocused: Boolean = false,
    actions: SaveTargetCardActions,
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
            SaveTargetCardHeader(target = target, onTargetClick = actions.onTargetClick)

            if (showSaves) {
                if (target.saves == null) {
                    SaveScanPrompt()
                } else {
                    SaveFileList(
                        target = target,
                        saves = target.saves,
                        onFieldClick = actions.onFieldClick,
                        onAdvancedClick = actions.onAdvancedClick,
                        onSaveRevert = actions.onSaveRevert,
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveTargetCardHeader(
    target: SaveTargetUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
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
        SaveTargetActionButton(
            target = target,
            onTargetClick = onTargetClick,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun SaveTargetActionButton(
    target: SaveTargetUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier,
    ) {
        Text(text = target.actionLabel)
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
