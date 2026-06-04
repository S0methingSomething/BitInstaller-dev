package dev.bitinstaller.app.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DebugSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = DEBUG_SECTION_LABEL_ALPHA),
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}

@Composable
internal fun DebugStatRow(
    label: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = DEBUG_STAT_ROW_ALPHA), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = DEBUG_STAT_LABEL_ALPHA),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

private const val DEBUG_SECTION_LABEL_ALPHA = 0.35f
private const val DEBUG_STAT_ROW_ALPHA = 0.06f
private const val DEBUG_STAT_LABEL_ALPHA = 0.5f
