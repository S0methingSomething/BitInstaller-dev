package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
internal fun SaveAdvancedInlineTab(
    save: BitLifeSaveSummary,
    draftValues: SnapshotStateMap<String, String>,
    recentFieldIds: List<String>,
    onDraftChange: (SaveEditableField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable(save.path) { mutableStateOf("") }
    var selectedCategory by rememberSaveable(save.path) { mutableStateOf<SaveFieldUiCategory?>(null) }

    val result by produceState(initialValue = AdvancedFieldResult(fields = emptyList(), isComputing = true)) {
        val debouncedQuery =
            snapshotFlow { query }
                .debounce(ADVANCED_DEBOUNCE_MS)
                .distinctUntilChanged()

        combine(debouncedQuery, snapshotFlow { selectedCategory }) { q, c -> q to c }
            .collect { (debouncedQuery, category) ->
                value = computeFields(save, recentFieldIds, debouncedQuery, category)
            }
    }

    val recentLabels = rememberRecentLabels(save = save, recentFieldIds = recentFieldIds)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 18.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        if (recentLabels.isNotEmpty()) {
            item(contentType = "advanced-recent") {
                RecentFieldsSection(labels = recentLabels, onChipClick = { label ->
                    query = label
                }, modifier = Modifier.fillMaxWidth())
            }
        }
        item(contentType = "advanced-search") {
            SaveAdvancedSearch(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth())
        }
        item(contentType = "advanced-categories") {
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item(contentType = "advanced-count") {
            Text(
                text = "${result.fields.size} variable${if (result.fields.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
            )
        }
        items(
            items = result.fields,
            key = { field: SaveEditableField -> field.id },
            contentType = { field: SaveEditableField -> field.valueKind },
        ) { field: SaveEditableField ->
            SaveAdvancedFieldCard(
                field = field,
                draftValue = draftValues.valueFor(field),
                onDraftChange = onDraftChange,
            )
        }
    }
}

@Composable
private fun rememberRecentLabels(
    save: BitLifeSaveSummary,
    recentFieldIds: List<String>,
): List<String> =
    remember(recentFieldIds) {
        save.advancedFields
            .filter { field -> field.id in recentFieldIds }
            .map { field -> field.label }
    }
