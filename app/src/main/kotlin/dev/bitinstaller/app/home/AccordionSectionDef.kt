package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.SaveEditableField

internal data class AccordionSectionDef(
    val id: String,
    val title: String,
    val prefixes: List<String>,
)

internal val ACCORDION_PATH_SECTIONS: List<AccordionSectionDef> =
    listOf(
        AccordionSectionDef(
            id = "assets",
            title = "Assets",
            prefixes =
                listOf(
                    "Life / Car Array /",
                    "Life / House Array /",
                    "Life / Aircraft Array /",
                    "Life / Watercraft Array /",
                    "Life / Recreational Vehicle Array /",
                    "Life / Jewelry Array /",
                    "Life / Heirloom Array /",
                ),
        ),
        AccordionSectionDef(
            id = "finance",
            title = "Finance",
            prefixes =
                listOf(
                    "Life / Finances /",
                    "Life / Portfolio /",
                    "Life / Landlord Portfolio /",
                ),
        ),
        AccordionSectionDef(
            id = "career",
            title = "Career",
            prefixes =
                listOf(
                    "Life / Occupation /",
                    "Life / Past /",
                    "Life / Part Time Job Array /",
                ),
        ),
        AccordionSectionDef(
            id = "health",
            title = "Health",
            prefixes =
                listOf(
                    "Life / Disease Array /",
                    "Life / Addiction Array /",
                    "Life / Diet /",
                    "Life / Plastic Surgeries Array /",
                ),
        ),
        AccordionSectionDef(
            id = "social",
            title = "Social & Fame",
            prefixes =
                listOf(
                    "Life / Rich Social Media /",
                    "Life / Fame /",
                    "Life / Model Headshot /",
                ),
        ),
        AccordionSectionDef(
            id = "investments",
            title = "Investments",
            prefixes = listOf("Life / Investment Market /"),
        ),
        AccordionSectionDef(
            id = "vampire",
            title = "Vampire",
            prefixes = listOf("Life / Hero / Vampire /"),
        ),
        AccordionSectionDef(
            id = "racing",
            title = "Racing",
            prefixes =
                listOf(
                    "Life / Racing /",
                    "Life / Racing Collectible Array /",
                ),
        ),
        AccordionSectionDef(
            id = "challenges",
            title = "Challenges",
            prefixes = listOf("Life / Challenge Tracker /"),
        ),
        AccordionSectionDef(
            id = "zoo",
            title = "Zoo",
            prefixes = listOf("Life / Zoo /"),
        ),
        AccordionSectionDef(
            id = "luxury",
            title = "Luxury",
            prefixes = listOf("Life / Luxury /"),
        ),
    )

internal const val SECTION_STATS = "stats"
internal const val SECTION_FAMILY = "family"
internal const val SECTION_ADVANCED = "advanced"

internal fun List<SaveEditableField>.filterByPathPrefixes(prefixes: List<String>): List<SaveEditableField> =
    filter { field -> prefixes.any { prefix -> field.path.startsWith(prefix) } }
