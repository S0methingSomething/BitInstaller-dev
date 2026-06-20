package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveEditableValueKind

private const val PROPERTY_LABEL_ALPHA = 0.45f
private const val PROPERTY_INPUT_ALPHA = 0.04f
private const val EXPANDABLE_CARD_ALPHA = 0.06f
private const val EXPANDABLE_CARD_EXPANDED_ALPHA = 0.08f
private const val META_ALPHA = 0.35f
private const val CHEVRON_ALPHA = 0.50f
private const val CHIP_ALPHA = 0.06f
private const val CHIP_SELECTED_ALPHA = 0.16f
private const val CHIP_LABEL_ALPHA = 0.50f
private const val SUBGROUP_ALPHA = 0.25f
private const val EMPTY_ALPHA = 0.30f
private const val PROPERTY_LABEL_WIDTH = 110
private const val PROPERTY_MIN_HEIGHT = 36
private const val TOGGLE_MIN_HEIGHT = 32
private val PropertyInputShape = RoundedCornerShape(8.dp)
private val ExpandableCardShape = RoundedCornerShape(12.dp)
private val ChipShape = RoundedCornerShape(8.dp)

@Composable
internal fun NotionPropertyRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isBoolean: Boolean = false,
) {
    if (isBoolean) {
        NotionToggleRow(label = label, value = value, onValueChange = onValueChange, modifier = modifier)
    } else {
        NotionTextRow(label = label, value = value, onValueChange = onValueChange, modifier = modifier)
    }
}

@Composable
private fun NotionToggleRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val checked = value.equals("true", ignoreCase = true)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = PROPERTY_LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier =
                Modifier
                    .heightIn(min = TOGGLE_MIN_HEIGHT.dp)
                    .background(Color.White.copy(alpha = if (checked) 0.14f else CHIP_ALPHA), shape = ChipShape),
        ) {
            ToggleOption("ON", checked) { onValueChange("True") }
            ToggleOption("OFF", !checked) { onValueChange("False") }
        }
    }
}

@Composable
private fun NotionTextRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = PROPERTY_LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(PROPERTY_LABEL_WIDTH.dp),
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = PROPERTY_MIN_HEIGHT.dp)
                    .background(Color.White.copy(alpha = PROPERTY_INPUT_ALPHA), shape = PropertyInputShape)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
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
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ToggleOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = ChipShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = if (selected) Color.White else Color.White.copy(alpha = CHIP_LABEL_ALPHA),
            fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
        )
    }
}

internal data class ExpandableCardContent(
    val title: String,
    val subtitle: String,
    val icon: String = "",
    val leadingContent: @Composable (RowScope.() -> Unit)? = null,
)

@Composable
internal fun NotionExpandableCard(
    content: ExpandableCardContent,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    expandedContent: @Composable ColumnScope.() -> Unit,
) {
    val cardAlpha = if (isExpanded) EXPANDABLE_CARD_EXPANDED_ALPHA else EXPANDABLE_CARD_ALPHA
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    Color.White.copy(alpha = cardAlpha),
                    shape = ExpandableCardShape,
                ).clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val leading = content.leadingContent
            if (leading != null) {
                leading()
            } else if (content.icon.isNotEmpty()) {
                Text(text = content.icon, style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (content.subtitle.isNotEmpty()) {
                    Text(
                        text = content.subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color.White.copy(alpha = META_ALPHA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = if (isExpanded) "\u25BC" else "\u25B6",
                color = Color.White.copy(alpha = CHEVRON_ALPHA),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        if (isExpanded) {
            expandedContent()
        }
    }
}

@Composable
internal fun NotionFilterChips(
    chips: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (chips.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
    ) {
        NotionChip(label = "All", selected = selected == null, onClick = { onSelect(null) })
        for (chip in chips) {
            NotionChip(label = chip, selected = selected == chip, onClick = { onSelect(chip) })
        }
    }
}

@Composable
private fun NotionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = ChipShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier =
            Modifier.background(
                color = Color.White.copy(alpha = if (selected) CHIP_SELECTED_ALPHA else CHIP_ALPHA),
                shape = ChipShape,
            ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = if (selected) Color.White else Color.White.copy(alpha = CHIP_LABEL_ALPHA),
            fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
        )
    }
}

@Composable
internal fun NotionSubGroupHeader(
    title: String,
    count: Int? = null,
) {
    val display = if (count != null) "$title ($count)" else title
    Text(
        text = display.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = SUBGROUP_ALPHA),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
    )
}

@Composable
internal fun NotionEmptyMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = EMPTY_ALPHA),
        modifier = Modifier.padding(16.dp),
    )
}

internal fun LazyListScope.notionGroupedFieldItems(
    fields: List<SaveEditableField>,
    draftValues: SnapshotStateMap<String, String>,
    onDraftChange: (SaveEditableField, String) -> Unit,
) {
    if (fields.isEmpty()) {
        item(contentType = "empty") { NotionEmptyMessage(text = "No fields found.") }
        return
    }
    val grouped = fields.groupBy { it.group }.toSortedMap()
    grouped.forEach { (groupName, groupFields) ->
        item(contentType = "subgroup-$groupName") {
            NotionSubGroupHeader(title = groupName, count = groupFields.size)
        }
        items(
            items = groupFields,
            key = { field: SaveEditableField -> field.id },
            contentType = { field: SaveEditableField -> field.valueKind },
        ) { field: SaveEditableField ->
            val isBool = field.valueKind == SaveEditableValueKind.BOOLEAN
            NotionPropertyRow(
                label = field.label,
                value = draftValues.valueFor(field),
                onValueChange = { onDraftChange(field, it) },
                isBoolean = isBool,
            )
        }
    }
}

@Composable
internal fun NotionFieldColumn(
    fields: List<SaveEditableField>,
    draftValues: SnapshotStateMap<String, String>,
    onDraftChange: (SaveEditableField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        for (field in fields) {
            val isBool = field.valueKind == SaveEditableValueKind.BOOLEAN
            NotionPropertyRow(
                label = field.label,
                value = draftValues.valueFor(field),
                onValueChange = { onDraftChange(field, it) },
                isBoolean = isBool,
            )
        }
    }
}
