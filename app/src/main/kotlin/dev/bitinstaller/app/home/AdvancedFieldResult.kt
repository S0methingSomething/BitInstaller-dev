package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal data class AdvancedFieldResult(
    val fields: List<SaveEditableField>,
    val isComputing: Boolean,
)

internal const val ADVANCED_DEBOUNCE_MS = 120L
private const val FIELD_CHUNK_SIZE = 60

@OptIn(FlowPreview::class)
internal fun queryFlow(query: Flow<String>): Flow<String> =
    query
        .debounce { q -> if (q.isEmpty()) 0L else ADVANCED_DEBOUNCE_MS }
        .distinctUntilChanged()

internal suspend fun computeFields(
    save: BitLifeSaveSummary,
    recentFieldIds: List<String>,
    query: String,
    category: SaveFieldUiCategory?,
    metadataMap: Map<String, FieldMetadata> = emptyMap(),
): AdvancedFieldResult =
    kotlinx.coroutines.withContext(Dispatchers.Default) {
        val fields =
            save.advancedFields.filteredAndSorted(
                query = query,
                recentFieldIds = recentFieldIds,
                config =
                    FilterConfig(
                        filter = AdvancedFieldFilter.ALL,
                        sort = AdvancedFieldSort.CATEGORY,
                        categoryFilter = category,
                    ),
                metadataMap = metadataMap,
            )
        AdvancedFieldResult(fields = fields, isComputing = false)
    }

internal fun computeFieldsFlow(
    save: BitLifeSaveSummary,
    recentFieldIds: List<String>,
    query: String,
    category: SaveFieldUiCategory?,
    metadataMap: Map<String, FieldMetadata>,
): Flow<List<SaveEditableField>> =
    flow {
        val results =
            save.advancedFields.filteredAndSorted(
                query = query,
                recentFieldIds = recentFieldIds,
                config =
                    FilterConfig(
                        filter = AdvancedFieldFilter.ALL,
                        sort = AdvancedFieldSort.CATEGORY,
                        categoryFilter = category,
                    ),
                metadataMap = metadataMap,
            )
        results.chunked(FIELD_CHUNK_SIZE).forEach { chunk -> emit(chunk) }
    }.flowOn(Dispatchers.Default)
