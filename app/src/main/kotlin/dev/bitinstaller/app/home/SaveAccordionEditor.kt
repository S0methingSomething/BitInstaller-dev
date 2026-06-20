package dev.bitinstaller.app.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveCharacterSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind

private const val SEARCH_COUNT_ALPHA = 0.3f

private data class AccordionSection(
    val id: String,
    val title: String,
    val fieldCount: Int? = null,
    val icon: String = "",
    val isPrimary: Boolean = false,
)

private data class AccordionState(
    val expandedSections: Set<String>,
    val onToggle: (String) -> Unit,
)

private data class FieldListContent(
    val fields: List<SaveEditableField>,
    val metadataMap: Map<String, FieldMetadata>,
    val draftValues: SnapshotStateMap<String, String>,
    val onDraftChange: (SaveEditableField, String) -> Unit,
)

private data class ChipState(
    val activeChips: Map<String, String>,
    val onChipSet: (String, String?) -> Unit,
    val expandedPersons: Set<String>,
    val onTogglePerson: (String) -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SaveAccordionEditor(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    modifier: Modifier = Modifier,
) {
    val save = state.save
    val draftValues = state.draftValues
    val onDraftChange = actions.onDraftChange

    var query by rememberSaveable(save.path) { mutableStateOf("") }
    val accordion = rememberAccordionState(save.path)
    val chipState = rememberChipState(save.path)

    val searchContext = rememberSearchContext(save)
    val searchResults = rememberSearchResults(query, save, state.recentFieldIds, searchContext)
    val pathSectionContent = rememberPathSectionContent(save, draftValues, onDraftChange, searchContext)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        if (save.errorMessage == null) {
            stickyHeader(contentType = "search-bar") {
                Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.6f))) {
                    SaveAdvancedSearch(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (query.isBlank() || save.errorMessage != null) {
            browseSections(
                state = state,
                actions = actions,
                content =
                    BrowseContent(
                        accordion = accordion,
                        pathSectionContent = pathSectionContent,
                        advancedContent =
                            FieldListContent(
                                fields = save.advancedFields,
                                metadataMap = searchContext.metadataMap,
                                draftValues = draftValues,
                                onDraftChange = onDraftChange,
                            ),
                        chipState = chipState,
                    ),
            )
        } else {
            searchResultItems(searchResults.value, searchContext.metadataMap, draftValues, onDraftChange)
        }
    }
}

@Composable
private fun rememberAccordionState(savePath: String): AccordionState {
    var expandedSections by rememberSaveable(savePath) { mutableStateOf(setOf(SECTION_STATS, SECTION_FAMILY)) }
    return AccordionState(
        expandedSections = expandedSections,
        onToggle = { id -> expandedSections = toggleSection(expandedSections, id) },
    )
}

@Composable
private fun rememberChipState(savePath: String): ChipState {
    var activeChips by remember(savePath) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var expandedPersons by remember(savePath) { mutableStateOf<Set<String>>(emptySet()) }
    return ChipState(
        activeChips = activeChips,
        onChipSet = { sectionId, chipLabel ->
            activeChips = if (chipLabel == null) activeChips - sectionId else activeChips + (sectionId to chipLabel)
        },
        expandedPersons = expandedPersons,
        onTogglePerson = { key ->
            expandedPersons = if (key in expandedPersons) expandedPersons - key else expandedPersons + key
        },
    )
}

@Composable
private fun rememberPathSectionContent(
    save: BitLifeSaveSummary,
    draftValues: SnapshotStateMap<String, String>,
    onDraftChange: (SaveEditableField, String) -> Unit,
    searchContext: AdvancedSearchContext,
): Map<String, FieldListContent> =
    remember(save, draftValues, onDraftChange, searchContext) {
        ACCORDION_PATH_SECTIONS.associate { def ->
            def.id to
                FieldListContent(
                    fields = save.advancedFields.filterByPathPrefixes(def.prefixes),
                    metadataMap = searchContext.metadataMap,
                    draftValues = draftValues,
                    onDraftChange = onDraftChange,
                )
        }
    }

private data class BrowseContent(
    val accordion: AccordionState,
    val pathSectionContent: Map<String, FieldListContent>,
    val advancedContent: FieldListContent,
    val chipState: ChipState,
)

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.browseSections(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    content: BrowseContent,
) {
    saveSlotStatusItem(state = state)
    if (state.save.errorMessage != null) return

    accordionSection(
        section = AccordionSection(SECTION_STATS, "Stats", icon = "\uD83D\uDCCA", isPrimary = true),
        accordion = content.accordion,
    ) {
        item(contentType = "stats-panel") { SaveStatsTabContent(state = state, actions = actions) }
    }
    accordionSection(
        section = AccordionSection(SECTION_FAMILY, "Family", icon = "\uD83D\uDC65", isPrimary = true),
        accordion = content.accordion,
    ) {
        familyItems(state = state, actions = actions, chipState = content.chipState)
    }
    item(contentType = "primary-divider") { PrimarySectionDivider() }
    val visibleSections =
        ACCORDION_PATH_SECTIONS.filter { def ->
            val sectionContent = content.pathSectionContent[def.id]
            sectionContent != null && sectionContent.fields.isNotEmpty()
        }
    for (def in visibleSections) {
        pathFilterSection(def, content.accordion, content.pathSectionContent[def.id]!!, content.chipState)
    }
    accordionSection(
        section =
            AccordionSection(
                SECTION_ADVANCED,
                "Advanced",
                content.advancedContent.fields.size,
                icon = "\u2699\uFE0F",
            ),
        accordion = content.accordion,
    ) {
        items(
            items = content.advancedContent.fields,
            key = { field: SaveEditableField -> field.id },
            contentType = { field: SaveEditableField -> field.valueKind },
        ) { field: SaveEditableField ->
            SaveAdvancedFieldCard(
                field = field,
                draftValue = content.advancedContent.draftValues.valueFor(field),
                metadata = content.advancedContent.metadataMap[field.id] ?: field.computeMetadata(),
                onDraftChange = content.advancedContent.onDraftChange,
            )
        }
    }
}

private fun LazyListScope.familyItems(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    chipState: ChipState,
) {
    val characters = state.save.characters
    if (characters.isEmpty()) {
        item(contentType = "empty-family") { NotionEmptyMessage(text = "No characters in this save.") }
        return
    }
    characters.forEachIndexed { index, character ->
        val personKey = "${character.role}-$index"
        val isExpanded = personKey in chipState.expandedPersons
        item(key = "person-$personKey", contentType = "person-card") {
            PersonCard(
                character = character,
                isExpanded = isExpanded,
                onToggle = { chipState.onTogglePerson(personKey) },
                draftValues = state.draftValues,
                onDraftChange = actions.onDraftChange,
            )
        }
    }
}

@Composable
private fun PersonCard(
    character: SaveCharacterSummary,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    draftValues: SnapshotStateMap<String, String>,
    onDraftChange: (SaveEditableField, String) -> Unit,
) {
    NotionExpandableCard(
        content =
            ExpandableCardContent(
                title = character.name.ifBlank { character.role },
                subtitle = character.relationshipMeta(),
                leadingContent = { NotionAvatar(initials = character.name.ifBlank { character.role }) },
            ),
        isExpanded = isExpanded,
        onToggle = onToggle,
    ) {
        val identityFields = character.fields.filter { it.group != "Attributes" }
        val attributeFields = character.fields.filter { it.group == "Attributes" }
        if (identityFields.isNotEmpty()) {
            NotionSubGroupHeader(title = "Identity", count = identityFields.size)
            NotionFieldColumn(fields = identityFields, draftValues = draftValues, onDraftChange = onDraftChange)
        }
        if (attributeFields.isNotEmpty()) {
            NotionSubGroupHeader(title = "Attributes", count = attributeFields.size)
            NotionFieldColumn(fields = attributeFields, draftValues = draftValues, onDraftChange = onDraftChange)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.pathFilterSection(
    def: AccordionSectionDef,
    accordion: AccordionState,
    content: FieldListContent,
    chipState: ChipState,
) {
    val expanded = def.id in accordion.expandedSections
    stickyHeader(contentType = "section-${def.id}") {
        AccordionSectionHeader(
            content = SectionHeaderContent(title = def.title, fieldCount = content.fields.size, icon = def.icon),
            expanded = expanded,
            onClick = { accordion.onToggle(def.id) },
        )
    }
    if (expanded) {
        if (def.chips.isNotEmpty()) {
            item(contentType = "chips-${def.id}") {
                NotionFilterChips(
                    chips = def.chips.map { it.label },
                    selected = chipState.activeChips[def.id],
                    onSelect = { chipLabel -> chipState.onChipSet(def.id, chipLabel) },
                )
            }
        }
        val selectedChip = chipState.activeChips[def.id]
        val filteredFields =
            if (selectedChip != null) {
                val chipDef = def.chips.find { it.label == selectedChip }
                if (chipDef != null) content.fields.filterByPathPrefixes(chipDef.prefixes) else content.fields
            } else {
                content.fields
            }
        val booleanFields = filteredFields.filter { it.valueKind == SaveEditableValueKind.BOOLEAN }
        if (booleanFields.isNotEmpty()) {
            item(contentType = "bulk-${def.id}") {
                NotionBulkActions(
                    booleanCount = booleanFields.size,
                    onAllTrue = { booleanFields.forEach { content.onDraftChange(it, "True") } },
                    onAllFalse = { booleanFields.forEach { content.onDraftChange(it, "False") } },
                    onReset = { filteredFields.forEach { content.onDraftChange(it, it.value) } },
                )
            }
        }
        notionGroupedFieldItems(
            fields = filteredFields,
            draftValues = content.draftValues,
            onDraftChange = content.onDraftChange,
        )
    }
}

private fun LazyListScope.searchResultItems(
    results: List<SaveEditableField>,
    metadataMap: Map<String, FieldMetadata>,
    draftValues: SnapshotStateMap<String, String>,
    onDraftChange: (SaveEditableField, String) -> Unit,
) {
    item(contentType = "search-count") {
        Text(
            text = "${results.size} result${if (results.size != 1) "s" else ""}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = SEARCH_COUNT_ALPHA),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
    items(
        items = results,
        key = { field: SaveEditableField -> field.id },
        contentType = { field: SaveEditableField -> field.valueKind },
    ) { field: SaveEditableField ->
        SaveAdvancedFieldCard(
            field = field,
            draftValue = draftValues.valueFor(field),
            metadata = metadataMap[field.id] ?: field.computeMetadata(),
            onDraftChange = onDraftChange,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.accordionSection(
    section: AccordionSection,
    accordion: AccordionState,
    content: LazyListScope.() -> Unit,
) {
    val expanded = section.id in accordion.expandedSections
    stickyHeader(contentType = "section-${section.id}") {
        AccordionSectionHeader(
            content =
                SectionHeaderContent(
                    title = section.title,
                    fieldCount = section.fieldCount,
                    icon = section.icon,
                    isPrimary = section.isPrimary,
                ),
            expanded = expanded,
            onClick = { accordion.onToggle(section.id) },
        )
    }
    if (expanded) {
        content()
    }
}

@Composable
private fun NotionBulkActions(
    booleanCount: Int,
    onAllTrue: () -> Unit,
    onAllFalse: () -> Unit,
    onReset: () -> Unit,
) {
    if (booleanCount == 0) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        for ((label, onClick) in listOf("All True" to onAllTrue, "All False" to onAllFalse, "Reset" to onReset)) {
            TextButton(
                onClick = onClick,
                shape = BulkActionShape,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier =
                    Modifier
                        .weight(1f)
                        .background(color = Color.White.copy(alpha = BULK_ACTION_ALPHA), shape = BulkActionShape),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White.copy(alpha = BULK_ACTION_LABEL_ALPHA),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private val BulkActionShape = RoundedCornerShape(8.dp)
private const val BULK_ACTION_ALPHA = 0.06f
private const val BULK_ACTION_LABEL_ALPHA = 0.50f
