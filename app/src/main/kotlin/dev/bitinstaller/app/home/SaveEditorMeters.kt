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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private const val ATTRIBUTE_GREEN_THRESHOLD = 80f
private const val ATTRIBUTE_AMBER_THRESHOLD = 50f
private const val ATTRIBUTE_VALUE_AMBER_ARGB = 0xFFCCA300
private const val ATTRIBUTE_VALUE_GREEN_ARGB = 0xFF4CAF50
private const val METER_BAR_HEIGHT = 8
private const val METER_SLIDER_THUMB_SIZE = 20
private val MeterRowShape = RoundedCornerShape(12.dp)

@Composable
internal fun SaveAttributeMeterRow(
    attribute: SaveAttributeSummary,
    draft: SaveSlotEditDraft,
    onFieldChange: (SaveEditableField, String) -> Unit,
) {
    val field = attribute.field
    val displayedValue = field?.let { draft.valueFor(it).toFloatOrNull() } ?: attribute.value
    var sliderValue by remember(field?.id, displayedValue) { mutableFloatStateOf(displayedValue) }
    val progress = sliderValue.coerceIn(ATTRIBUTE_MIN_VALUE, ATTRIBUTE_MAX_VALUE) / ATTRIBUTE_MAX_VALUE
    val valueColor = attributeValueColor(sliderValue)

    Column(
        verticalArrangement = Arrangement.spacedBy(9.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.06f), shape = MeterRowShape)
                .padding(14.dp),
    ) {
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
                    text = if (attribute.field != null) "Slide to adjust" else "Read only",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                )
            }
            Text(
                text = String.format(Locale.US, "%.0f%%", sliderValue),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                color = valueColor,
                fontWeight = FontWeight.Black,
            )
        }

        if (field != null) {
            MeterSlider(value = sliderValue, onValueChange = {
                sliderValue = it
            }, onValueChangeFinished = { onFieldChange(field, sliderValue.toString()) }, valueColor = valueColor)
        } else {
            MeterBar(progress = progress, valueColor = valueColor)
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

private fun attributeValueColor(value: Float): Color =
    when {
        value >= ATTRIBUTE_GREEN_THRESHOLD -> Color(ATTRIBUTE_VALUE_GREEN_ARGB)
        value >= ATTRIBUTE_AMBER_THRESHOLD -> Color.White
        else -> Color(ATTRIBUTE_VALUE_AMBER_ARGB)
    }

@Composable
private fun MeterSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueColor: Color,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = ATTRIBUTE_MIN_VALUE..ATTRIBUTE_MAX_VALUE,
        colors =
            SliderDefaults.colors(
                thumbColor = valueColor,
                activeTrackColor = valueColor,
                inactiveTrackColor = Color.White.copy(alpha = ATTRIBUTE_TRACK_ALPHA),
            ),
        modifier = Modifier.fillMaxWidth().height(METER_SLIDER_THUMB_SIZE.dp),
    )
}

@Composable
private fun MeterBar(
    progress: Float,
    valueColor: Color,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(METER_BAR_HEIGHT.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = ATTRIBUTE_TRACK_ALPHA)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progress)
                    .height(METER_BAR_HEIGHT.dp)
                    .background(valueColor),
        )
    }
}
