package dev.bitinstaller.app.home

import androidx.compose.ui.graphics.Color
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.explanation

private const val COLOR_RISK_SAFE_ARGB = 0xFF4CAF50L
private const val COLOR_RISK_MEDIUM_ARGB = 0xFFFFC107L
private const val COLOR_RISK_DANGER_ARGB = 0xFFF44336L

internal enum class SaveFieldUiRisk(
    val label: String,
    val colorArgb: Long,
) {
    SAFE("Safe", COLOR_RISK_SAFE_ARGB),
    MEDIUM("Medium", COLOR_RISK_MEDIUM_ARGB),
    DANGER("Danger", COLOR_RISK_DANGER_ARGB),
}

internal fun SaveEditableField.uiRisk(): SaveFieldUiRisk {
    val category = explanation()?.category
    val member = memberName.lowercase()

    return when {
        category.isDangerousCategory() || member.isDangerousField() -> SaveFieldUiRisk.DANGER
        category.isMediumRiskCategory() -> SaveFieldUiRisk.MEDIUM
        else -> SaveFieldUiRisk.SAFE
    }
}

private fun String?.isDangerousCategory(): Boolean =
    this == "Identity / metadata" || this == "Rendering internal" || this == "Enum id"

private fun String.isDangerousField(): Boolean = this == "_lifeid" || this == "metagenerationid"

private fun String?.isMediumRiskCategory(): Boolean =
    this == "Cooldown / timing" || this == "Counter" || this == "Attribute"

internal fun SaveFieldUiRisk.color(): Color = Color(colorArgb)
