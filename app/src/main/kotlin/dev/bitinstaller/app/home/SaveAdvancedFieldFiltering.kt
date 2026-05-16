package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind
import dev.bitinstaller.app.save.explanation

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
): List<SaveEditableField> {
    val needle = query.trim()
    val recentRank = recentFieldIds.withIndex().associate { (index, fieldId) -> fieldId to index }
    return filter { field ->
        val hint = field.explanation()
        field.matchesQuery(needle, hint) && field.matchesFilter(filter, recentFieldIds, hint?.category.orEmpty())
    }.sortedWith(sort.comparator(recentRank))
}

private fun SaveEditableField.matchesQuery(
    needle: String,
    hint: dev.bitinstaller.app.save.SaveFieldExplanation?,
): Boolean =
    needle.isBlank() ||
        path.contains(needle, ignoreCase = true) ||
        label.contains(needle, ignoreCase = true) ||
        memberName.contains(needle, ignoreCase = true) ||
        value.contains(needle, ignoreCase = true) ||
        hint?.category?.contains(needle, ignoreCase = true) == true ||
        hint?.description?.contains(needle, ignoreCase = true) == true

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
