package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveFieldEdit

internal data class SaveSlotEditDraft(
    val values: Map<String, String> = emptyMap(),
) {
    val dirtyCount: Int get() = values.size
    val isDirty: Boolean get() = values.isNotEmpty()

    fun valueFor(field: SaveEditableField): String = values[field.id] ?: field.value

    fun isDirty(field: SaveEditableField): Boolean = values.containsKey(field.id)

    fun update(
        field: SaveEditableField,
        rawValue: String,
    ): SaveSlotEditDraft =
        copy(
            values =
                if (valuesEqual(rawValue, field.value)) {
                    values - field.id
                } else {
                    values + (field.id to rawValue)
                },
        )

    private fun valuesEqual(
        a: String,
        b: String,
    ): Boolean =
        a == b || a.toFloatOrNull()?.let { fa ->
            b.toFloatOrNull()?.let { fb -> fa == fb }
        } == true

    fun toEdits(save: BitLifeSaveSummary): List<SaveFieldEdit> {
        val fieldsById = save.editableFields().associateBy { field -> field.id }
        return values.mapNotNull { (fieldId, rawValue) ->
            fieldsById[fieldId]?.let { field -> SaveFieldEdit(field = field, rawValue = rawValue) }
        }
    }
}

internal fun BitLifeSaveSummary.editableFields(): List<SaveEditableField> =
    buildList {
        bankBalanceField?.let(::add)
        facts.mapNotNullTo(this) { fact -> fact.field }
        attributes.mapNotNullTo(this) { attribute -> attribute.field }
        characters.forEach { character -> addAll(character.fields) }
        addAll(advancedFields)
    }.distinctBy { field -> field.id }
