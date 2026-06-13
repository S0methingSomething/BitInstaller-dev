package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.SaveEditableField
import java.util.Locale

private const val FIELD_CARD_ALPHA = 0.06f
private const val FIELD_LABEL_ALPHA = 0.45f
private const val FIELD_VALUE_ALPHA = 0.04f
private const val FIELD_TOGGLE_ALPHA = 0.06f
private const val FIELD_TOGGLE_SELECTED_ALPHA = 0.14f
private const val FIELD_BAR_TRACK_ALPHA = 0.1f
private const val FIELD_SLIDER_THUMB_SIZE = 20
private const val FIELD_INPUT_MIN_HEIGHT = 42
private const val ATTRIBUTE_MIN_VALUE = 0f
private const val ATTRIBUTE_MAX_VALUE = 100f
private val FieldCardShape = RoundedCornerShape(14.dp)
private val FieldInputShape = RoundedCornerShape(10.dp)

@Composable
internal fun SaveInlineSliderField(
    label: String,
    value: Float,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = ATTRIBUTE_MIN_VALUE..ATTRIBUTE_MAX_VALUE,
    onValueChangeFinished: (Float) -> Unit,
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value) }
    val thresholdColor = remember(sliderValue, valueRange) { valueThresholdColor(sliderValue, valueRange) }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = FIELD_CARD_ALPHA), shape = FieldCardShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = Color.White.copy(alpha = FIELD_LABEL_ALPHA),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = String.format(Locale.US, "%.0f", sliderValue),
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = thresholdColor,
                fontWeight = FontWeight.Black,
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChangeFinished(sliderValue) },
            valueRange = valueRange,
            colors =
                SliderDefaults.colors(
                    thumbColor = thresholdColor,
                    activeTrackColor = thresholdColor,
                    inactiveTrackColor = Color.White.copy(alpha = FIELD_BAR_TRACK_ALPHA),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(FIELD_SLIDER_THUMB_SIZE.dp),
        )
    }
}

@Composable
internal fun SaveInlineTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = FIELD_CARD_ALPHA), shape = FieldCardShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = FIELD_LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = FIELD_INPUT_MIN_HEIGHT.dp)
                    .background(Color.White.copy(alpha = FIELD_VALUE_ALPHA), shape = FieldInputShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle =
                    TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            )
        }
    }
}

@Composable
internal fun SaveInlineToggleField(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = FIELD_CARD_ALPHA), shape = FieldCardShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = FIELD_LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SaveInlineToggleOption(
                label = "ON",
                selected = checked,
                onClick = { onCheckedChange(true) },
                modifier = Modifier.weight(1f),
            )
            SaveInlineToggleOption(
                label = "OFF",
                selected = !checked,
                onClick = { onCheckedChange(false) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SaveInlineToggleOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier =
            modifier
                .heightIn(min = FIELD_INPUT_MIN_HEIGHT.dp)
                .background(
                    color =
                        if (selected) {
                            Color.White.copy(alpha = FIELD_TOGGLE_SELECTED_ALPHA)
                        } else {
                            Color.White.copy(
                                alpha =
                                FIELD_TOGGLE_ALPHA,
                            )
                        },
                    shape = RoundedCornerShape(8.dp),
                ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = if (selected) Color.White else Color.White.copy(alpha = FIELD_LABEL_ALPHA),
            fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
        )
    }
}

private fun valueThresholdColor(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
): Color {
    val normalized = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    return when {
        normalized >= ATTRIBUTE_GREEN_THRESHOLD_NORMALIZED -> {
            Color(ATTRIBUTE_VALUE_GREEN_ARGB)
        }

        normalized >= ATTRIBUTE_AMBER_THRESHOLD_NORMALIZED -> {
            Color.White
        }

        else -> {
            Color(ATTRIBUTE_VALUE_AMBER_ARGB)
        }
    }
}

private const val ATTRIBUTE_GREEN_THRESHOLD_NORMALIZED = 0.80f
private const val ATTRIBUTE_AMBER_THRESHOLD_NORMALIZED = 0.50f
private const val ATTRIBUTE_VALUE_AMBER_ARGB = 0xFFCCA300
private const val ATTRIBUTE_VALUE_GREEN_ARGB = 0xFF4CAF50
