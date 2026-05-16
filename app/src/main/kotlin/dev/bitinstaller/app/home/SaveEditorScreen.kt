package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
    var selectedSavePath by remember(selectedTarget?.packageName) { mutableStateOf<String?>(null) }
    var dismissedSuccessTokens by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val selectedSave = selectedTarget?.saves?.firstOrNull { save -> save.path == selectedSavePath }
    val modalState = SaveEditorModalState(selectedTarget, selectedSavePath, advancedSave, editDraft, revertSave)
    val modalActions =
        SaveEditorModalActions(
            closeAdvanced = { advancedSave = null },
            closeEdit = { editDraft = null },
            closeRevert = { revertSave = null },
            openEditFromAdvanced = { save, field ->
                selectedTarget?.let { target ->
                    editDraft = SaveFieldEditDraft(target = target, save = save, field = field)
                    advancedSave = null
                }
            },
            submitEdit = { draft, value ->
                onFieldEdit(draft.target, draft.save, draft.field, value)
                editDraft = null
            },
            confirmRevert = { target, save ->
                onSaveRevert(target, save)
                revertSave = null
            },
            backToSaves = { selectedSavePath = null },
            backToTargets = onBackClick,
        )

    SaveEditorBackHandler(state = modalState, actions = modalActions)
    SaveEditorModals(state = modalState, actions = modalActions)
    SaveEditorSuccessPopup(selectedTarget, dismissedSuccessTokens) { popup ->
        dismissedSuccessTokens = dismissedSuccessTokens + (popup.path to popup.token)
    }

    if (selectedTarget != null) {
        SaveSelectedTargetContent(
            target = selectedTarget,
            selectedSave = selectedSave,
            onSaveBackClick = { selectedSavePath = null },
            actions =
                SaveSelectedTargetActions(
                    onTargetClick = onTargetClick,
                    onSaveOpen = { save -> selectedSavePath = save.path },
                    onFieldClick = { save, field ->
                        editDraft = SaveFieldEditDraft(target = selectedTarget, save = save, field = field)
                    },
                    onAdvancedClick = { save -> advancedSave = save },
                    onSaveRevert = { save -> revertSave = save },
                    onChangeApp = onBackClick,
                ),
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
    Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
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
internal fun SaveTargetDetail(
    target: SaveTargetUiState,
    actions: SaveTargetCardActions,
    onBackClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Save slots",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${target.name} · pick the life ID you want to edit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onBackClick) {
                Text(text = "Change app")
            }
        }
        SaveTargetCardHeader(target = target, onTargetClick = actions.onTargetClick)
        if (target.saves == null) {
            SaveScanPrompt()
        } else {
            SaveFileList(
                target = target,
                saves = target.saves,
                onSaveOpen = actions.onSaveOpen,
            )
        }
    }
}

internal data class SaveTargetCardActions(
    val onTargetClick: (SaveTargetUiState) -> Unit,
    val onSaveOpen: (BitLifeSaveSummary) -> Unit = {},
)

@Composable
private fun SaveEditorIntro() {
    Surface(
        shape = SaveEditorShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "No installed BitLife apps found on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
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
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
            },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = if (isFocused) 0.34f else 0.24f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            SaveTargetCardHeader(target = target, onTargetClick = actions.onTargetClick)

            if (showSaves) {
                if (target.saves == null) {
                    SaveScanPrompt()
                } else {
                    SaveFileList(
                        target = target,
                        saves = target.saves,
                        onSaveOpen = actions.onSaveOpen,
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SaveAppGlyph(icon = target.icon, name = target.name)
            SaveTargetTextBlock(target = target)
        }
        SaveTargetActionButton(
            target = target,
            onTargetClick = onTargetClick,
            modifier = Modifier.fillMaxWidth(),
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
