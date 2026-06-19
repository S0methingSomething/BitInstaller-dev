package dev.bitinstaller.app.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val SEARCH_COUNT_ALPHA = 0.3f

private data class AccordionSection(
    val id: String,
    val title: String,
    val fieldCount: Int? = null,
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
    var expandedSections by rememberSaveable(save.path) {
        mutableStateOf(setOf(SECTION_STATS, SECTION_FAMILY))
    }

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
                accordion =
                    AccordionState(
                        expandedSections = expandedSections,
                        onToggle = { id -> expandedSections = toggleSection(expandedSections, id) },
                    ),
                pathSectionContent = pathSectionContent,
                advancedContent =
                    FieldListContent(
                        fields = save.advancedFields,
                        metadataMap = searchContext.metadataMap,
                        draftValues = draftValues,
                        onDraftChange = onDraftChange,
                    ),
            )
        } else {
            searchResultItems(searchResults.value, searchContext.metadataMap, draftValues, onDraftChange)
        }
    }
}

@Composable
private fun rememberSearchContext(save: BitLifeSaveSummary): AdvancedSearchContext =
    remember(save) {
        val metadataMap = save.advancedFields.associate { it.id to it.computeMetadata() }
        AdvancedSearchContext(metadataMap, FieldSearchIndex.build(save.advancedFields, metadataMap))
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

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.browseSections(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    accordion: AccordionState,
    pathSectionContent: Map<String, FieldListContent>,
    advancedContent: FieldListContent,
) {
    saveSlotStatusItem(state = state)
    if (state.save.errorMessage != null) return

    accordionSection(section = AccordionSection(SECTION_STATS, "Stats"), accordion = accordion) {
        item(contentType = "stats-panel") { SaveStatsTabContent(state = state, actions = actions) }
    }
    accordionSection(section = AccordionSection(SECTION_FAMILY, "Family"), accordion = accordion) {
        item(contentType = "people-panel") { SavePeopleTabContent(state = state, actions = actions) }
    }
    for (def in ACCORDION_PATH_SECTIONS) {
        pathFilterSection(def, accordion, pathSectionContent[def.id]!!)
    }
    accordionSection(
        section = AccordionSection(SECTION_ADVANCED, "Advanced", advancedContent.fields.size),
        accordion = accordion,
    ) {
        items(
            items = advancedContent.fields,
            key = { field: SaveEditableField -> field.id },
            contentType = { field: SaveEditableField -> field.valueKind },
        ) { field: SaveEditableField ->
            SaveAdvancedFieldCard(
                field = field,
                draftValue = advancedContent.draftValues.valueFor(field),
                metadata = advancedContent.metadataMap[field.id] ?: field.computeMetadata(),
                onDraftChange = advancedContent.onDraftChange,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.pathFilterSection(
    def: AccordionSectionDef,
    accordion: AccordionState,
    content: FieldListContent,
) {
    accordionSection(section = AccordionSection(def.id, def.title, content.fields.size), accordion = accordion) {
        groupedFieldItems(content = content)
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

@Composable
private fun rememberSearchResults(
    query: String,
    save: BitLifeSaveSummary,
    recentFieldIds: List<String>,
    searchContext: AdvancedSearchContext,
): State<List<SaveEditableField>> =
    produceState(initialValue = emptyList<SaveEditableField>(), query) {
        if (query.isBlank()) {
            value = emptyList()
        } else {
            delay(ADVANCED_DEBOUNCE_MS)
            value =
                withContext(Dispatchers.Default) {
                    save.editableFields().filteredAndSorted(
                        query = query,
                        recentFieldIds = recentFieldIds,
                        config =
                            FilterConfig(
                                filter = AdvancedFieldFilter.ALL,
                                sort = AdvancedFieldSort.CATEGORY,
                            ),
                        metadataMap = searchContext.metadataMap,
                        searchIndex = searchContext.searchIndex,
                    )
                }
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
            title = section.title,
            fieldCount = section.fieldCount,
            expanded = expanded,
            onClick = { accordion.onToggle(section.id) },
        )
    }
    if (expanded) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.groupedFieldItems(content: FieldListContent) {
    if (content.fields.isEmpty()) {
        item(contentType = "empty") {
            Text(
                text = "No fields found.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.padding(16.dp),
            )
        }
        return
    }
    val grouped = content.fields.groupBy { it.group }.toSortedMap()
    grouped.forEach { (groupName, groupFields) ->
        item(contentType = "subgroup-$groupName") {
            AccordionSubGroupHeader(title = groupName)
        }
        items(
            items = groupFields,
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
