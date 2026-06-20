package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.crypto.MonetizationData
import dev.bitinstaller.app.crypto.MonetizationValue

private const val ROW_ALPHA = 0.04f
private const val LABEL_ALPHA = 0.45f
private const val KEY_ALPHA = 0.25f
private const val TOGGLE_BG_ALPHA = 0.06f
private const val TOGGLE_ACTIVE_ALPHA = 0.14f
private const val TOGGLE_LABEL_ALPHA = 0.50f
private const val INPUT_ALPHA = 0.04f
private const val ROW_MIN_HEIGHT = 36
private const val TOGGLE_MIN_HEIGHT = 32
private val RowShape = RoundedCornerShape(8.dp)
private val ToggleShape = RoundedCornerShape(8.dp)

@Composable
internal fun SimplifiedEditor(
    draftValues: Map<String, String>,
    originalData: MonetizationData,
    onBooleanChanged: (String, Boolean) -> Unit,
    onTextChanged: (String, String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.sizeIn(maxHeight = 350.dp),
    ) {
        items(originalData.entries.toList(), key = { it.key }) { entry ->
            val value = entry.value
            when (value) {
                is Boolean -> {
                    BooleanRow(
                        keyName = entry.key,
                        value = value,
                        onBooleanChanged = onBooleanChanged,
                    )
                }

                else -> {
                    ValueRow(
                        keyName = entry.key,
                        value = value,
                        draftValue = draftValues[entry.key].orEmpty(),
                        onTextChanged = onTextChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun BooleanRow(
    keyName: String,
    value: Boolean,
    onBooleanChanged: (String, Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = ROW_MIN_HEIGHT.dp)
                .background(Color.White.copy(alpha = ROW_ALPHA), shape = RowShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        KeyLabel(
            keyName = keyName,
            supportingText = if (value) "True" else "False",
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier =
                Modifier
                    .heightIn(min = TOGGLE_MIN_HEIGHT.dp)
                    .background(
                        Color.White.copy(alpha = if (value) TOGGLE_ACTIVE_ALPHA else TOGGLE_BG_ALPHA),
                        shape = ToggleShape,
                    ),
        ) {
            ToggleOpt("True", value) { onBooleanChanged(keyName, true) }
            ToggleOpt("False", !value) { onBooleanChanged(keyName, false) }
        }
    }
}

@Composable
private fun ValueRow(
    keyName: String,
    value: MonetizationValue,
    draftValue: String,
    onTextChanged: (String, String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = ROW_ALPHA), shape = RowShape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        KeyLabel(keyName = keyName, supportingText = if (value is Int) "Int32" else "Base64 payload")
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = ROW_MIN_HEIGHT.dp)
                    .background(Color.White.copy(alpha = INPUT_ALPHA), shape = RowShape)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            BasicTextField(
                value = draftValue,
                onValueChange = { onTextChanged(keyName, it) },
                textStyle =
                    TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun KeyLabel(
    keyName: String,
    supportingText: String,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = modifier) {
        Text(
            text = monetizationDisplayName(keyName),
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = supportingText,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = KEY_ALPHA),
        )
    }
}

@Composable
private fun ToggleOpt(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = ToggleShape,
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(horizontal = 12.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = if (selected) Color.White else Color.White.copy(alpha = TOGGLE_LABEL_ALPHA),
            fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
        )
    }
}
