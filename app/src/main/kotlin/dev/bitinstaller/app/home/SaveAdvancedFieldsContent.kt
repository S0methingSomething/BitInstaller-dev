package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.explanation
import java.util.Locale

private val AdvancedFieldShape = RoundedCornerShape(10.dp)
private const val ADVANCED_SUMMARY_LABEL_WEIGHT = 0.35f
private const val ADVANCED_SUMMARY_VALUE_WEIGHT = 0.65f
private const val ADVANCED_FIELD_VALUE_WEIGHT = 0.45f

internal data class SaveAdvancedFieldsContentState(
    val targetName: String,
    val save: BitLifeSaveSummary,
    val fields: List<SaveEditableField>,
    val recentFieldIds: List<String>,
    val query: String,
    val filter: AdvancedFieldFilter,
    val sort: AdvancedFieldSort,
)

internal data class SaveAdvancedFieldsContentActions(
    val onQueryChange: (String) -> Unit,
    val onFilterChange: (AdvancedFieldFilter) -> Unit,
    val onSortChange: (AdvancedFieldSort) -> Unit,
    val onFieldClick: (SaveEditableField) -> Unit,
    val onClose: () -> Unit,
)

@Composable
internal fun SaveAdvancedFieldsContent(
    state: SaveAdvancedFieldsContentState,
    actions: SaveAdvancedFieldsContentActions,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = modifier) {
        AdvancedFieldsTopBar(state = state)
        AdvancedSaveSummary(save = state.save)
        SaveAdvancedSearch(
            value = state.query,
            onValueChange = actions.onQueryChange,
            modifier = Modifier.fillMaxWidth(),
        )
        SaveAdvancedFieldControls(
            filter = state.filter,
            sort = state.sort,
            onFilterChange = actions.onFilterChange,
            onSortChange = actions.onSortChange,
        )
        AdvancedFieldList(
            fields = state.fields,
            recentFieldIds = state.recentFieldIds,
            onFieldClick = actions.onFieldClick,
            modifier = Modifier.weight(1f),
        )
        Button(onClick = actions.onClose, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)) {
            Text(text = "Save and close")
        }
    }
}

@Composable
private fun AdvancedFieldsTopBar(state: SaveAdvancedFieldsContentState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = state.targetName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${state.save.slotName} · ${state.fields.size}/${state.save.advancedFields.size} values",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AdvancedSaveSummary(save: BitLifeSaveSummary) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(18.dp)) {
            Text(
                text = save.heroName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            AdvancedSummaryLine(label = "Character", value = save.heroName)
            save.bankBalance?.let { balance ->
                AdvancedSummaryLine(label = "Bank", value = formatAdvancedMoney(balance))
            }
            save.age?.let { age -> AdvancedSummaryLine(label = "Age", value = age.toString()) }
            remember(save) { save.occupation() }?.let { occupation ->
                AdvancedSummaryLine(label = "Occupation", value = occupation)
            }
            remember(save) { save.characterNames() }.takeIf { it.isNotBlank() }?.let { names ->
                AdvancedSummaryLine(label = "Names", value = names, maxLines = 2)
            }
        }
    }
}

@Composable
private fun AdvancedSummaryLine(
    label: String,
    value: String,
    maxLines: Int = 1,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(ADVANCED_SUMMARY_LABEL_WEIGHT),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(ADVANCED_SUMMARY_VALUE_WEIGHT),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AdvancedFieldList(
    fields: List<SaveEditableField>,
    recentFieldIds: List<String>,
    onFieldClick: (SaveEditableField) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier,
    ) {
        items(fields, key = { field -> field.id }, contentType = { SaveEditableField::class }) { field ->
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
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
        shape = AdvancedFieldShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text(text = field.label, style = MaterialTheme.typography.titleMedium)
                AdvancedFieldMeta(field = field, isRecent = isRecent)
                Text(
                    text = field.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = field.value.ifBlank { "empty" },
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(ADVANCED_FIELD_VALUE_WEIGHT),
            )
        }
    }
}

@Composable
private fun AdvancedFieldMeta(
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
    } else if (isRecent) {
        Text(
            text = "Recently edited",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun BitLifeSaveSummary.occupation(): String? =
    facts.firstOrNull { fact -> fact.label.contains("Occupation", ignoreCase = true) }?.value

private fun BitLifeSaveSummary.characterNames(): String =
    characters
        .map { character -> character.name.takeUnless { it == "Unnamed life" } ?: character.role }
        .distinct()
        .joinToString(" · ")

private fun formatAdvancedMoney(value: Double): String = String.format(Locale.US, "$%,.0f", value)
