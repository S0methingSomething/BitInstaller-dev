package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.SaveEditableField

internal data class SectionChipDef(
    val label: String,
    val prefixes: List<String>,
)

internal data class AccordionSectionDef(
    val id: String,
    val title: String,
    val icon: String,
    val prefixes: List<String>,
    val isPrimary: Boolean = false,
    val chips: List<SectionChipDef> = emptyList(),
)

internal val ACCORDION_PATH_SECTIONS: List<AccordionSectionDef> =
    listOf(
        AccordionSectionDef(
            id = "assets",
            title = "Assets",
            icon = "\uD83C\uDFE0",
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
            chips =
                listOf(
                    SectionChipDef("Cars", listOf("Life / Car Array /")),
                    SectionChipDef("Houses", listOf("Life / House Array /")),
                    SectionChipDef("Aircraft", listOf("Life / Aircraft Array /")),
                    SectionChipDef("Watercraft", listOf("Life / Watercraft Array /")),
                    SectionChipDef("RV", listOf("Life / Recreational Vehicle Array /")),
                    SectionChipDef("Jewelry", listOf("Life / Jewelry Array /")),
                    SectionChipDef("Heirlooms", listOf("Life / Heirloom Array /")),
                ),
        ),
        AccordionSectionDef(
            id = "finance",
            title = "Finance",
            icon = "\uD83D\uDCB0",
            prefixes =
                listOf(
                    "Life / Finances /",
                    "Life / Portfolio /",
                    "Life / Landlord Portfolio /",
                ),
            chips =
                listOf(
                    SectionChipDef("Finances", listOf("Life / Finances /")),
                    SectionChipDef("Portfolio", listOf("Life / Portfolio /")),
                    SectionChipDef("Landlord", listOf("Life / Landlord Portfolio /")),
                ),
        ),
        AccordionSectionDef(
            id = "career",
            title = "Career",
            icon = "\uD83D\uDCBC",
            prefixes =
                listOf(
                    "Life / Occupation /",
                    "Life / Past /",
                    "Life / Part Time Job Array /",
                ),
            chips =
                listOf(
                    SectionChipDef("Current", listOf("Life / Occupation /")),
                    SectionChipDef("History", listOf("Life / Past /")),
                    SectionChipDef("Part-Time", listOf("Life / Part Time Job Array /")),
                ),
        ),
        AccordionSectionDef(
            id = "health",
            title = "Health",
            icon = "\uD83C\uDFE5",
            prefixes =
                listOf(
                    "Life / Disease Array /",
                    "Life / Addiction Array /",
                    "Life / Diet /",
                    "Life / Plastic Surgeries Array /",
                ),
            chips =
                listOf(
                    SectionChipDef("Diseases", listOf("Life / Disease Array /")),
                    SectionChipDef("Addictions", listOf("Life / Addiction Array /")),
                    SectionChipDef("Diet", listOf("Life / Diet /")),
                    SectionChipDef("Plastic Surgery", listOf("Life / Plastic Surgeries Array /")),
                ),
        ),
        AccordionSectionDef(
            id = "social",
            title = "Social & Fame",
            icon = "\uD83D\uDCF1",
            prefixes =
                listOf(
                    "Life / Rich Social Media /",
                    "Life / Fame /",
                    "Life / Model Headshot /",
                ),
            chips =
                listOf(
                    SectionChipDef("Social Media", listOf("Life / Rich Social Media /")),
                    SectionChipDef("Fame", listOf("Life / Fame /")),
                    SectionChipDef("Model", listOf("Life / Model Headshot /")),
                ),
        ),
        AccordionSectionDef(
            id = "investments",
            title = "Investments",
            icon = "\uD83D\uDCC8",
            prefixes = listOf("Life / Investment Market /"),
        ),
        AccordionSectionDef(
            id = "vampire",
            title = "Vampire",
            icon = "\uD83E\uDDDB",
            prefixes = listOf("Life / Hero / Vampire /"),
        ),
        AccordionSectionDef(
            id = "racing",
            title = "Racing",
            icon = "\uD83C\uDFC1",
            prefixes =
                listOf(
                    "Life / Racing /",
                    "Life / Racing Collectible Array /",
                ),
            chips =
                listOf(
                    SectionChipDef("Racing", listOf("Life / Racing /")),
                    SectionChipDef("Collectibles", listOf("Life / Racing Collectible Array /")),
                ),
        ),
        AccordionSectionDef(
            id = "challenges",
            title = "Challenges",
            icon = "\uD83C\uDFC6",
            prefixes = listOf("Life / Challenge Tracker /"),
        ),
        AccordionSectionDef(
            id = "zoo",
            title = "Zoo",
            icon = "\uD83E\uDD81",
            prefixes = listOf("Life / Zoo /"),
        ),
        AccordionSectionDef(
            id = "luxury",
            title = "Luxury",
            icon = "\uD83D\uDC8E",
            prefixes = listOf("Life / Luxury /"),
        ),
    )

internal val PRIMARY_SECTIONS: List<AccordionSectionDef> =
    listOf(
        AccordionSectionDef(
            id = SECTION_STATS,
            title = "Stats",
            icon = "\uD83D\uDCCA",
            prefixes = emptyList(),
            isPrimary = true,
        ),
        AccordionSectionDef(
            id = SECTION_FAMILY,
            title = "Family",
            icon = "\uD83D\uDC65",
            prefixes = emptyList(),
            isPrimary = true,
        ),
    )

internal const val SECTION_STATS = "stats"
internal const val SECTION_FAMILY = "family"
internal const val SECTION_ADVANCED = "advanced"

internal fun List<SaveEditableField>.filterByPathPrefixes(prefixes: List<String>): List<SaveEditableField> =
    filter { field -> prefixes.any { prefix -> field.path.startsWith(prefix) } }
