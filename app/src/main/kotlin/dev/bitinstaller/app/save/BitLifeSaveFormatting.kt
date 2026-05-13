package dev.bitinstaller.app.save

import java.util.Locale

internal fun MutableList<SaveFactSummary>.addFact(
    label: String,
    value: String?,
) {
    if (!value.isNullOrBlank()) {
        add(SaveFactSummary(label = label, value = value))
    }
}

internal fun displayName(
    firstName: String,
    lastName: String,
    royalTitle: String,
    hasDoctorate: Boolean,
): String {
    val baseName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
    val titlePrefix = royalTitle.takeIf { it.isNotBlank() }
    val doctorPrefix = "Dr.".takeIf { hasDoctorate }
    return listOfNotNull(titlePrefix, doctorPrefix, baseName.takeIf { it.isNotBlank() })
        .joinToString(" ")
        .ifBlank { "Unnamed life" }
}

internal fun Int.toGenderLabel(): String =
    when (this) {
        0 -> "Male"
        1 -> "Female"
        else -> "Gender $this"
    }

internal fun Double.toWholeNumberLabel(): String = String.format(Locale.US, "%,.0f", this)
