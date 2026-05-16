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
private const val MAX_CHARACTER_PREVIEW_COUNT = 6
private const val SAVE_DETAIL_COLUMNS = 2

@Composable
internal fun SaveFileMetaLine(save: BitLifeSaveSummary) {
    Text(
        text =
            listOfNotNull(
                save.slotName,
                save.age?.let { "Age $it" },
                save.gender,
                formatBytes(save.sizeBytes),
            ).joinToString("  •  "),
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
            save.bankBalance?.let { add(SaveFactChip("Bank", formatMoney(it), save.bankBalanceField)) }
            save.facts.forEach { fact -> add(SaveFactChip(fact.label, fact.value, fact.field)) }
        }
    facts.chunked(SAVE_DETAIL_COLUMNS).forEach { rowFacts ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rowFacts.forEach { fact ->
                SaveFactChipView(fact = fact, onFieldClick = onFieldClick, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun SaveAttributeRows(
    attributes: List<SaveAttributeSummary>,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    attributes.take(MAX_ATTRIBUTE_PREVIEW_COUNT).chunked(SAVE_DETAIL_COLUMNS).forEach { rowAttributes ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rowAttributes.forEach { attribute ->
                SaveFactChipView(
                    fact = SaveFactChip(attribute.label, formatPercent(attribute.value), attribute.field),
                    onFieldClick = onFieldClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun SaveCharacterRows(
    characters: List<SaveCharacterSummary>,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    if (characters.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Characters",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        characters.take(MAX_CHARACTER_PREVIEW_COUNT).forEach { character ->
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = character.role.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(text = character.characterLabel(), style = MaterialTheme.typography.labelLarge)
                    character.fields.chunked(SAVE_DETAIL_COLUMNS).forEach { rowFields ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowFields.forEach { field ->
                                SaveFactChipView(
                                    fact = SaveFactChip(field.label, field.value, field),
                                    onFieldClick = onFieldClick,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveFactChipView(
    fact: SaveFactChip,
    onFieldClick: (SaveEditableField) -> Unit,
    modifier: Modifier = Modifier,
) {
    val field = fact.field
    if (field == null) {
        Surface(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier,
        ) {
            SaveFactChipContent(fact = fact)
        }
    } else {
        Surface(
            onClick = { onFieldClick(field) },
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier,
        ) {
            SaveFactChipContent(fact = fact)
        }
    }
}

@Composable
private fun SaveFactChipContent(fact: SaveFactChip) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
        Text(
            text = fact.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = fact.value,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class SaveFactChip(
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

private fun formatMoney(value: Double): String = String.format(Locale.US, "$%,.0f", value)

private fun formatPercent(value: Float): String = String.format(Locale.US, "%.0f", value)

private fun SaveCharacterSummary.characterLabel(): String =
    listOfNotNull(
        name.takeUnless { it == "Unnamed life" },
        age?.let { "Age $it" },
        relationship?.let { String.format(Locale.US, "%.0f relationship", it) },
        isAlive?.let { alive -> if (alive) "Alive" else "Dead" },
    ).joinToString(" • ").ifBlank { name }
