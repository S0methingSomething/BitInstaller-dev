package dev.bitinstaller.app.home

import com.github.terrakok.fuzzykot.ratio
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind

private const val FUZZY_MATCH_CUTOFF = 60
private const val SCORE_EXACT_LABEL = 1000
private const val SCORE_EXACT_MEMBER = 950
private const val SCORE_PREFIX_LABEL = 900
private const val SCORE_PREFIX_MEMBER = 850
private const val SCORE_SUBSTRING_TEXT = 800

private val whitespaceRegex = Regex("\\s+")

internal enum class AdvancedFieldFilter(
    val label: String,
    val description: String,
) {
    ALL("All", "Search every editable advanced field."),
    RECENT("Recent", "Only fields edited in this session."),
    EXPLAINED("Explained", "Only fields with known BitLife patterns."),
    ATTRIBUTES("Attributes", "Att_* stats, usually 0-100."),
    MONEY("Money", "Balances, prices, salaries, and cash values."),
    COUNTERS("Counters", "Num* lifetime and event counters."),
    FLAGS("Flags", "Boolean true/false state fields."),
    RISKY("Risky", "Internal, enum, identity, or cooldown fields."),
}

internal enum class AdvancedFieldSort(
    val label: String,
) {
    RECENT_FIRST("Recent first"),
    NAME("Name A-Z"),
    PATH("Path A-Z"),
    CATEGORY("Category"),
}

internal data class FilterConfig(
    val filter: AdvancedFieldFilter,
    val sort: AdvancedFieldSort,
    val categoryFilter: SaveFieldUiCategory? = null,
)

internal fun List<SaveEditableField>.filteredAndSorted(
    query: String,
    recentFieldIds: List<String>,
    config: FilterConfig,
    metadataMap: Map<String, FieldMetadata> = emptyMap(),
): List<SaveEditableField> {
    val needle = query.trim()
    val recentRank = recentFieldIds.withIndex().associate { (index, fieldId) -> fieldId to index }
    return mapNotNull { field ->
        val meta = metadataMap[field.id] ?: field.computeMetadata()
        if (field.shouldInclude(needle, config, recentFieldIds, meta)) {
            field to field.searchScore(needle, meta)
        } else {
            null
        }
    }.sortedWith(
        compareByDescending<Pair<SaveEditableField, Int>> { it.second }
            .then(
                config.sort.comparator(recentRank, metadataMap).let { c ->
                    Comparator { a, b -> c.compare(a.first, b.first) }
                },
            ),
    ).map { it.first }
}

private fun SaveEditableField.shouldInclude(
    needle: String,
    config: FilterConfig,
    recentFieldIds: List<String>,
    meta: FieldMetadata,
): Boolean {
    val matchesQuery = matchesQuery(needle, meta)
    val matchesFilter = matchesFilter(config.filter, recentFieldIds, meta)
    val matchesCategory = config.categoryFilter == null || meta.uiCategory == config.categoryFilter
    return matchesQuery && matchesFilter && matchesCategory
}

private fun SaveEditableField.searchText(meta: FieldMetadata): String =
    buildString {
        append(memberName)
        append(' ')
        append(label)
        append(' ')
        append(path)
        meta.explanation?.category?.let { append(' ').append(it) }
        meta.explanation?.description?.let { append(' ').append(it) }
    }

private fun SaveEditableField.matchesQuery(
    needle: String,
    meta: FieldMetadata,
): Boolean {
    if (needle.isBlank()) return true
    val tokens = needle.split(whitespaceRegex).filter { token -> token.isNotBlank() }
    val text = searchText(meta)
    return tokens.all { token ->
        text.contains(token, ignoreCase = true) || text.ratio(token) >= FUZZY_MATCH_CUTOFF
    }
}

private fun SaveEditableField.searchScore(
    needle: String,
    meta: FieldMetadata,
): Int {
    if (needle.isBlank()) return 0
    val tokens = needle.split(whitespaceRegex).filter { token -> token.isNotBlank() }
    val text = searchText(meta)
    return tokens.sumOf { token ->
        when {
            label.equals(token, ignoreCase = true) -> SCORE_EXACT_LABEL
            memberName.equals(token, ignoreCase = true) -> SCORE_EXACT_MEMBER
            label.startsWith(token, ignoreCase = true) -> SCORE_PREFIX_LABEL
            memberName.startsWith(token, ignoreCase = true) -> SCORE_PREFIX_MEMBER
            text.contains(token, ignoreCase = true) -> SCORE_SUBSTRING_TEXT
            else -> text.ratio(token)
        }
    } / tokens.size
}

private fun SaveEditableField.matchesFilter(
    filter: AdvancedFieldFilter,
    recentFieldIds: List<String>,
    meta: FieldMetadata,
): Boolean =
    when (filter) {
        AdvancedFieldFilter.ALL -> {
            true
        }

        AdvancedFieldFilter.RECENT -> {
            id in recentFieldIds
        }

        AdvancedFieldFilter.EXPLAINED -> {
            meta.explanation != null
        }

        AdvancedFieldFilter.ATTRIBUTES -> {
            meta.explanation?.category == "Attribute"
        }

        AdvancedFieldFilter.MONEY -> {
            meta.explanation?.category == "Money"
        }

        AdvancedFieldFilter.COUNTERS -> {
            meta.explanation?.category == "Counter"
        }

        AdvancedFieldFilter.FLAGS -> {
            valueKind == SaveEditableValueKind.BOOLEAN ||
                meta.explanation?.category in listOf("State flag", "Boolean")
        }

        AdvancedFieldFilter.RISKY -> {
            meta.explanation?.category in
                listOf(
                    "Cooldown / timing",
                    "Enum id",
                    "Identity / metadata",
                    "Rendering internal",
                )
        }
    }

private fun AdvancedFieldSort.comparator(
    recentRank: Map<String, Int>,
    metadataMap: Map<String, FieldMetadata>,
): Comparator<SaveEditableField> =
    when (this) {
        AdvancedFieldSort.RECENT_FIRST -> {
            compareBy<SaveEditableField> { field -> recentRank[field.id] ?: Int.MAX_VALUE }
                .thenBy { field -> field.path }
        }

        AdvancedFieldSort.NAME -> {
            compareBy<SaveEditableField> { field -> field.label }.thenBy { field -> field.path }
        }

        AdvancedFieldSort.PATH -> {
            compareBy { field -> field.path }
        }

        AdvancedFieldSort.CATEGORY -> {
            compareBy<SaveEditableField> { field ->
                metadataMap[field.id]?.uiCategory?.ordinal ?: field.uiCategory().ordinal
            }.thenBy { field -> field.path }
        }
    }
