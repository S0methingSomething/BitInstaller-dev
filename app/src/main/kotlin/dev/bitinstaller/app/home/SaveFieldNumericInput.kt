package dev.bitinstaller.app.home

import dev.bitinstaller.app.save.SaveEditableValueKind
import dev.bitinstaller.app.save.normalizedSaveNumberInput

internal fun SaveEditableValueKind.validateIntegerInput(raw: String): String? {
    val normalized = raw.normalizedSaveNumberInput()
    val parser = integerParser()
    return when {
        normalized.isBlank() -> "Value is required"
        parser == null -> null
        parser(normalized) -> null
        else -> integerValidationMessage()
    }
}

internal fun validateDecimalInput(raw: String): String? {
    val normalized = raw.normalizedSaveNumberInput()
    return when {
        normalized.isBlank() -> "Value is required"
        normalized.toDoubleOrNull() != null -> null
        else -> "Expected a number"
    }
}

internal fun Char.isWholeNumberInputChar(): Boolean =
    this == '-' || this == ',' || this == '_' || isWhitespace() || isDigit()

internal fun Char.isDecimalInputChar(): Boolean = isWholeNumberInputChar() || this == '.'

private fun SaveEditableValueKind.integerParser(): ((String) -> Boolean)? =
    when (this) {
        SaveEditableValueKind.BYTE -> { raw -> raw.toByteOrNull() != null }
        SaveEditableValueKind.SHORT -> { raw -> raw.toShortOrNull() != null }
        SaveEditableValueKind.INT -> { raw -> raw.toIntOrNull() != null }
        SaveEditableValueKind.LONG -> { raw -> raw.toLongOrNull() != null }
        else -> null
    }

private fun SaveEditableValueKind.integerValidationMessage(): String =
    when (this) {
        SaveEditableValueKind.BYTE -> "Expected a whole number (-128 to 127)"
        SaveEditableValueKind.SHORT -> "Expected a whole number (-32768 to 32767)"
        else -> "Expected a whole number"
    }
