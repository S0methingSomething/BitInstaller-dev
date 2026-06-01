package dev.bitinstaller.app.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

private val SaveEditorButtonShape = SaveEditorControlShape

@Composable
internal fun SaveEditorSection(
    state: SaveEditorUiState,
    actions: SaveEditorSectionActions,
) {
    var advancedSave by remember { mutableStateOf<BitLifeSaveSummary?>(null) }
    var editDraft by remember { mutableStateOf<SaveFieldEditDraft?>(null) }
    var revertSave by remember { mutableStateOf<BitLifeSaveSummary?>(null) }
    val selectedTarget = state.selectedTarget
    var selectedSavePath by rememberSaveable(selectedTarget?.packageName) { mutableStateOf<String?>(null) }
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
                actions.onFieldEdit(draft.target, draft.save, draft.field, value)
                editDraft = null
            },
            confirmRevert = { target, save ->
                actions.onSaveRevert(target, save)
                revertSave = null
            },
            backToSaves = { selectedSavePath = null },
            backToTargets = actions.onBackClick,
        )

    SaveEditorBackHandler(state = modalState, actions = modalActions)
    SaveEditorModals(state = modalState, actions = modalActions)
    SaveEditorFullscreenFrame(
        selectedTarget = selectedTarget,
        selectedSave = selectedSave,
        successPopup = selectedTarget?.saveSuccessPopup(dismissedTokens = dismissedSuccessTokens),
        onDismissPopup = { popup -> dismissedSuccessTokens = dismissedSuccessTokens + (popup.path to popup.token) },
    ) {
        SaveEditorNavigator(
            state = state,
            selectedSave = selectedSave,
            actions = actions,
            callbacks =
                SaveEditorNavigatorCallbacks(
                    onSaveOpen = { save -> selectedSavePath = save.path },
                    onSaveBackClick = { selectedSavePath = null },
                    onFieldClick = { save, field ->
                        selectedTarget?.let { target ->
                            editDraft = SaveFieldEditDraft(target = target, save = save, field = field)
                        }
                    },
                    onAdvancedClick = { save -> advancedSave = save },
                    onSaveRevert = { save -> revertSave = save },
                ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun SaveEditorTargetList(
    targets: List<SaveTargetUiState>,
    onTargetClick: (SaveTargetUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(horizontal = SaveEditorHorizontalPadding, vertical = 18.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (targets.isEmpty()) {
            item(contentType = "empty") { EmptySaveTargetsCard() }
        } else {
            items(targets, key = { target -> target.packageName }, contentType = { "target" }) { target ->
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
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize().padding(horizontal = SaveEditorHorizontalPadding, vertical = 12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Pick a life ID to edit",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onBackClick) {
                Text(text = "Change app")
            }
        }
        if (target.saves == null) {
            SaveScanPrompt()
        } else {
            SaveFileList(
                target = target,
                saves = target.saves,
                onSaveOpen = actions.onSaveOpen,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

internal data class SaveTargetCardActions(
    val onTargetClick: (SaveTargetUiState) -> Unit,
    val onSaveOpen: (BitLifeSaveSummary) -> Unit = {},
)

@Composable
private fun EmptySaveTargetsCard() {
    SaveEditorPanel(shape = SaveEditorCardShape, containerAlpha = 0.035f, modifier = Modifier.fillMaxWidth()) {
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
    SaveEditorPanel(
        shape = if (isFocused) SaveEditorPanelShape else SaveEditorCardShape,
        containerAlpha = if (isFocused) 0.055f else 0.04f,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        if (target.isLoading) {
            LoadingIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = target.actionLabel, fontWeight = FontWeight.Black)
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
            fontWeight = FontWeight.Black,
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
