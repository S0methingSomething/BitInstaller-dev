package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind

@Composable
internal fun SaveFieldEditDialog(
    draft: SaveFieldEditDraft,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(draft.field.id) { mutableStateOf(draft.field.value) }
    val validationError = remember(value) { draft.field.valueKind.validate(value) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Edit ${draft.field.label}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = draft.field.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { next -> value = draft.field.valueKind.filterInput(next) },
                    label = { Text(text = draft.field.valueKind.inputLabel()) },
                    isError = validationError != null,
                    supportingText = validationError?.let { error -> { Text(text = error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = draft.field.valueKind.keyboardType()),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(value) },
                enabled = validationError == null,
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        },
    )
}

@Composable
internal fun SaveAdvancedFieldsDialog(
    save: BitLifeSaveSummary,
    recentFieldIds: List<String>,
    onDismissRequest: () -> Unit,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    var query by remember(save.path) { mutableStateOf("") }
    var filter by remember(save.path) { mutableStateOf(AdvancedFieldFilter.ALL) }
    var sort by remember(save.path) { mutableStateOf(AdvancedFieldSort.RECENT_FIRST) }
    val filtered =
        remember(query, filter, sort, recentFieldIds, save.advancedFields) {
            save.advancedFields.filteredAndSorted(
                query = query,
                recentFieldIds = recentFieldIds,
                filter = filter,
                sort = sort,
            )
        }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Advanced fields") },
        text = {
            SaveAdvancedFieldsContent(
                state =
                    SaveAdvancedFieldsContentState(
                        save = save,
                        fields = filtered,
                        recentFieldIds = recentFieldIds,
                        query = query,
                        filter = filter,
                        sort = sort,
                    ),
                onQueryChange = { query = it },
                onFilterChange = { filter = filter.next() },
                onSortChange = { sort = sort.next() },
                onFieldClick = onFieldClick,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Close")
            }
        },
    )
}

private fun SaveEditableValueKind.inputLabel(): String =
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

private fun SaveEditableValueKind.keyboardType(): KeyboardType =
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

private fun SaveEditableValueKind.filterInput(raw: String): String =
    when (this) {
        SaveEditableValueKind.TEXT -> {
            raw
        }

        SaveEditableValueKind.BOOLEAN -> {
            raw.lowercase().filter { ch -> ch in "truefalse" }
        }

        SaveEditableValueKind.BYTE,
        SaveEditableValueKind.SHORT,
        SaveEditableValueKind.INT,
        SaveEditableValueKind.LONG,
        -> {
            raw.filter { ch -> ch == '-' || ch.isDigit() }
        }

        SaveEditableValueKind.FLOAT,
        SaveEditableValueKind.DOUBLE,
        -> {
            raw.filter { ch -> ch == '-' || ch == '.' || ch.isDigit() }
        }
    }

private fun SaveEditableValueKind.validate(raw: String): String? {
    if (raw.isBlank()) return "Value is required"
    return when (this) {
        SaveEditableValueKind.TEXT -> null

        SaveEditableValueKind.BOOLEAN -> validateBoolean(raw)

        SaveEditableValueKind.BYTE,
        SaveEditableValueKind.SHORT,
        SaveEditableValueKind.INT,
        SaveEditableValueKind.LONG,
        -> validateInteger(raw)

        SaveEditableValueKind.FLOAT,
        SaveEditableValueKind.DOUBLE,
        -> validateDecimal(raw)
    }
}

private fun SaveEditableValueKind.validateBoolean(raw: String): String? =
    if (raw.trim().lowercase() in listOf("true", "false")) null else "Enter true or false"

private fun SaveEditableValueKind.validateInteger(raw: String): String? =
    when (this) {
        SaveEditableValueKind.BYTE -> raw.toByteOrNull()?.let { null } ?: "Expected a whole number (-128 to 127)"
        SaveEditableValueKind.SHORT -> raw.toShortOrNull()?.let { null } ?: "Expected a whole number (-32768 to 32767)"
        SaveEditableValueKind.INT -> raw.toIntOrNull()?.let { null } ?: "Expected a whole number"
        SaveEditableValueKind.LONG -> raw.toLongOrNull()?.let { null } ?: "Expected a whole number"
        else -> null
    }

private fun validateDecimal(raw: String): String? = if (raw.toDoubleOrNull() != null) null else "Expected a number"
