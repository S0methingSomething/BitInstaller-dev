package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.explanation

private val AdvancedFieldShape = RoundedCornerShape(10.dp)
private const val ADVANCED_LIST_HEIGHT_FRACTION = 0.45f

internal data class SaveAdvancedFieldsContentState(
    val save: BitLifeSaveSummary,
    val fields: List<SaveEditableField>,
    val recentFieldIds: List<String>,
    val query: String,
    val filter: AdvancedFieldFilter,
    val sort: AdvancedFieldSort,
)

@Composable
internal fun SaveAdvancedFieldsContent(
    state: SaveAdvancedFieldsContentState,
    onQueryChange: (String) -> Unit,
    onFilterChange: () -> Unit,
    onSortChange: () -> Unit,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "${state.save.heroName} · ${state.fields.size}/${state.save.advancedFields.size} editable fields",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text(text = "Search names, stats, money, flags...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        SaveAdvancedFieldControls(
            filter = state.filter,
            sort = state.sort,
            onFilterChange = onFilterChange,
            onSortChange = onSortChange,
        )
        AdvancedFieldList(
            fields = state.fields,
            recentFieldIds = state.recentFieldIds,
            onFieldClick = onFieldClick,
        )
    }
}

@Composable
private fun AdvancedFieldList(
    fields: List<SaveEditableField>,
    recentFieldIds: List<String>,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    val density = LocalDensity.current
    val containerHeightDp =
        with(density) {
            LocalWindowInfo.current.containerSize.height
                .toDp()
        }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = containerHeightDp * ADVANCED_LIST_HEIGHT_FRACTION),
    ) {
        items(fields, key = { field -> field.id }) { field ->
            AdvancedFieldRow(
                field = field,
                isRecent = field.id in recentFieldIds,
                onClick = { onFieldClick(field) },
            )
        }
    }
}

@Composable
private fun AdvancedFieldRow(
    field: SaveEditableField,
    isRecent: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        shape = AdvancedFieldShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Text(text = field.label, style = MaterialTheme.typography.labelLarge)
            AdvancedFieldExplanationText(field = field, isRecent = isRecent)
            Text(
                text = field.path,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = field.value.ifBlank { "empty" },
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AdvancedFieldExplanationText(
    field: SaveEditableField,
    isRecent: Boolean,
) {
    val explanation = field.explanation()
    if (explanation != null) {
        val categoryLine =
            listOfNotNull("Recently edited".takeIf { isRecent }, explanation.category)
                .joinToString(" · ")
        Text(
            text = categoryLine,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = explanation.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    } else if (isRecent) {
        Text(
            text = "Recently edited",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
