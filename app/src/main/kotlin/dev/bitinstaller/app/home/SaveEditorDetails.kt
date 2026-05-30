package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveAttributeSummary
import dev.bitinstaller.app.save.SaveCharacterSummary
import dev.bitinstaller.app.save.SaveEditableField
import java.util.Locale

private const val BYTES_PER_KIB = 1024f
private const val BYTES_PER_MIB = BYTES_PER_KIB * BYTES_PER_KIB
private const val MAX_ATTRIBUTE_PREVIEW_COUNT = 4
private const val MAX_CHARACTER_PREVIEW_COUNT = 4
private const val MAX_CHARACTER_FIELD_COUNT = 5
private const val SAVE_VALUE_LABEL_WEIGHT = 0.38f
private const val SAVE_VALUE_TEXT_WEIGHT = 0.62f
private val SaveValueRowShape = RoundedCornerShape(12.dp)

@Composable
internal fun SaveFileMetaLine(save: BitLifeSaveSummary) {
    Text(
        text =
            listOfNotNull(
                save.slotName,
                save.age?.let { "Age $it" },
                save.gender,
                formatBytes(save.sizeBytes),
            ).joinToString(" · "),
        style =
            MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.2.sp,
            ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun SaveFactRows(
    save: BitLifeSaveSummary,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    val facts =
        buildList {
            save.bankBalance?.let { balance ->
                add(SaveValueRow("Bank", String.format(Locale.US, "$%,.0f", balance), save.bankBalanceField))
            }
            save.facts.forEach { fact -> add(SaveValueRow(fact.label, fact.value, fact.field)) }
        }
    CompactValueRows(rows = facts, onFieldClick = onFieldClick)
}

@Composable
internal fun SaveAttributeRows(
    attributes: List<SaveAttributeSummary>,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    val rows =
        attributes
            .take(MAX_ATTRIBUTE_PREVIEW_COUNT)
            .map { attribute ->
                SaveValueRow(attribute.label, String.format(Locale.US, "%.0f", attribute.value), attribute.field)
            }
    CompactValueRows(rows = rows, onFieldClick = onFieldClick)
}

@Composable
internal fun SaveCharacterRows(
    characters: List<SaveCharacterSummary>,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    if (characters.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        characters.take(MAX_CHARACTER_PREVIEW_COUNT).forEach { character ->
            SaveCharacterCompactCard(character = character, onFieldClick = onFieldClick)
        }
        if (characters.size > MAX_CHARACTER_PREVIEW_COUNT) {
            Text(
                text = "+${characters.size - MAX_CHARACTER_PREVIEW_COUNT} more people in Advanced Editor",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompactValueRows(
    rows: List<SaveValueRow>,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row -> SaveValueRowView(row = row, onFieldClick = onFieldClick) }
    }
}

@Composable
private fun SaveCharacterCompactCard(
    character: SaveCharacterSummary,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        shape = SaveValueRowShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(12.dp)) {
            Text(
                text = character.role.uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = character.characterLabel(),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            character.fields.take(MAX_CHARACTER_FIELD_COUNT).forEach { field ->
                SaveValueRowView(
                    row = SaveValueRow(field.label, field.value, field),
                    onFieldClick = onFieldClick,
                )
            }
        }
    }
}

@Composable
private fun SaveValueRowView(
    row: SaveValueRow,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    val field = row.field
    if (field == null) {
        Surface(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
            shape = SaveValueRowShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SaveValueRowContent(row = row)
        }
        return
    }

    Surface(
        onClick = { onFieldClick(field) },
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
        shape = SaveValueRowShape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SaveValueRowContent(row = row)
    }
}

@Composable
private fun SaveValueRowContent(row: SaveValueRow) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(SAVE_VALUE_LABEL_WEIGHT)) {
            Text(
                text = row.label.uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (row.field != null) {
                Text(text = "Edit", style = MaterialTheme.typography.labelSmall)
            }
        }
        Text(
            text = row.value,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(SAVE_VALUE_TEXT_WEIGHT),
        )
    }
}

private data class SaveValueRow(
    val label: String,
    val value: String,
    val field: SaveEditableField?,
)

private fun formatBytes(sizeBytes: Int): String =
    when {
        sizeBytes <= 0 -> "unknown size"
        sizeBytes >= BYTES_PER_MIB -> String.format(Locale.US, "%.1f MB", sizeBytes / BYTES_PER_MIB)
        else -> String.format(Locale.US, "%.0f KB", sizeBytes / BYTES_PER_KIB)
    }

private fun SaveCharacterSummary.characterLabel(): String =
    listOfNotNull(
        name.takeUnless { it == "Unnamed life" },
        age?.let { "Age $it" },
        relationship?.let { String.format(Locale.US, "%.0f relationship", it) },
        isAlive?.let { alive -> if (alive) "Alive" else "Dead" },
    ).joinToString(" · ").ifBlank { name }
