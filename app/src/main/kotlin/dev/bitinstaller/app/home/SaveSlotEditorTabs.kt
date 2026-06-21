package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

internal fun SnapshotStateMap<String, String>.valueFor(field: SaveEditableField): String = this[field.id] ?: field.value

private const val SAVE_DETAIL_SECTION_LETTER_SPACING_SP = 1f
private const val SAVE_DETAIL_PANEL_TITLE_ALPHA = 0.4f
private const val SECTION_HEADER_ALPHA = 0.08f
private const val SECTION_HEADER_PRIMARY_ALPHA = 0.12f
private const val SECTION_TITLE_ALPHA = 0.9f
private const val SECTION_COUNT_ALPHA = 0.4f
private const val SECTION_CHEVRON_ALPHA = 0.5f
private const val SECTION_ICON_ALPHA = 0.85f
private const val SUBGROUP_ALPHA = 0.25f
private const val PRIMARY_DIVIDER_ALPHA = 0.15f
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

internal data class SectionHeaderContent(
    val title: String,
    val fieldCount: Int?,
    val icon: String = "",
    val isPrimary: Boolean = false,
)

@Composable
internal fun AccordionSectionHeader(
    content: SectionHeaderContent,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val bgAlpha = if (content.isPrimary) SECTION_HEADER_PRIMARY_ALPHA else SECTION_HEADER_ALPHA
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = bgAlpha), shape = AccordionSectionShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (content.icon.isNotEmpty()) {
            Text(
                text = content.icon,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = SECTION_ICON_ALPHA),
            )
        }
        Text(
            text = content.title,
            style =
                if (content.isPrimary) {
                    MaterialTheme.typography.titleSmall
                } else {
                    MaterialTheme.typography.labelMedium
                },
            color = Color.White.copy(alpha = SECTION_TITLE_ALPHA),
            fontWeight = if (content.isPrimary) FontWeight.Black else FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (content.fieldCount != null) {
            Text(
                text = content.fieldCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = SECTION_COUNT_ALPHA),
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = if (expanded) "\u25BC" else "\u25B6",
            color = Color.White.copy(alpha = SECTION_CHEVRON_ALPHA),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
internal fun PrimarySectionDivider() {
    androidx.compose.material3.HorizontalDivider(
        color = Color.White.copy(alpha = PRIMARY_DIVIDER_ALPHA),
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
internal fun AccordionSubGroupHeader(
    title: String,
    count: Int? = null,
) {
    val displayTitle = if (count != null) "$title ($count)" else title
    Text(
        text = displayTitle.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = SUBGROUP_ALPHA),
        fontWeight = FontWeight.Bold,
        letterSpacing = SAVE_DETAIL_SECTION_LETTER_SPACING_SP.sp,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
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

internal fun toggleSection(
    expanded: Set<String>,
    id: String,
): Set<String> = if (id in expanded) expanded - id else expanded + id

private const val AVATAR_SIZE = 36
private const val AVATAR_ALPHA = 0.10f
private const val AVATAR_TEXT_ALPHA = 0.70f
private val AvatarShape = RoundedCornerShape(20.dp)

@Composable
internal fun NotionAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(AVATAR_SIZE.dp)
                .background(Color.White.copy(alpha = AVATAR_ALPHA), shape = AvatarShape),
    ) {
        Text(
            text = initials.take(2).uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = AVATAR_TEXT_ALPHA),
            fontWeight = FontWeight.Bold,
        )
    }
}
