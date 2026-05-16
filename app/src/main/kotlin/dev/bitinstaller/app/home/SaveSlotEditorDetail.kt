package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

private val SaveDetailPanelShape = RoundedCornerShape(18.dp)
private val SaveDetailButtonShape = RoundedCornerShape(14.dp)

internal data class SaveSlotEditorDetailActions(
    val onBackClick: () -> Unit,
    val onFieldClick: (SaveEditableField) -> Unit,
    val onAdvancedClick: () -> Unit,
    val onSaveRevert: () -> Unit,
)

@Composable
internal fun SaveSlotEditorDetail(
    target: SaveTargetUiState,
    save: BitLifeSaveSummary,
    actions: SaveSlotEditorDetailActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
        SaveSlotEditorHeader(save = save, onBackClick = actions.onBackClick)
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
            SaveDetailActions(
                fieldCount = save.advancedFields.size,
                enabled = target.editingSavePath != save.path,
                onAdvancedClick = actions.onAdvancedClick,
                onSaveRevert = actions.onSaveRevert,
            )
        }
    }
}

@Composable
private fun SaveSlotEditorHeader(
    save: BitLifeSaveSummary,
    onBackClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)),
        shape = SaveDetailPanelShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(18.dp)) {
            TextButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Back to save slots")
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SaveSlotBadge(slotName = save.slotName)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
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
    }
}

@Composable
private fun SaveDetailPanel(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.26f)),
        shape = SaveDetailPanelShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            enabled = enabled,
            onClick = onAdvancedClick,
            shape = SaveDetailButtonShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        ) {
            Text(text = "Open Advanced Editor · $fieldCount")
        }
        TextButton(enabled = enabled, onClick = onSaveRevert, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Revert from backup")
        }
    }
}
