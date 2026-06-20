package dev.bitinstaller.app.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

internal const val ADVANCED_DEBOUNCE_MS = 120L

@Composable
internal fun rememberSearchContext(save: BitLifeSaveSummary): AdvancedSearchContext =
    remember(save) {
        val metadataMap = save.advancedFields.associate { it.id to it.computeMetadata() }
        AdvancedSearchContext(metadataMap, FieldSearchIndex.build(save.advancedFields, metadataMap))
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
