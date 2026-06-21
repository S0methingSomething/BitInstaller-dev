package dev.bitinstaller.app.save

private const val BOOLEAN_TRUE = "true"
private const val BOOLEAN_FALSE = "false"

internal val backingFieldRegex = Regex("<([^>]+)>k__BackingField")
private val camelCaseBoundaryRegex = Regex("(?<=[a-z])(?=[A-Z])")
private val whitespaceCollapseRegex = Regex("\\s+")

internal fun SaveEditableField.parseRawValue(rawValue: String): Any? =
    when (valueKind) {
        SaveEditableValueKind.TEXT -> rawValue
        SaveEditableValueKind.BOOLEAN -> rawValue.parseBooleanValue(label)
        else -> rawValue.parseNumericValue(valueKind, label)
    }

internal fun String.normalizedSaveNumberInput(): String =
    trim().filterNot { ch -> ch == ',' || ch == '_' || ch.isWhitespace() }

private fun String.parseNumericValue(
    kind: SaveEditableValueKind,
    label: String,
): Number =
    when (kind) {
        SaveEditableValueKind.BYTE -> {
            normalizedSaveNumberInput().toByteOrNull()
                ?: error("$label must be a whole number from -128 to 127")
        }

        SaveEditableValueKind.SHORT -> {
            normalizedSaveNumberInput().toShortOrNull()
                ?: error("$label must be a whole number from -32768 to 32767")
        }

        SaveEditableValueKind.INT -> {
            normalizedSaveNumberInput().toIntOrNull() ?: error("$label must be a whole number")
        }

        SaveEditableValueKind.LONG -> {
            normalizedSaveNumberInput().toLongOrNull() ?: error("$label must be a whole number")
        }

        SaveEditableValueKind.FLOAT -> {
            normalizedSaveNumberInput().toFloatOrNull() ?: error("$label must be a number")
        }

        SaveEditableValueKind.DOUBLE -> {
            normalizedSaveNumberInput().toDoubleOrNull() ?: error("$label must be a number")
        }

        else -> {
            error("$label is not numeric")
        }
    }

internal fun Any?.toEditableDisplayValue(): String =
    when (this) {
        null -> ""
        is Float -> toCleanNumberString()
        is Double -> toCleanNumberString()
        else -> toString()
    }

internal fun String.cleanSaveMemberName(): String =
    substringAfterLast('+')
        .replace(backingFieldRegex, "$1")
        .removePrefix("_")
        .splitCamelCase()
        .stripSaveNameNoise()

internal fun String.stripSaveNameNoise(): String =
    replace("SimPerson", "")
        .replace("SimHero", "Hero")
        .replace("Sim", "")
        .replace("Att ", "")
        .replace("  ", " ")
        .trim()

private fun String.parseBooleanValue(label: String): Boolean =
    when (trim().lowercase()) {
        BOOLEAN_TRUE,
        "1",
        "yes",
        "alive",
        -> true

        BOOLEAN_FALSE,
        "0",
        "no",
        "dead",
        -> false

        else -> error("$label must be true or false")
    }

private fun Float.toCleanNumberString(): String = if (this % 1f == 0f) toInt().toString() else toString()

private fun Double.toCleanNumberString(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()

private fun String.splitCamelCase(): String =
    replace('_', ' ')
        .replace(camelCaseBoundaryRegex, " ")
        .replace(whitespaceCollapseRegex, " ")
        .trim()
