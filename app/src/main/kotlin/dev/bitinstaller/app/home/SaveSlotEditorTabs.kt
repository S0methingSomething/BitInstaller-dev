package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

internal fun SnapshotStateMap<String, String>.valueFor(field: SaveEditableField): String = this[field.id] ?: field.value

private const val SAVE_DETAIL_SECTION_LETTER_SPACING_SP = 1f
private const val SAVE_DETAIL_PANEL_TITLE_ALPHA = 0.4f
private const val SECTION_HEADER_ALPHA = 0.08f
private const val SECTION_TITLE_ALPHA = 0.9f
private const val SECTION_COUNT_ALPHA = 0.4f
private const val SECTION_CHEVRON_ALPHA = 0.5f
private const val SUBGROUP_ALPHA = 0.25f
private val AccordionSectionShape = RoundedCornerShape(10.dp)

internal data class SaveSlotTabBodyState(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val draftValues: SnapshotStateMap<String, String>,
    val recentFieldIds: List<String>,
)

@Immutable
internal data class SaveSlotTabBodyActions(
    val onDraftChange: (SaveEditableField, String) -> Unit,
)

internal fun LazyListScope.saveSlotStatusItem(state: SaveSlotTabBodyState) {
    if (state.target.editingSavePath == state.save.path) return
    val statusText = state.target.editErrors[state.save.path] ?: state.save.errorMessage
    if (statusText != null) {
        item(contentType = "status") { SaveSlotStatus(text = statusText, isError = true) }
    }
}

@Composable
internal fun AccordionSectionHeader(
    title: String,
    fieldCount: Int?,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = SECTION_HEADER_ALPHA), shape = AccordionSectionShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = if (expanded) "▼" else "▶",
            color = Color.White.copy(alpha = SECTION_CHEVRON_ALPHA),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = SECTION_TITLE_ALPHA),
            fontWeight = FontWeight.Black,
            letterSpacing = SAVE_DETAIL_SECTION_LETTER_SPACING_SP.sp,
            modifier = Modifier.weight(1f),
        )
        if (fieldCount != null) {
            Text(
                text = fieldCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = SECTION_COUNT_ALPHA),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
internal fun AccordionSubGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = SUBGROUP_ALPHA),
        fontWeight = FontWeight.Bold,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

@Composable
internal fun SaveStatsTabContent(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    SaveDetailPanel(title = "IDENTITY & BIO METRICS") {
        SaveFactRows(save = state.save, draftValues = state.draftValues, onFieldChange = actions.onDraftChange)
        SaveAttributeRows(
            attributes = state.save.attributes,
            draftValues = state.draftValues,
            onFieldChange = actions.onDraftChange,
        )
    }
}

@Composable
internal fun SavePeopleTabContent(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    SaveDetailPanel(title = "FAMILY & RELATIONSHIPS") {
        SaveCharacterRows(
            characters = state.save.characters,
            draftValues = state.draftValues,
            onFieldChange = actions.onDraftChange,
        )
    }
}

@Composable
internal fun SaveDetailPanel(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    SaveEditorPanel(containerAlpha = 0.04f, shape = SaveEditorControlShape, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = SAVE_DETAIL_PANEL_TITLE_ALPHA),
                fontWeight = FontWeight.Bold,
                letterSpacing = SAVE_DETAIL_SECTION_LETTER_SPACING_SP.sp,
            )
            content()
        }
    }
}
