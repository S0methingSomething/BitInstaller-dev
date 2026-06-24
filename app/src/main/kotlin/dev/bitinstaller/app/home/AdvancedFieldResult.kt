package dev.bitinstaller.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

internal const val ADVANCED_DEBOUNCE_MS = 120L

@Composable
internal fun rememberSearchContext(save: BitLifeSaveSummary): State<AdvancedSearchContext?> =
    produceState<AdvancedSearchContext?>(initialValue = null, save.advancedFields, save.advancedFieldsParsed) {
        if (save.advancedFields.isEmpty()) {
            // If parsing already happened (advancedFieldsParsed=true) and there really are no
            // advanced fields, emit an empty context so the editor treats the save as empty rather
            // than pending. If parsing hasn't happened yet (advancedFieldsParsed=false) stay null
            // so the editor shows the "Loading fields…" state until the lazy-load path upserts a
            // parsed summary (which restarts this produceState with the new keys).
            if (save.advancedFieldsParsed) {
                value = AdvancedSearchContext(emptyMap(), FieldSearchIndex(emptyMap(), emptyList()))
            }
        } else {
            value =
                withContext(Dispatchers.Default) {
                    val metadataMap = save.advancedFields.associate { it.id to it.computeMetadata() }
                    AdvancedSearchContext(metadataMap, FieldSearchIndex.build(save.advancedFields, metadataMap))
                }
        }
    }

@Composable
internal fun rememberSearchResults(
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
                        config = FilterConfig(filter = AdvancedFieldFilter.ALL, sort = AdvancedFieldSort.CATEGORY),
                        metadataMap = searchContext.metadataMap,
                        searchIndex = searchContext.searchIndex,
                    )
                }
        }
    }
