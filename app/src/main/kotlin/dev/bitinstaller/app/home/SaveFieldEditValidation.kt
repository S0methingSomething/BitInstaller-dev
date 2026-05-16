package dev.bitinstaller.app.home

import androidx.compose.ui.text.input.KeyboardType
import dev.bitinstaller.app.save.SaveEditableValueKind

private val VALID_BOOLEAN_INPUTS = setOf("true", "false", "1", "0", "yes", "no", "alive", "dead")

internal fun SaveEditableValueKind.validateEditInput(raw: String): String? {
    if (raw.isBlank()) return "Value is required"
    return when (this) {
        SaveEditableValueKind.TEXT -> null

        SaveEditableValueKind.BOOLEAN -> validateBoolean(raw)

        SaveEditableValueKind.BYTE,
        SaveEditableValueKind.SHORT,
        SaveEditableValueKind.INT,
        SaveEditableValueKind.LONG,
        -> validateIntegerInput(raw)

        SaveEditableValueKind.FLOAT,
        SaveEditableValueKind.DOUBLE,
        -> validateDecimalInput(raw)
    }
}

internal fun SaveEditableValueKind.filterEditInput(raw: String): String =
    when (this) {
        SaveEditableValueKind.TEXT -> raw

        SaveEditableValueKind.BOOLEAN -> raw.lowercase().filter { ch -> ch.isLetterOrDigit() }

        SaveEditableValueKind.BYTE,
        SaveEditableValueKind.SHORT,
        SaveEditableValueKind.INT,
        SaveEditableValueKind.LONG,
        -> raw.filter { ch -> ch.isWholeNumberInputChar() }

        SaveEditableValueKind.FLOAT,
        SaveEditableValueKind.DOUBLE,
        -> raw.filter { ch -> ch.isDecimalInputChar() }
    }

internal fun SaveEditableValueKind.inputLabel(): String =
    when (this) {
        SaveEditableValueKind.TEXT -> "Text"

        SaveEditableValueKind.BYTE,
        SaveEditableValueKind.SHORT,
        SaveEditableValueKind.INT,
        SaveEditableValueKind.LONG,
        -> "Whole number"

        SaveEditableValueKind.FLOAT,
        SaveEditableValueKind.DOUBLE,
        -> "Number"

        SaveEditableValueKind.BOOLEAN -> "true / false"
    }

internal fun SaveEditableValueKind.keyboardType(): KeyboardType =
    when (this) {
        SaveEditableValueKind.TEXT -> KeyboardType.Text

        SaveEditableValueKind.BOOLEAN -> KeyboardType.Text

        SaveEditableValueKind.FLOAT,
        SaveEditableValueKind.DOUBLE,
        -> KeyboardType.Decimal

        SaveEditableValueKind.BYTE,
        SaveEditableValueKind.SHORT,
        SaveEditableValueKind.INT,
        SaveEditableValueKind.LONG,
        -> KeyboardType.Number
    }

internal fun SaveEditableValueKind.editTip(): String =
    when (this) {
        SaveEditableValueKind.TEXT -> {
            "Edit the text exactly as it should appear in the save."
        }

        SaveEditableValueKind.BOOLEAN -> {
            "Use true or false. 1/0, yes/no, and alive/dead also work when saved."
        }

        SaveEditableValueKind.BYTE -> {
            "Use a whole number from -128 to 127. Most stat-like byte values should stay near 0-100."
        }

        SaveEditableValueKind.SHORT -> {
            "Use a whole number from -32768 to 32767. Commas and spaces are ignored."
        }

        SaveEditableValueKind.INT -> {
            "Use a whole number. Commas and spaces are ignored."
        }

        SaveEditableValueKind.LONG -> {
            "Use a large whole number. Commas and spaces are ignored."
        }

        SaveEditableValueKind.FLOAT,
        SaveEditableValueKind.DOUBLE,
        -> {
            "Use a number with or without decimals. Commas and spaces are ignored."
        }
    }

private fun SaveEditableValueKind.validateBoolean(raw: String): String? =
    if (raw.trim().lowercase() in VALID_BOOLEAN_INPUTS) {
        null
    } else {
        "Enter true/false, 1/0, yes/no, or alive/dead"
    }
