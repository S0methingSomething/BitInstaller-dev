package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.explanation
import java.util.Locale

private val AdvancedFieldShape = RoundedCornerShape(10.dp)
private const val ADVANCED_FIELD_VALUE_WEIGHT = 0.45f
private const val ADVANCED_SURFACE_ALPHA = 0.06f
private const val ADVANCED_VALUE_ALPHA = 0.08f
private const val ADVANCED_SECONDARY_ALPHA = 0.4f
private const val ADVANCED_LABEL_ALPHA = 0.3f
private const val ADVANCED_LABEL_LETTER_SPACING_SP = 1f

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
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = modifier) {
        AdvancedSaveHeader(state = state)
        Text(
            text = "ADVANCED VARIABLES",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = ADVANCED_LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
            letterSpacing = ADVANCED_LABEL_LETTER_SPACING_SP.sp,
        )
        SaveAdvancedSearch(
            value = state.query,
            onValueChange = actions.onQueryChange,
            modifier = Modifier.fillMaxWidth(),
        )
        AdvancedFieldList(
            fields = state.fields,
            recentFieldIds = state.recentFieldIds,
            onFieldClick = actions.onFieldClick,
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = actions.onClose,
            shape = SaveEditorControlShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        ) {
            Text(text = "Close", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AdvancedSaveHeader(state: SaveAdvancedFieldsContentState) {
    Surface(
        color = Color.White.copy(alpha = ADVANCED_SURFACE_ALPHA),
        shape = SaveEditorControlShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
        ) {
            SaveSlotBubble(slotName = state.save.slotName)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.save.heroName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${state.targetName} · ${state.fields.size}/${state.save.advancedFields.size} values",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = ADVANCED_SECONDARY_ALPHA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
        color = Color.White.copy(alpha = ADVANCED_SURFACE_ALPHA),
        shape = AdvancedFieldShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AdvancedFieldMeta(field = field, isRecent = isRecent)
                Text(
                    text = field.path,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White.copy(alpha = ADVANCED_SECONDARY_ALPHA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier =
                    Modifier
                        .weight(ADVANCED_FIELD_VALUE_WEIGHT),
            ) {
                Surface(
                    color = Color.White.copy(alpha = ADVANCED_VALUE_ALPHA),
                    shape = AdvancedFieldShape,
                ) {
                    Text(
                        text = field.value.ifBlank { "empty" },
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                        color = Color.White,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }
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
