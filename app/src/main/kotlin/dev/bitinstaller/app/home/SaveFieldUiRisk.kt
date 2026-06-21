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
    return computeUiRisk(category)
}

internal fun SaveEditableField.computeUiRisk(category: String?): SaveFieldUiRisk {
    val member = memberName.lowercase()
    val pathLower = path.lowercase()

    return when {
        category.isDangerousCategory() || member.isDangerousField() || pathLower.isDangerousPath() -> {
            SaveFieldUiRisk.DANGER
        }

        category.isMediumRiskCategory() || member.isMediumRiskField() || pathLower.isMediumRiskPath() -> {
            SaveFieldUiRisk.MEDIUM
        }

        else -> {
            SaveFieldUiRisk.SAFE
        }
    }
}

private fun String?.isDangerousCategory(): Boolean =
    this == "Identity / metadata" || this == "Rendering internal" || this == "Enum id"

private fun String.isDangerousField(): Boolean = this == "_lifeid" || this == "metagenerationid"

private fun String.isDangerousPath(): Boolean =
    contains("metalastbootupversion") || contains("_script") || contains("emojiscr")

private fun String?.isMediumRiskCategory(): Boolean =
    this == "Cooldown / timing" || this == "Counter" || this == "Attribute" || this == "Cosmetic index"

private fun String.isMediumRiskField(): Boolean = this == "age" || (startsWith("accessory") && contains("index"))

private fun String.isMediumRiskPath(): Boolean = contains("accessory") && contains("index")

internal fun SaveFieldUiRisk.color(): Color = Color(colorArgb)
