package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import java.util.Locale

private val SaveCardShape = RoundedCornerShape(18.dp)
private val SaveMetricShape = RoundedCornerShape(10.dp)
private const val COLLAPSED_ATTRIBUTE_COUNT = 3
private const val SUMMARY_BYTES_PER_KIB = 1024f
private const val SUMMARY_BYTES_PER_MIB = SUMMARY_BYTES_PER_KIB * SUMMARY_BYTES_PER_KIB

@Composable
internal fun SaveFileList(
    target: SaveTargetUiState,
    saves: List<BitLifeSaveSummary>,
    onSaveOpen: (BitLifeSaveSummary) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        saves.forEach { save ->
            SaveSlotSummaryCard(
                save = save,
                isWorking = target.editingSavePath == save.path,
                error = target.editErrors[save.path] ?: save.errorMessage,
                onOpen = { onSaveOpen(save) },
            )
        }
    }
}

@Composable
private fun SaveSlotSummaryCard(
    save: BitLifeSaveSummary,
    isWorking: Boolean,
    error: String?,
    onOpen: () -> Unit,
) {
    Surface(
        onClick = onOpen,
        shape = SaveCardShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            SaveSlotSummaryHeader(save = save, isWorking = isWorking, onOpen = onOpen)
            if (error != null) {
                SaveSlotStatus(text = error, isError = true)
            } else {
                SaveSlotMetrics(save = save)
            }
        }
    }
}

@Composable
private fun SaveSlotSummaryHeader(
    save: BitLifeSaveSummary,
    isWorking: Boolean,
    onOpen: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SaveSlotBadge(slotName = save.slotName)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = save.heroName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = save.identityLine(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextButton(enabled = !isWorking, onClick = onOpen) {
            Text(text = if (isWorking) "Working" else "Open Editor")
        }
    }
}

@Composable
private fun SaveSlotMetrics(save: BitLifeSaveSummary) {
    val metrics = save.summaryMetrics()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowMetrics.forEach { metric ->
                    SaveSlotMetric(label = metric.label, value = metric.value, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SaveSlotMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)),
        shape = SaveMetricShape,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(10.dp)) {
            Text(
                text = label.uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun SaveSlotStatus(
    text: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Surface(
        color = color.copy(alpha = if (isError) 0.10f else 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = if (isError) 0.30f else 0.22f)),
        shape = SaveMetricShape,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        )
    }
}

private data class SaveSlotMetricItem(
    val label: String,
    val value: String,
)

private fun BitLifeSaveSummary.identityLine(): String =
    listOfNotNull(
        slotName,
        age?.let { "Age $it" },
        gender,
        formatBytes(sizeBytes),
    ).joinToString(" · ")

private fun BitLifeSaveSummary.summaryMetrics(): List<SaveSlotMetricItem> =
    buildList {
        bankBalance?.let { add(SaveSlotMetricItem("Bank", String.format(Locale.US, "$%,.0f", it))) }
        attributes.take(COLLAPSED_ATTRIBUTE_COUNT).forEach { attribute ->
            add(SaveSlotMetricItem(attribute.label, String.format(Locale.US, "%.0f", attribute.value)))
        }
        add(SaveSlotMetricItem("People", "${characters.size}"))
    }

private fun formatBytes(sizeBytes: Int): String =
    when {
        sizeBytes <= 0 -> "unknown size"
        sizeBytes >= SUMMARY_BYTES_PER_MIB -> String.format(Locale.US, "%.1f MB", sizeBytes / SUMMARY_BYTES_PER_MIB)
        else -> String.format(Locale.US, "%.0f KB", sizeBytes / SUMMARY_BYTES_PER_KIB)
    }
