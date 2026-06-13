package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val ADVANCED_SEARCH_DEBOUNCE_MS = 120L
private const val FIELD_DRAFT_SYNC_DEBOUNCE_MS = 100L

private val RecentChipShape = RoundedCornerShape(8.dp)
private const val RECENT_CHIP_ALPHA = 0.06f

@OptIn(FlowPreview::class)
@Composable
internal fun SaveAdvancedInlineTab(
    save: BitLifeSaveSummary,
    draft: SaveSlotEditDraft,
    recentFieldIds: List<String>,
    onDraftChange: (SaveEditableField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable(save.path) { mutableStateOf("") }
    val currentSave by rememberUpdatedState(save)

    val fields: List<SaveEditableField> by remember(recentFieldIds, save.path) {
        snapshotFlow { query }
            .debounce(ADVANCED_SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .map { currentQuery ->
                currentSave.advancedFields.filteredAndSorted(
                    query = currentQuery,
                    recentFieldIds = recentFieldIds,
                    filter = AdvancedFieldFilter.ALL,
                    sort = AdvancedFieldSort.NAME,
                )
            }
    }.collectAsState(initial = save.advancedFields)

    val recentLabels =
        remember(recentFieldIds) {
            save.advancedFields
                .filter { field -> field.id in recentFieldIds }
                .map { field -> field.label }
        }

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
        item(contentType = "advanced-count") {
            Text(
                text = "${fields.size} variable${if (fields.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
            )
        }
        items(
            items = fields,
            key = { field -> field.id },
            contentType = { field -> field.valueKind },
        ) { field ->
            SaveAdvancedDraftField(
                field = field,
                draftValue = draft.valueFor(field),
                onDraftChange = onDraftChange,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentFieldsSection(
    labels: List<String>,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "RECENTLY EDITED",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.25f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            labels.forEach { label ->
                FilterChip(
                    selected = false,
                    onClick = { onChipClick(label) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    shape = RecentChipShape,
                    colors =
                        androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = RECENT_CHIP_ALPHA),
                            labelColor = Color.White,
                        ),
                )
            }
        }
    }
}

@Composable
private fun SaveAdvancedDraftField(
    field: SaveEditableField,
    draftValue: String,
    onDraftChange: (SaveEditableField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var localValue by rememberSaveable(field.id) { mutableStateOf(draftValue) }

    LaunchedEffect(localValue) {
        if (localValue != draftValue) {
            delay(FIELD_DRAFT_SYNC_DEBOUNCE_MS)
            onDraftChange(field, localValue)
        }
    }

    if (field.valueKind == SaveEditableValueKind.BOOLEAN) {
        val checked = localValue.equals("true", ignoreCase = true)
        SaveInlineToggleField(
            label = field.label,
            checked = checked,
            onCheckedChange = { value ->
                val str = if (value) "True" else "False"
                localValue = str
            },
            modifier = modifier.fillMaxWidth(),
        )
    } else {
        SaveInlineTextField(
            label = field.label,
            value = localValue,
            onValueChange = { value -> localValue = value },
            modifier = modifier.fillMaxWidth(),
        )
    }
}
