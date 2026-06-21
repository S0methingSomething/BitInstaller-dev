package dev.bitinstaller.app.save

internal fun String.explanationByExtendedName(): SaveFieldExplanation? =
    accessoryExplanation()
        ?: namingExplanation()
        ?: vampireExplanation()
        ?: ageExplanation()
        ?: conditionExplanation()

private fun String.accessoryExplanation(): SaveFieldExplanation? =
    if (contains("Accessory") && contains("Index")) {
        SaveFieldExplanation(
            category = "Cosmetic index",
            description = "Sprite index for a cosmetic accessory. 0 means none. Invalid values may cause glitches.",
        )
    } else {
        null
    }

private fun String.namingExplanation(): SaveFieldExplanation? =
    when (this) {
        "RoyalTitle" -> {
            SaveFieldExplanation(
                category = "Naming flag",
                description = "Title prefix like King, Queen, Prince. Boolean — true to display title before name.",
            )
        }

        "ReversesNames" -> {
            SaveFieldExplanation(
                category = "Naming flag",
                description = "If true, the character displays surname before given name.",
            )
        }

        "IsCustom" -> {
            SaveFieldExplanation(
                category = "Naming flag",
                description = "Whether this is a custom-created character rather than a randomly generated one.",
            )
        }

        "HasDoctorate" -> {
            SaveFieldExplanation(
                category = "Naming flag",
                description = "If true, the character is titled Dr. Usually earned through graduate school.",
            )
        }

        else -> {
            null
        }
    }

private fun String.vampireExplanation(): SaveFieldExplanation? =
    when (this) {
        "VampireDiseaseCarrier" -> {
            SaveFieldExplanation(
                category = "State flag",
                description = "Whether the character carries vampirism without being a full vampire yet.",
            )
        }

        "IsDestinedToBecomeVampire" -> {
            SaveFieldExplanation(
                category = "State flag",
                description = "If true, the character is scripted to turn into a vampire. Overrides normal logic.",
            )
        }

        else -> {
            null
        }
    }

private fun String.ageExplanation(): SaveFieldExplanation? =
    if (this == "Age") {
        SaveFieldExplanation(
            category = "Attribute",
            description = "Current age in years. Editing this affects many cross-field dependencies.",
        )
    } else {
        null
    }

private fun String.conditionExplanation(): SaveFieldExplanation? =
    if (this == "Condition") {
        SaveFieldExplanation(
            category = "Asset condition",
            description = "Usually a 0-100 condition scale for cars, homes, or other owned assets.",
        )
    } else {
        null
    }
