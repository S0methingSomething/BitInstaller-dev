package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.SaveAttributeSummary
import dev.bitinstaller.app.save.SaveEditableField
import java.util.Locale

private const val ATTRIBUTE_MIN_VALUE = 0f
private const val ATTRIBUTE_MAX_VALUE = 100f
private const val ATTRIBUTE_BADGE_ALPHA = 0.18f
private const val ATTRIBUTE_TRACK_ALPHA = 0.1f
private val MeterRowShape = RoundedCornerShape(12.dp)

@Composable
internal fun SaveAttributeMeterRow(
    attribute: SaveAttributeSummary,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    val progress = attribute.value.coerceIn(ATTRIBUTE_MIN_VALUE, ATTRIBUTE_MAX_VALUE) / ATTRIBUTE_MAX_VALUE
    val content: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AttributeMeterBadge(attribute.label)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attribute.label.uppercase(Locale.US),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (attribute.field != null) "Tap to edit" else "Read only",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.0f%%", attribute.value),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
            }
            AttributeMeterBar(progress = progress)
        }
    }

    val field = attribute.field
    if (field != null) {
        Surface(
            onClick = { onFieldClick(field) },
            color = Color.White.copy(alpha = 0.06f),
            shape = MeterRowShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    } else {
        Surface(
            color = Color.White.copy(alpha = 0.06f),
            shape = MeterRowShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    }
}

@Composable
private fun AttributeMeterBadge(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(34.dp)
                .background(
                    color = Color.White.copy(alpha = ATTRIBUTE_BADGE_ALPHA),
                    shape = CircleShape,
                ),
    ) {
        Text(
            text =
                label
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    .orEmpty(),
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun AttributeMeterBar(progress: Float) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = ATTRIBUTE_TRACK_ALPHA), shape = CircleShape),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primary),
        )
    }
}
