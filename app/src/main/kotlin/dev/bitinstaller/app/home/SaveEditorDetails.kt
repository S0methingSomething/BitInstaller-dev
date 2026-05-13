package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import java.util.Locale

private const val BYTES_PER_KIB = 1024f
private const val BYTES_PER_MIB = BYTES_PER_KIB * BYTES_PER_KIB
private const val MAX_ATTRIBUTE_PREVIEW_COUNT = 4
private const val SAVE_DETAIL_COLUMNS = 2

@Composable
internal fun SaveFileMetaLine(save: BitLifeSaveSummary) {
    Text(
        text =
            listOfNotNull(
                save.fileName,
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
internal fun SaveFactRows(save: BitLifeSaveSummary) {
    val facts =
        buildList {
            save.bankBalance?.let { add(SaveFactChip("Bank", formatMoney(it))) }
            save.facts.forEach { fact -> add(SaveFactChip(fact.label, fact.value)) }
        }
    facts.chunked(SAVE_DETAIL_COLUMNS).forEach { rowFacts ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rowFacts.forEach { fact -> SaveFactChipView(fact = fact, modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
internal fun SaveAttributeRows(attributes: List<SaveAttributeSummary>) {
    attributes.take(MAX_ATTRIBUTE_PREVIEW_COUNT).chunked(SAVE_DETAIL_COLUMNS).forEach { rowAttributes ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rowAttributes.forEach { attribute ->
                SaveFactChipView(
                    fact = SaveFactChip(attribute.label, formatPercent(attribute.value)),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SaveFactChipView(
    fact: SaveFactChip,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
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
}

private data class SaveFactChip(
    val label: String,
    val value: String,
)

private fun formatBytes(sizeBytes: Int): String =
    when {
        sizeBytes <= 0 -> "unknown size"
        sizeBytes >= BYTES_PER_MIB -> String.format(Locale.US, "%.1f MB", sizeBytes / BYTES_PER_MIB)
        else -> String.format(Locale.US, "%.0f KB", sizeBytes / BYTES_PER_KIB)
    }

private fun formatMoney(value: Double): String = String.format(Locale.US, "$%,.0f", value)

private fun formatPercent(value: Float): String = String.format(Locale.US, "%.0f", value)
