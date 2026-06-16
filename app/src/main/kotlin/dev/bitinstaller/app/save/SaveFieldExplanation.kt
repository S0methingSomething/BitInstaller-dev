package dev.bitinstaller.app.save

internal data class SaveFieldExplanation(
    val category: String,
    val description: String,
)

private val backingFieldRegex = Regex("<([^>]+)>k__BackingField")

internal fun SaveEditableField.explanation(): SaveFieldExplanation? {
    val name =
        memberName
            .substringAfterLast('+')
            .replace(backingFieldRegex, "$1")
            .removePrefix("_")
    return name.explanationByName()
        ?: when (valueKind) {
            SaveEditableValueKind.BOOLEAN -> {
                SaveFieldExplanation(
                    category = "Boolean",
                    description = "On/off value. Use true or false.",
                )
            }

            else -> {
                null
            }
        }
}

private fun String.explanationByName(): SaveFieldExplanation? =
    attributeExplanation()
        ?: timingExplanation()
        ?: moneyExplanation()
        ?: enumExplanation()
        ?: dangerousExplanation()
        ?: renderingExplanation()
        ?: counterExplanation()
        ?: stateFlagExplanation()
        ?: explanationByExtendedName()

private fun String.attributeExplanation(): SaveFieldExplanation? {
    if (!startsWith("Att_")) return null
    val label = removePrefix("Att_").replace('_', ' ')
    return SaveFieldExplanation(
        category = "Attribute",
        description =
            "$label usually uses a 0-100 stat scale. " +
                "Extreme values can make lives behave strangely.",
    )
}

private fun String.timingExplanation(): SaveFieldExplanation? =
    if (startsWith("AgeAtLast") || startsWith("HeroAgeAtLast")) {
        SaveFieldExplanation(
            category = "Cooldown / timing",
            description = "Tracks the age when an event last happened. Editing it can reset cooldowns.",
        )
    } else {
        null
    }

private fun String.moneyExplanation(): SaveFieldExplanation? =
    when {
        this == "BankBalance" -> {
            SaveFieldExplanation(
                category = "Money",
                description = "Main cash balance. This is stored as a Double, so decimal values are valid.",
            )
        }

        contains("Price") || contains("Salary") || contains("Balance") -> {
            SaveFieldExplanation(
                category = "Money",
                description = "Financial amount used by jobs, assets, purchases, or balances.",
            )
        }

        else -> {
            null
        }
    }

private fun String.enumExplanation(): SaveFieldExplanation? =
    when (this) {
        "RelationshipStatus",
        "Gender",
        "Sexuality",
        "OccupationType",
        -> {
            SaveFieldExplanation(
                category = "Enum id",
                description = "Numeric choice id. Random values may point to unknown game options.",
            )
        }

        else -> {
            null
        }
    }

private fun String.dangerousExplanation(): SaveFieldExplanation? =
    when (this) {
        "_lifeId",
        "MetaGenerationID",
        "MetaLastBootupVersion",
        -> {
            SaveFieldExplanation(
                category = "Identity / metadata",
                description = "High-risk internal identity data. Avoid editing unless you know the save structure.",
            )
        }

        else -> {
            null
        }
    }

private fun String.renderingExplanation(): SaveFieldExplanation? =
    when (this) {
        "_Script",
        "EmojiScript",
        "Proxy",
        -> {
            SaveFieldExplanation(
                category = "Rendering internal",
                description = "Used by game UI or text rendering. Usually not useful for gameplay edits.",
            )
        }

        else -> {
            null
        }
    }

private fun String.counterExplanation(): SaveFieldExplanation? =
    if (startsWith("Num")) {
        SaveFieldExplanation(
            category = "Counter",
            description = "Lifetime or event counter. Often affects achievements, stats, or game history.",
        )
    } else {
        null
    }

private fun String.stateFlagExplanation(): SaveFieldExplanation? =
    when (this) {
        "Alive",
        "Vampire",
        "OnBirthControl",
        -> {
            SaveFieldExplanation(
                category = "State flag",
                description = "Boolean on/off state. Use true or false.",
            )
        }

        else -> {
            null
        }
    }
