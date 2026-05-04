package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (value) {
            is Boolean -> {
                BooleanEditorRow(
                    keyName = keyName,
                    value = value,
                    onBooleanChanged = onBooleanChanged,
                )
            }

            else -> {
                ValueEditorRow(
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
private fun BooleanEditorRow(
    keyName: String,
    value: Boolean,
    onBooleanChanged: (String, Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        SimplifiedKeyText(
            keyName = keyName,
            supportingText = if (value) "Enabled" else "Disabled",
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
        )
        Switch(
            checked = value,
            onCheckedChange = { checked -> onBooleanChanged(keyName, checked) },
        )
    }
}

@Composable
private fun ValueEditorRow(
    keyName: String,
    value: MonetizationValue,
    draftValue: String,
    onTextChanged: (String, String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        SimplifiedKeyText(keyName = keyName, supportingText = if (value is Int) "Int32" else "Base64 payload")
        OutlinedTextField(
            value = draftValue,
            onValueChange = { updated -> onTextChanged(keyName, updated) },
            singleLine = true,
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
    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = modifier) {
        Text(
            text = monetizationDisplayName(keyName),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = keyName,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = supportingText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
