package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val RecentChipShape = RoundedCornerShape(8.dp)
private const val RECENT_CHIP_ALPHA = 0.06f

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecentFieldsSection(
    labels: List<String>,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "RECENTLY EDITED",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.25f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            labels.forEach { label ->
                FilterChip(
                    selected = false,
                    onClick = { onChipClick(label) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    shape = RecentChipShape,
                    colors =
                        FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = RECENT_CHIP_ALPHA),
                            labelColor = Color.White,
                        ),
                )
            }
        }
    }
}
