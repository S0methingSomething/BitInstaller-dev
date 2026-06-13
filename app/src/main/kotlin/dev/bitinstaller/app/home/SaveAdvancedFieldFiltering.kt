package dev.bitinstaller.app.home

import com.github.terrakok.fuzzykot.ratio
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind
import dev.bitinstaller.app.save.explanation

private const val FUZZY_MATCH_CUTOFF = 60
private const val SCORE_EXACT_LABEL = 1000
private const val SCORE_EXACT_MEMBER = 950
private const val SCORE_PREFIX_LABEL = 900
private const val SCORE_PREFIX_MEMBER = 850
private const val SCORE_SUBSTRING_TEXT = 800

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

internal fun List<SaveEditableField>.filteredAndSorted(
    query: String,
    recentFieldIds: List<String>,
    filter: AdvancedFieldFilter,
    sort: AdvancedFieldSort,
    categoryFilter: SaveFieldUiCategory? = null,
): List<SaveEditableField> {
    val needle = query.trim()
    val recentRank = recentFieldIds.withIndex().associate { (index, fieldId) -> fieldId to index }
    return mapNotNull { field ->
        val hint = field.explanation()
        if (field.shouldInclude(needle, filter, recentFieldIds, categoryFilter, hint)) {
            field to field.searchScore(needle, hint)
        } else {
            null
        }
    }.sortedWith(
        compareByDescending<Pair<SaveEditableField, Int>> { it.second }
            .then(sort.comparator(recentRank).let { c -> Comparator { a, b -> c.compare(a.first, b.first) } }),
    ).map { it.first }
}

private fun SaveEditableField.shouldInclude(
    needle: String,
    filter: AdvancedFieldFilter,
    recentFieldIds: List<String>,
    categoryFilter: SaveFieldUiCategory?,
    hint: dev.bitinstaller.app.save.SaveFieldExplanation?,
): Boolean {
    val matchesQuery = matchesQuery(needle, hint)
    val matchesFilter = matchesFilter(filter, recentFieldIds, hint?.category.orEmpty())
    val matchesCategory = categoryFilter == null || uiCategory() == categoryFilter
    return matchesQuery && matchesFilter && matchesCategory
}

private fun SaveEditableField.searchText(hint: dev.bitinstaller.app.save.SaveFieldExplanation?): String =
    buildString {
        append(memberName)
        append(' ')
        append(label)
        append(' ')
        append(path)
        hint?.category?.let { append(' ').append(it) }
        hint?.description?.let { append(' ').append(it) }
    }

private fun SaveEditableField.matchesQuery(
    needle: String,
    hint: dev.bitinstaller.app.save.SaveFieldExplanation?,
): Boolean {
    if (needle.isBlank()) return true
    val tokens = needle.split(Regex("\\s+")).filter { token -> token.isNotBlank() }
    val text = searchText(hint)
    return tokens.all { token ->
        text.contains(token, ignoreCase = true) || text.ratio(token) >= FUZZY_MATCH_CUTOFF
    }
}

private fun SaveEditableField.searchScore(
    needle: String,
    hint: dev.bitinstaller.app.save.SaveFieldExplanation?,
): Int {
    if (needle.isBlank()) return 0
    val tokens = needle.split(Regex("\\s+")).filter { token -> token.isNotBlank() }
    val text = searchText(hint)
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
    category: String,
): Boolean =
    when (filter) {
        AdvancedFieldFilter.ALL -> {
            true
        }

        AdvancedFieldFilter.RECENT -> {
            id in recentFieldIds
        }

        AdvancedFieldFilter.EXPLAINED -> {
            category.isNotEmpty()
        }

        AdvancedFieldFilter.ATTRIBUTES -> {
            category == "Attribute"
        }

        AdvancedFieldFilter.MONEY -> {
            category == "Money"
        }

        AdvancedFieldFilter.COUNTERS -> {
            category == "Counter"
        }

        AdvancedFieldFilter.FLAGS -> {
            valueKind == SaveEditableValueKind.BOOLEAN || category in listOf("State flag", "Boolean")
        }

        AdvancedFieldFilter.RISKY -> {
            category in listOf("Cooldown / timing", "Enum id", "Identity / metadata", "Rendering internal")
        }
    }

private fun AdvancedFieldSort.comparator(recentRank: Map<String, Int>): Comparator<SaveEditableField> =
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
            compareBy<SaveEditableField> { field -> field.explanation()?.category.orEmpty() }
                .thenBy { field -> field.path }
        }
    }
