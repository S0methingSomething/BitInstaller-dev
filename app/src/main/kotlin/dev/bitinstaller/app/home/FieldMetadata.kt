package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveFieldExplanation
import dev.bitinstaller.app.save.explanation

internal data class FieldMetadata(
    val explanation: SaveFieldExplanation?,
    val uiCategory: SaveFieldUiCategory,
    val uiRisk: SaveFieldUiRisk,
)

internal fun SaveEditableField.computeMetadata(): FieldMetadata {
    val explanation = this.explanation()
    return FieldMetadata(
        explanation = explanation,
        uiCategory = computeUiCategory(explanation?.category),
        uiRisk = computeUiRisk(explanation?.category),
    )
}
