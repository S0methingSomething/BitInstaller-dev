package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal data class AdvancedFieldResult(
    val fields: List<SaveEditableField>,
    val isComputing: Boolean,
)

internal const val ADVANCED_DEBOUNCE_MS = 120L

@OptIn(FlowPreview::class)
internal fun queryFlow(query: Flow<String>): Flow<String> =
    query
        .debounce(ADVANCED_DEBOUNCE_MS)
        .distinctUntilChanged()

internal suspend fun computeFields(
    save: BitLifeSaveSummary,
    recentFieldIds: List<String>,
    query: String,
    category: SaveFieldUiCategory?,
): AdvancedFieldResult =
    withContext(Dispatchers.Default) {
        val fields =
            save.advancedFields.filteredAndSorted(
                query = query,
                recentFieldIds = recentFieldIds,
                filter = AdvancedFieldFilter.ALL,
                sort = AdvancedFieldSort.CATEGORY,
                categoryFilter = category,
            )
        AdvancedFieldResult(fields = fields, isComputing = false)
    }
