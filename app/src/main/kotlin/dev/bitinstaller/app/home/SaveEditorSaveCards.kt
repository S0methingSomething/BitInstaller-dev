package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import java.util.Locale

private val SaveCardShape = RoundedCornerShape(18.dp)
private val AdvancedButtonShape = RoundedCornerShape(12.dp)
private const val COLLAPSED_ATTRIBUTE_COUNT = 3

@Composable
internal fun SaveFileList(
    target: SaveTargetUiState,
    saves: List<BitLifeSaveSummary>,
    onFieldClick: (BitLifeSaveSummary, SaveEditableField) -> Unit,
    onAdvancedClick: (BitLifeSaveSummary) -> Unit,
    onSaveRevert: (BitLifeSaveSummary) -> Unit,
) {
    var expandedPath by remember(saves) { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        saves.forEach { save ->
            val isExpanded = expandedPath == save.path
            SaveFileCard(
                state =
                    SaveFileCardState(
                        save = save,
                        isExpanded = isExpanded,
                        isSaving = target.editingSavePath == save.path,
                        editError = if (isExpanded) target.editErrors[save.path] else null,
                        editMessage = if (isExpanded) target.editMessages[save.path] else null,
                    ),
                actions =
                    SaveFileCardActions(
                        onToggle = { expandedPath = if (expandedPath == save.path) null else save.path },
                        onFieldClick = { field -> onFieldClick(save, field) },
                        onAdvancedClick = { onAdvancedClick(save) },
                        onSaveRevert = { onSaveRevert(save) },
                    ),
            )
        }
    }
}

private data class SaveFileCardState(
    val save: BitLifeSaveSummary,
    val isExpanded: Boolean,
    val isSaving: Boolean,
    val editError: String?,
    val editMessage: String?,
)

private data class SaveFileCardActions(
    val onToggle: () -> Unit,
    val onFieldClick: (SaveEditableField) -> Unit,
    val onAdvancedClick: () -> Unit,
    val onSaveRevert: () -> Unit,
)

@Composable
private fun SaveFileCard(
    state: SaveFileCardState,
    actions: SaveFileCardActions,
) {
    val save = state.save
    Surface(
        shape = SaveCardShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(14.dp),
        ) {
            SaveFileCardHeader(
                save = save,
                isExpanded = state.isExpanded,
                onToggle = actions.onToggle,
            )
            SaveStatusMessage(
                isSaving = state.isSaving,
                error = state.editError ?: save.errorMessage,
                message = state.editMessage,
            )
            SaveFileCardBody(state = state, actions = actions)
        }
    }
}

@Composable
private fun SaveFileCardHeader(
    save: BitLifeSaveSummary,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        SaveSlotBadge(slotName = save.slotName)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = save.heroName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SaveFileMetaLine(save = save)
        }
        TextButton(onClick = onToggle) {
            Text(text = if (isExpanded) "Collapse" else "Edit")
        }
    }
}

@Composable
private fun SaveFileCardBody(
    state: SaveFileCardState,
    actions: SaveFileCardActions,
) {
    val save = state.save
    if (save.errorMessage != null) return
    if (!state.isExpanded) {
        SaveCollapsedSummary(save = save)
        return
    }
    SaveFactRows(save = save, onFieldClick = actions.onFieldClick)
    SaveAttributeRows(attributes = save.attributes, onFieldClick = actions.onFieldClick)
    SaveCharacterRows(characters = save.characters, onFieldClick = actions.onFieldClick)
    SaveFileActions(fieldCount = save.advancedFields.size, actions = actions, enabled = !state.isSaving)
}

@Composable
private fun SaveCollapsedSummary(save: BitLifeSaveSummary) {
    val attrs =
        save.attributes
            .take(COLLAPSED_ATTRIBUTE_COUNT)
            .joinToString("  •  ") { attribute -> "${attribute.label} ${attribute.value.toInt()}" }
    Text(
        text =
            listOfNotNull(
                save.bankBalance?.let { String.format(Locale.US, "$%,.0f", it) },
                attrs.takeIf { it.isNotBlank() },
                "${save.characters.size} characters",
            ).joinToString("  •  "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SaveStatusMessage(
    isSaving: Boolean,
    error: String?,
    message: String?,
) {
    val text =
        when {
            isSaving -> "Working on save..."
            error != null -> error
            message != null -> message
            else -> return
        }
    val isError = error != null
    Surface(
        color = statusColor(isError = isError),
        border = BorderStroke(1.dp, statusBorderColor(isError = isError)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun statusColor(isError: Boolean) =
    if (isError) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }

@Composable
private fun statusBorderColor(isError: Boolean) =
    if (isError) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.24f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    }

@Composable
private fun SaveFileActions(
    fieldCount: Int,
    actions: SaveFileCardActions,
    enabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            enabled = enabled,
            onClick = actions.onAdvancedClick,
            shape = AdvancedButtonShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Open values · $fieldCount")
        }
        TextButton(enabled = enabled, onClick = actions.onSaveRevert, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Revert backup")
        }
    }
}
