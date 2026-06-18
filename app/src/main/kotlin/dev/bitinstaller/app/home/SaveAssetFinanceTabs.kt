package dev.bitinstaller.app.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.SaveEditableField

private val ASSET_FIELD_PREFIXES =
    listOf(
        "Life / Car Array /",
        "Life / House Array /",
        "Life / Aircraft Array /",
        "Life / Watercraft Array /",
        "Life / Recreational Vehicle Array /",
        "Life / Jewelry Array /",
        "Life / Heirloom Array /",
    )

private val FINANCE_FIELD_PREFIXES =
    listOf(
        "Life / Finances /",
        "Life / Portfolio /",
        "Life / Landlord Portfolio /",
    )

private const val SECTION_LETTER_SPACING_SP = 1f
private const val SECTION_HEADER_ALPHA = 0.25f
private const val SECTION_HEADER_PADDING_HP = 12
private const val SECTION_HEADER_PADDING_VP = 6
private const val EMPTY_STATE_ALPHA = 0.5f
private val SectionHeaderShape = RoundedCornerShape(8.dp)

internal fun List<SaveEditableField>.filterByPathPrefixes(prefixes: List<String>): List<SaveEditableField> =
    filter { field -> prefixes.any { prefix -> field.path.startsWith(prefix) } }

private data class GroupedFieldContent(
    val fields: List<SaveEditableField>,
    val metadataMap: Map<String, FieldMetadata>,
    val draftValues: SnapshotStateMap<String, String>,
    val onDraftChange: (SaveEditableField, String) -> Unit,
    val emptyMessage: String,
)

@Composable
internal fun SaveAssetsTabContent(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    modifier: Modifier = Modifier,
) {
    val fields = remember(state.save) { state.save.advancedFields.filterByPathPrefixes(ASSET_FIELD_PREFIXES) }
    val metadataMap = remember(state.save) { fields.associate { it.id to it.computeMetadata() } }
    GroupedFieldList(
        content =
            GroupedFieldContent(
                fields = fields,
                metadataMap = metadataMap,
                draftValues = state.draftValues,
                onDraftChange = actions.onDraftChange,
                emptyMessage = "No assets found in this save.",
            ),
        modifier = modifier,
    )
}

@Composable
internal fun SaveFinanceTabContent(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    modifier: Modifier = Modifier,
) {
    val fields = remember(state.save) { state.save.advancedFields.filterByPathPrefixes(FINANCE_FIELD_PREFIXES) }
    val metadataMap = remember(state.save) { fields.associate { it.id to it.computeMetadata() } }
    GroupedFieldList(
        content =
            GroupedFieldContent(
                fields = fields,
                metadataMap = metadataMap,
                draftValues = state.draftValues,
                onDraftChange = actions.onDraftChange,
                emptyMessage = "No finance fields found in this save.",
            ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupedFieldList(
    content: GroupedFieldContent,
    modifier: Modifier = Modifier,
) {
    if (content.fields.isEmpty()) {
        Box(modifier = modifier.fillMaxSize()) {
            Text(
                text = content.emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = EMPTY_STATE_ALPHA),
                modifier = Modifier.padding(20.dp),
            )
        }
        return
    }
    val grouped = remember(content.fields) { content.fields.groupBy { it.group }.toSortedMap() }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        grouped.forEach { (sectionName, sectionFields) ->
            stickyHeader(contentType = "section-header") {
                SectionHeader(title = sectionName)
            }
            items(
                items = sectionFields,
                key = { field: SaveEditableField -> field.id },
                contentType = { field: SaveEditableField -> field.valueKind },
            ) { field: SaveEditableField ->
                SaveAdvancedFieldCard(
                    field = field,
                    draftValue = content.draftValues.valueFor(field),
                    metadata = content.metadataMap[field.id] ?: field.computeMetadata(),
                    onDraftChange = content.onDraftChange,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = SECTION_HEADER_ALPHA),
        fontWeight = FontWeight.Bold,
        letterSpacing = SECTION_LETTER_SPACING_SP.sp,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), shape = SectionHeaderShape)
                .padding(horizontal = SECTION_HEADER_PADDING_HP.dp, vertical = SECTION_HEADER_PADDING_VP.dp),
    )
}
