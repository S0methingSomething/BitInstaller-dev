package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        facts.forEach { fact ->
            SaveFactChipView(fact = fact, onFieldClick = onFieldClick, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
internal fun SaveAttributeRows(
    attributes: List<SaveAttributeSummary>,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        attributes.take(MAX_ATTRIBUTE_PREVIEW_COUNT).forEach { attribute ->
            SaveFactChipView(
                fact = SaveFactChip(attribute.label, formatPercent(attribute.value), attribute.field),
                onFieldClick = onFieldClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun SaveCharacterRows(
    characters: List<SaveCharacterSummary>,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    if (characters.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Characters",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        characters.take(MAX_CHARACTER_PREVIEW_COUNT).forEach { character ->
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = character.role.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(text = character.characterLabel(), style = MaterialTheme.typography.labelLarge)
                    character.fields.forEach { field ->
                        SaveFactChipView(
                            fact = SaveFactChip(field.label, field.value, field),
                            onFieldClick = onFieldClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier,
        ) {
            SaveFactChipContent(fact = fact, isEditable = false)
        }
    } else {
        Surface(
            onClick = { onFieldClick(field) },
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f)),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier,
        ) {
            SaveFactChipContent(fact = fact, isEditable = true)
        }
    }
}

@Composable
private fun SaveFactChipContent(
    fact: SaveFactChip,
    isEditable: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = fact.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = fact.value,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (isEditable) {
            Text(
                text = "Tap to edit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
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
