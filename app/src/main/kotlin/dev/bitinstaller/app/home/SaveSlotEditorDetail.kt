package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

@Composable
internal fun SaveSlotEditorDetail(
    target: SaveTargetUiState,
    save: BitLifeSaveSummary,
    actions: SaveSlotEditorDetailActions,
    modifier: Modifier = Modifier,
    sharedTransitionState: SaveEditorSharedTransitionState = SaveEditorSharedTransitionState.Empty,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        SaveSlotEditorHeader(
            save = save,
            onBackClick = actions.onBackClick,
            sharedTransitionState = sharedTransitionState,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            val statusText = target.editErrors[save.path] ?: save.errorMessage
            if (target.editingSavePath == save.path) {
                SaveSlotStatus(text = "Working on save...", isError = false)
            } else if (statusText != null) {
                SaveSlotStatus(text = statusText, isError = true)
            }
            if (save.errorMessage == null) {
                SaveDetailPanel(title = "Core values", description = "Most-used values for this life.") {
                    SaveFactRows(save = save, onFieldClick = actions.onFieldClick)
                    SaveAttributeRows(attributes = save.attributes, onFieldClick = actions.onFieldClick)
                }
                SaveDetailPanel(title = "Relationships", description = "People tied to this save, kept compact.") {
                    SaveCharacterRows(characters = save.characters, onFieldClick = actions.onFieldClick)
                }
            }
        }
        if (save.errorMessage == null) {
            SaveDetailActions(
                fieldCount = save.advancedFields.size,
                enabled = target.editingSavePath != save.path,
                onAdvancedClick = actions.onAdvancedClick,
                onSaveRevert = actions.onSaveRevert,
            )
        }
    }
}

internal data class SaveSlotEditorDetailActions(
    val onBackClick: () -> Unit,
    val onFieldClick: (SaveEditableField) -> Unit,
    val onAdvancedClick: () -> Unit,
    val onSaveRevert: () -> Unit,
)

@Composable
private fun SaveSlotEditorHeader(
    save: BitLifeSaveSummary,
    onBackClick: () -> Unit,
    sharedTransitionState: SaveEditorSharedTransitionState,
) {
    SaveEditorPanel(
        containerAlpha = 0.055f,
        modifier =
            Modifier
                .fillMaxWidth()
                .saveSlotSharedBounds(save = save, transitionState = sharedTransitionState),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(18.dp)) {
            TextButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Back to save slots")
            }
            SaveSlotBadge(slotName = save.slotName)
            Text(
                text = save.heroName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            SaveFileMetaLine(save = save)
        }
    }
}

@Composable
private fun SaveDetailPanel(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    SaveEditorPanel(containerAlpha = 0.04f, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun SaveDetailActions(
    fieldCount: Int,
    enabled: Boolean,
    onAdvancedClick: () -> Unit,
    onSaveRevert: () -> Unit,
) {
    val primaryWeight by animateExpressiveFontWeight(
        isActive = enabled,
        restWeight = FontWeight.SemiBold.weight,
        activeWeight = FontWeight.Black.weight,
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            enabled = enabled,
            onClick = onAdvancedClick,
            shape = SaveEditorControlShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        ) {
            Text(
                text = "Open Advanced Editor · $fieldCount",
                fontWeight = FontWeight(primaryWeight),
                textAlign = TextAlign.Center,
            )
        }
        FilledTonalButton(
            enabled = enabled,
            onClick = onSaveRevert,
            shape = SaveEditorControlShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        ) {
            Text(text = "Revert from backup", textAlign = TextAlign.Center)
        }
    }
}
