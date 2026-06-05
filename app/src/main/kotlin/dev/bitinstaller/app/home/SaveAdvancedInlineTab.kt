package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    modifier: Modifier = Modifier,
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

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item(contentType = "advanced-search") {
            SaveAdvancedSearch(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth())
        }
        items(
            items = fields,
            key = { field -> field.id },
            contentType = { field -> field.valueKind },
        ) { field ->
            SaveAdvancedDraftField(
                field = field,
                draft = draft,
                onDraftChange = onDraftChange,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun SaveAdvancedDraftField(
    field: SaveEditableField,
    draft: SaveSlotEditDraft,
    onDraftChange: (SaveEditableField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (field.valueKind == SaveEditableValueKind.BOOLEAN) {
        SaveInlineToggleField(
            label = field.label,
            checked = draft.valueFor(field).equals("true", ignoreCase = true),
            onCheckedChange = { checked -> onDraftChange(field, if (checked) "True" else "False") },
            modifier = modifier.fillMaxWidth(),
        )
    } else {
        SaveInlineTextField(
            label = field.label,
            value = draft.valueFor(field),
            onValueChange = { value -> onDraftChange(field, value) },
            modifier = modifier.fillMaxWidth(),
        )
    }
}
