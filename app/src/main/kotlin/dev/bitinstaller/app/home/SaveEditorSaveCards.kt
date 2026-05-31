package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import java.util.Locale

private val SaveCardShape = RoundedCornerShape(16.dp)
private val SaveMetricShape = RoundedCornerShape(12.dp)
private const val COLLAPSED_ATTRIBUTE_COUNT = 3
private const val SUMMARY_BYTES_PER_KIB = 1024f
private const val SUMMARY_BYTES_PER_MIB = SUMMARY_BYTES_PER_KIB * SUMMARY_BYTES_PER_KIB

@Composable
internal fun SaveFileList(
    target: SaveTargetUiState,
    saves: List<BitLifeSaveSummary>,
    onSaveOpen: (BitLifeSaveSummary) -> Unit,
    sharedTransitionState: SaveEditorSharedTransitionState = SaveEditorSharedTransitionState.Empty,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        saves.forEach { save ->
            SaveSlotSummaryCard(
                save = save,
                isWorking = target.editingSavePath == save.path,
                error = target.editErrors[save.path] ?: save.errorMessage,
                onOpen = { onSaveOpen(save) },
                sharedTransitionState = sharedTransitionState,
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
    sharedTransitionState: SaveEditorSharedTransitionState,
) {
    Surface(
        onClick = onOpen,
        shape = SaveCardShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f),
        modifier =
            Modifier
                .fillMaxWidth()
                .saveSlotSharedBounds(save = save, transitionState = sharedTransitionState),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            SaveSlotSummaryHeader(save = save, isWorking = isWorking)
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
) {
    val titleWeight by animateExpressiveFontWeight(
        isActive = !isWorking,
        restWeight = FontWeight.SemiBold.weight,
        activeWeight = FontWeight.Black.weight,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SaveSlotBadge(slotName = save.slotName)
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = save.heroName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight(titleWeight),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = remember(save) { save.identityLine() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FilledTonalButton(
            enabled = !isWorking,
            onClick = {},
            shape = RoundedCornerShape(999.dp),
            colors =
                ButtonDefaults.filledTonalButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Text(
                text = if (isWorking) "Working" else "Open",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight(titleWeight),
            )
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
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
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
