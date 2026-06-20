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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.crypto.MonetizationData
import dev.bitinstaller.app.crypto.MonetizationValue

private const val EDITOR_ROW_ALPHA: Float = 0.58f

@Composable
internal fun SimplifiedEditor(
    draftValues: Map<String, String>,
    originalData: MonetizationData,
    onBooleanChanged: (String, Boolean) -> Unit,
    onTextChanged: (String, String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.sizeIn(maxHeight = 350.dp),
    ) {
        items(originalData.entries.toList(), key = { it.key }) { entry ->
            SimplifiedEditorRow(
                keyName = entry.key,
                value = entry.value,
                draftValue = draftValues[entry.key].orEmpty(),
                onBooleanChanged = onBooleanChanged,
                onTextChanged = onTextChanged,
            )
        }
    }
}

@Composable
private fun SimplifiedEditorRow(
    keyName: String,
    value: MonetizationValue,
    draftValue: String,
    onBooleanChanged: (String, Boolean) -> Unit,
    onTextChanged: (String, String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = EDITOR_ROW_ALPHA),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (value) {
            is Boolean -> {
                BooleanRow(
                    keyName = keyName,
                    value = value,
                    onBooleanChanged = onBooleanChanged,
                )
            }

            else -> {
                ValueRow(
                    keyName = keyName,
                    value = value,
                    draftValue = draftValue,
                    onTextChanged = onTextChanged,
                )
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
                .heightIn(min = 48.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        SimplifiedKeyText(
            keyName = keyName,
            supportingText = if (value) "Enabled" else "Disabled",
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        Switch(
            checked = value,
            onCheckedChange = { onBooleanChanged(keyName, it) },
        )
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        SimplifiedKeyText(keyName = keyName, supportingText = if (value is Int) "Int32" else "Base64 payload")
        BasicTextField(
            value = draftValue,
            onValueChange = { onTextChanged(keyName, it) },
            textStyle =
                TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(6.dp),
                            ).padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (draftValue.isEmpty()) {
                        Text(
                            text = "Enter value...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SimplifiedKeyText(
    keyName: String,
    supportingText: String,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = modifier) {
        Text(
            text = monetizationDisplayName(keyName),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = keyName,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = supportingText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
