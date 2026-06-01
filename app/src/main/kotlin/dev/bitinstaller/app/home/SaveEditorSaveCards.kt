package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import java.util.Locale

private val SaveCardShape = RoundedCornerShape(18.dp)
private val SaveMetricShape = RoundedCornerShape(12.dp)
private val SaveActionShape = RoundedCornerShape(12.dp)
private const val SAVE_CARD_CONTAINER_ARGB = 0x0EFFFFFF
private const val SAVE_CARD_BADGE_ALPHA = 0.08f
private const val SAVE_CARD_SECONDARY_ALPHA = 0.4f
private const val SAVE_CARD_METRIC_ALPHA = 0.06f
private const val COLLAPSED_ATTRIBUTE_COUNT = 3
private const val SUMMARY_BYTES_PER_KIB = 1024f
private const val SUMMARY_BYTES_PER_MIB = SUMMARY_BYTES_PER_KIB * SUMMARY_BYTES_PER_KIB

@Composable
internal fun SaveFileList(
    target: SaveTargetUiState,
    saves: List<BitLifeSaveSummary>,
    onSaveOpen: (BitLifeSaveSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = modifier,
    ) {
        item(contentType = "save-count") {
            Text(
                text = "Found ${saves.size} game save records",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = SAVE_CARD_SECONDARY_ALPHA),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        items(saves, key = { save -> save.path }, contentType = { BitLifeSaveSummary::class }) { save ->
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
    Card(
        onClick = onOpen,
        shape = SaveCardShape,
        colors = CardDefaults.cardColors(containerColor = Color(SAVE_CARD_CONTAINER_ARGB)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            SaveSlotSummaryHeader(save = save)
            if (error != null) {
                SaveSlotStatus(text = error, isError = true)
            } else {
                SaveSlotMetrics(save = save)
            }
            Button(
                enabled = !isWorking,
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = SaveActionShape,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) {
                Text(text = if (isWorking) "Working" else "Open Editor", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SaveSlotSummaryHeader(save: BitLifeSaveSummary) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SaveSlotBubble(slotName = save.slotName)
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = save.heroName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = remember(save) { save.identityLine() },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = SAVE_CARD_SECONDARY_ALPHA),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun SaveSlotBubble(
    slotName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(36.dp),
    ) {
        Surface(
            color = Color.White.copy(alpha = SAVE_CARD_BADGE_ALPHA),
            shape = CircleShape,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = slotName.uppercase(Locale.US),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SaveSlotMetrics(save: BitLifeSaveSummary) {
    val metrics = remember(save) { save.summaryMetrics() }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val rows = remember(metrics) { metrics.chunked(2) }
        rows.forEach { rowMetrics ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
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
        color = Color.White.copy(alpha = SAVE_CARD_METRIC_ALPHA),
        shape = SaveMetricShape,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = label.uppercase(Locale.US),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = SAVE_CARD_SECONDARY_ALPHA),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
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
        color = color.copy(alpha = if (isError) 0.12f else 0.06f),
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
