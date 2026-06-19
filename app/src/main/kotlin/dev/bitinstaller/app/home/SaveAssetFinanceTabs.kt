package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.SaveEditableField

internal val ASSET_FIELD_PREFIXES =
    listOf(
        "Life / Car Array /",
        "Life / House Array /",
        "Life / Aircraft Array /",
        "Life / Watercraft Array /",
        "Life / Recreational Vehicle Array /",
        "Life / Jewelry Array /",
        "Life / Heirloom Array /",
    )

internal val FINANCE_FIELD_PREFIXES =
    listOf(
        "Life / Finances /",
        "Life / Portfolio /",
        "Life / Landlord Portfolio /",
    )

internal fun List<SaveEditableField>.filterByPathPrefixes(prefixes: List<String>): List<SaveEditableField> =
    filter { field -> prefixes.any { prefix -> field.path.startsWith(prefix) } }
