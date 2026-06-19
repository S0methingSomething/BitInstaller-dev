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

private const val SECTION_STATS = "stats"
private const val SECTION_FAMILY = "family"
private const val SECTION_ASSETS = "assets"
private const val SECTION_FINANCE = "finance"
private const val SECTION_CAREER = "career"
private const val SECTION_HEALTH = "health"
private const val SECTION_SOCIAL = "social"
private const val SECTION_INVESTMENTS = "investments"
private const val SECTION_VAMPIRE = "vampire"
private const val SECTION_RACING = "racing"
private const val SECTION_CHALLENGES = "challenges"
private const val SECTION_ZOO = "zoo"
private const val SECTION_LUXURY = "luxury"
private const val SECTION_ADVANCED = "advanced"

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

private data class BrowseContentInput(
    val state: SaveSlotTabBodyState,
    val actions: SaveSlotTabBodyActions,
    val expandedSections: Set<String>,
    val onToggle: (String) -> Unit,
    val save: BitLifeSaveSummary,
    val searchContext: AdvancedSearchContext,
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
                content =
                    buildBrowseContent(
                        input =
                            BrowseContentInput(
                                state = state,
                                actions = actions,
                                expandedSections = expandedSections,
                                onToggle = { id -> expandedSections = toggleSection(expandedSections, id) },
                                save = save,
                                searchContext = searchContext,
                                draftValues = draftValues,
                                onDraftChange = onDraftChange,
                            ),
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

private fun buildBrowseContent(input: BrowseContentInput): BrowseSectionsContent {
    val meta = input.searchContext.metadataMap
    val save = input.save
    val draftValues = input.draftValues
    val onDraftChange = input.onDraftChange
    return BrowseSectionsContent(
        state = input.state,
        actions = input.actions,
        accordion = AccordionState(input.expandedSections, input.onToggle),
        assetContent = save.fieldList(ASSET_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        financeContent = save.fieldList(FINANCE_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        careerContent = save.fieldList(CAREER_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        healthContent = save.fieldList(HEALTH_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        socialContent = save.fieldList(SOCIAL_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        investmentsContent = save.fieldList(INVESTMENTS_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        vampireContent = save.fieldList(VAMPIRE_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        racingContent = save.fieldList(RACING_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        challengesContent = save.fieldList(CHALLENGES_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        zooContent = save.fieldList(ZOO_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        luxuryContent = save.fieldList(LUXURY_FIELD_PREFIXES, meta, draftValues, onDraftChange),
        advancedContent = FieldListContent(save.advancedFields, meta, draftValues, onDraftChange),
    )
}

private fun BitLifeSaveSummary.fieldList(
    prefixes: List<String>,
    meta: Map<String, FieldMetadata>,
    draftValues: SnapshotStateMap<String, String>,
    onDraftChange: (SaveEditableField, String) -> Unit,
): FieldListContent =
    FieldListContent(
        advancedFields.filterByPathPrefixes(prefixes),
        meta,
        draftValues,
        onDraftChange,
    )

private data class BrowseSectionsContent(
    val state: SaveSlotTabBodyState,
    val actions: SaveSlotTabBodyActions,
    val accordion: AccordionState,
    val assetContent: FieldListContent,
    val financeContent: FieldListContent,
    val careerContent: FieldListContent,
    val healthContent: FieldListContent,
    val socialContent: FieldListContent,
    val investmentsContent: FieldListContent,
    val vampireContent: FieldListContent,
    val racingContent: FieldListContent,
    val challengesContent: FieldListContent,
    val zooContent: FieldListContent,
    val luxuryContent: FieldListContent,
    val advancedContent: FieldListContent,
)

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.browseSections(content: BrowseSectionsContent) {
    val state = content.state
    val actions = content.actions
    val accordion = content.accordion
    saveSlotStatusItem(state = state)
    if (state.save.errorMessage != null) return

    accordionSection(section = AccordionSection(SECTION_STATS, "Stats"), accordion = accordion) {
        item(contentType = "stats-panel") { SaveStatsTabContent(state = state, actions = actions) }
    }
    accordionSection(section = AccordionSection(SECTION_FAMILY, "Family"), accordion = accordion) {
        item(contentType = "people-panel") { SavePeopleTabContent(state = state, actions = actions) }
    }
    pathFilterSection(SECTION_ASSETS, "Assets", accordion, content.assetContent)
    pathFilterSection(SECTION_FINANCE, "Finance", accordion, content.financeContent)
    pathFilterSection(SECTION_CAREER, "Career", accordion, content.careerContent)
    pathFilterSection(SECTION_HEALTH, "Health", accordion, content.healthContent)
    pathFilterSection(SECTION_SOCIAL, "Social & Fame", accordion, content.socialContent)
    pathFilterSection(SECTION_INVESTMENTS, "Investments", accordion, content.investmentsContent)
    pathFilterSection(SECTION_VAMPIRE, "Vampire", accordion, content.vampireContent)
    pathFilterSection(SECTION_RACING, "Racing", accordion, content.racingContent)
    pathFilterSection(SECTION_CHALLENGES, "Challenges", accordion, content.challengesContent)
    pathFilterSection(SECTION_ZOO, "Zoo", accordion, content.zooContent)
    pathFilterSection(SECTION_LUXURY, "Luxury", accordion, content.luxuryContent)
    accordionSection(
        section = AccordionSection(SECTION_ADVANCED, "Advanced", content.advancedContent.fields.size),
        accordion = accordion,
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

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.pathFilterSection(
    id: String,
    title: String,
    accordion: AccordionState,
    content: FieldListContent,
) {
    accordionSection(section = AccordionSection(id, title, content.fields.size), accordion = accordion) {
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
