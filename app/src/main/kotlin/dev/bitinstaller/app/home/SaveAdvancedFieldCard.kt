package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind
import kotlinx.coroutines.delay

private const val FIELD_DRAFT_SYNC_DEBOUNCE_MS = 100L
private val FieldCardShape = RoundedCornerShape(14.dp)
private const val FIELD_CARD_ALPHA = 0.06f
private const val FIELD_TAG_ALPHA = 0.12f
private const val FIELD_RISK_DOT_SIZE_DP = 6

@Composable
internal fun SaveAdvancedFieldCard(
    field: SaveEditableField,
    draftValue: String,
    onDraftChange: (SaveEditableField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val category = field.uiCategory()
    val risk = field.uiRisk()
    var localValue by rememberSaveable(field.id) { mutableStateOf(draftValue) }

    LaunchedEffect(localValue) {
        if (localValue != draftValue) {
            delay(FIELD_DRAFT_SYNC_DEBOUNCE_MS)
            onDraftChange(field, localValue)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = FIELD_CARD_ALPHA), shape = FieldCardShape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        FieldHeaderRow(field = field, category = category, risk = risk)
        FieldControl(
            field = field,
            localValue = localValue,
            onValueChange = { localValue = it },
        )
    }
}

@Composable
private fun FieldHeaderRow(
    field: SaveEditableField,
    category: SaveFieldUiCategory,
    risk: SaveFieldUiRisk,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            FieldCategoryChip(category = category)
            FieldRiskDot(risk = risk)
        }
        Text(
            text = field.memberName,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = 0.25f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FieldCategoryChip(
    category: SaveFieldUiCategory,
    modifier: Modifier = Modifier,
) {
    Text(
        text = category.label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = category.color(),
        fontWeight = FontWeight.Black,
        modifier =
            modifier
                .background(category.color().copy(alpha = FIELD_TAG_ALPHA), shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun FieldRiskDot(
    risk: SaveFieldUiRisk,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(FIELD_RISK_DOT_SIZE_DP.dp)
                .background(risk.color(), shape = CircleShape),
    )
}

@Composable
private fun FieldControl(
    field: SaveEditableField,
    localValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = field.label
    if (field.valueKind == SaveEditableValueKind.BOOLEAN) {
        val checked = localValue.equals("true", ignoreCase = true)
        SaveInlineToggleField(
            label = label,
            checked = checked,
            onCheckedChange = { value ->
                onValueChange(if (value) "True" else "False")
            },
            modifier = modifier.fillMaxWidth(),
        )
    } else {
        val keyboardType =
            when (field.valueKind) {
                SaveEditableValueKind.BYTE,
                SaveEditableValueKind.SHORT,
                SaveEditableValueKind.INT,
                SaveEditableValueKind.LONG,
                -> androidx.compose.ui.text.input.KeyboardType.Number

                SaveEditableValueKind.FLOAT,
                SaveEditableValueKind.DOUBLE,
                -> androidx.compose.ui.text.input.KeyboardType.Decimal

                else -> androidx.compose.ui.text.input.KeyboardType.Text
            }
        SaveInlineTextField(
            label = label,
            value = localValue,
            onValueChange = onValueChange,
            keyboardType = keyboardType,
            modifier = modifier.fillMaxWidth(),
        )
    }
}
