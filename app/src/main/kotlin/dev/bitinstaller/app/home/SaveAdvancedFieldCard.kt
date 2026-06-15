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
import dev.bitinstaller.app.save.explanation
import kotlinx.coroutines.delay

private const val FIELD_DRAFT_SYNC_DEBOUNCE_MS = 100L
private val FieldCardShape = RoundedCornerShape(14.dp)
private const val FIELD_CARD_ALPHA = 0.06f
private const val FIELD_TAG_ALPHA = 0.12f
private const val FIELD_RISK_DOT_SIZE_DP = 6
private const val FIELD_HEADER_ALPHA = 0.35f
private const val FIELD_PATH_MAX_LINES = 2
private const val FIELD_NOTICE_ALPHA = 0.08f
private const val FIELD_NOTICE_LABEL_ALPHA = 0.9f
private const val FIELD_DESCRIPTION_ALPHA = 0.5f
private val FieldNoticeShape = RoundedCornerShape(8.dp)

@Composable
internal fun SaveAdvancedFieldCard(
    field: SaveEditableField,
    draftValue: String,
    onDraftChange: (SaveEditableField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val category = field.uiCategory()
    val risk = field.uiRisk()
    val explanation = field.explanation()
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
        if (explanation != null) {
            FieldFooter(explanation = explanation, risk = risk)
        }
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        FieldCategoryChip(category = category)
        FieldRiskDot(risk = risk)
        Text(
            text = field.formatBreadcrumb(),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = FIELD_HEADER_ALPHA),
            maxLines = FIELD_PATH_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
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
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
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
private fun FieldFooter(
    explanation: dev.bitinstaller.app.save.SaveFieldExplanation,
    risk: SaveFieldUiRisk,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = explanation.description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = FIELD_DESCRIPTION_ALPHA),
        )
        if (risk != SaveFieldUiRisk.SAFE) {
            FieldRiskNotice(risk = risk)
        }
    }
}

@Composable
private fun FieldRiskNotice(
    risk: SaveFieldUiRisk,
    modifier: Modifier = Modifier,
) {
    val noticeColor = risk.color()
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .background(noticeColor.copy(alpha = FIELD_NOTICE_ALPHA), shape = FieldNoticeShape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = risk.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = noticeColor.copy(alpha = FIELD_NOTICE_LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = risk.noticeText(),
            style = MaterialTheme.typography.bodySmall,
            color = noticeColor.copy(alpha = FIELD_NOTICE_LABEL_ALPHA),
            modifier = Modifier.weight(1f),
        )
    }
}

private fun SaveFieldUiRisk.noticeText(): String =
    when (this) {
        SaveFieldUiRisk.DANGER -> "Editing this field can break cloud sync or corrupt your save."
        SaveFieldUiRisk.MEDIUM -> "Invalid values may cause game glitches or unexpected behavior."
        SaveFieldUiRisk.SAFE -> ""
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

private fun SaveEditableField.formatBreadcrumb(): String {
    val segments = path.split(" / ")
    val filtered =
        segments.filterNot { segment ->
            segment.equals("items", ignoreCase = true) ||
                segment.equals(label, ignoreCase = true)
        }
    return filtered.joinToString(" / ")
}
