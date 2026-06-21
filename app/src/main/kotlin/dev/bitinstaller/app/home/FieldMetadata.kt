package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveFieldExplanation
import dev.bitinstaller.app.save.explanation

internal data class FieldMetadata(
    val explanation: SaveFieldExplanation?,
    val uiCategory: SaveFieldUiCategory,
    val uiRisk: SaveFieldUiRisk,
    val searchText: String,
)

internal fun SaveEditableField.computeMetadata(): FieldMetadata {
    val explanation = this.explanation()
    val uiCategory = computeUiCategory(explanation?.category)
    val uiRisk = computeUiRisk(explanation?.category)
    val searchText =
        buildString {
            append(memberName)
            append(' ')
            append(label)
            append(' ')
            append(path)
            explanation?.category?.let { append(' ').append(it) }
            explanation?.description?.let { append(' ').append(it) }
        }
    return FieldMetadata(
        explanation = explanation,
        uiCategory = uiCategory,
        uiRisk = uiRisk,
        searchText = searchText,
    )
}
