package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind
import kotlinx.coroutines.delay

private const val INLINE_ADVANCED_SEARCH_DEBOUNCE_MS = 250L

@Composable
internal fun SaveAdvancedInlineTab(
    save: BitLifeSaveSummary,
    draft: SaveSlotEditDraft,
    onDraftChange: (SaveEditableField, String) -> Unit,
) {
    var query by rememberSaveable(save.path) { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        delay(INLINE_ADVANCED_SEARCH_DEBOUNCE_MS)
        debouncedQuery = query
    }
    val fields by remember(debouncedQuery, save.advancedFields) {
        derivedStateOf {
            save.advancedFields.filteredAndSorted(
                query = debouncedQuery,
                recentFieldIds = emptyList(),
                filter = AdvancedFieldFilter.ALL,
                sort = AdvancedFieldSort.NAME,
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SaveAdvancedSearch(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth())
        fields.forEach { field ->
            SaveAdvancedDraftField(field = field, draft = draft, onDraftChange = onDraftChange)
        }
    }
}

@Composable
private fun SaveAdvancedDraftField(
    field: SaveEditableField,
    draft: SaveSlotEditDraft,
    onDraftChange: (SaveEditableField, String) -> Unit,
) {
    if (field.valueKind == SaveEditableValueKind.BOOLEAN) {
        SaveInlineToggleField(
            label = field.label,
            checked = draft.valueFor(field).equals("true", ignoreCase = true),
            onCheckedChange = { checked -> onDraftChange(field, if (checked) "True" else "False") },
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        SaveInlineTextField(
            label = field.label,
            value = draft.valueFor(field),
            onValueChange = { value -> onDraftChange(field, value) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
