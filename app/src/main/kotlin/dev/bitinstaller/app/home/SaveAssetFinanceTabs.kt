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

internal val CAREER_FIELD_PREFIXES =
    listOf(
        "Life / Occupation /",
        "Life / Past /",
        "Life / Part Time Job Array /",
    )

internal val HEALTH_FIELD_PREFIXES =
    listOf(
        "Life / Disease Array /",
        "Life / Addiction Array /",
        "Life / Diet /",
        "Life / Plastic Surgeries Array /",
    )

internal val SOCIAL_FIELD_PREFIXES =
    listOf(
        "Life / Rich Social Media /",
        "Life / Fame /",
        "Life / Model Headshot /",
    )

internal val INVESTMENTS_FIELD_PREFIXES =
    listOf(
        "Life / Investment Market /",
    )

internal val VAMPIRE_FIELD_PREFIXES =
    listOf(
        "Life / Hero / Vampire /",
    )

internal val RACING_FIELD_PREFIXES =
    listOf(
        "Life / Racing /",
        "Life / Racing Collectible Array /",
    )

internal val CHALLENGES_FIELD_PREFIXES =
    listOf(
        "Life / Challenge Tracker /",
    )

internal val ZOO_FIELD_PREFIXES =
    listOf(
        "Life / Zoo /",
    )

internal val LUXURY_FIELD_PREFIXES =
    listOf(
        "Life / Luxury /",
    )

internal fun List<SaveEditableField>.filterByPathPrefixes(prefixes: List<String>): List<SaveEditableField> =
    filter { field -> prefixes.any { prefix -> field.path.startsWith(prefix) } }
