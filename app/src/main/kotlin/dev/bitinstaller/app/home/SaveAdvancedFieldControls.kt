package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun SaveAdvancedFieldControls(
    filter: AdvancedFieldFilter,
    sort: AdvancedFieldSort,
    onFilterChange: () -> Unit,
    onSortChange: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AdvancedFieldControlButton(
                label = "Filter: ${filter.label}",
                onClick = onFilterChange,
                modifier = Modifier.weight(1f),
            )
            AdvancedFieldControlButton(
                label = "Sort: ${sort.label}",
                onClick = onSortChange,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = filter.description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AdvancedFieldControlButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        modifier = modifier,
    ) {
        Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
