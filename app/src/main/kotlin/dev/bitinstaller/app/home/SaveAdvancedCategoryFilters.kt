package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val CategoryChipShape = RoundedCornerShape(8.dp)
private const val CATEGORY_CHIP_ALPHA = 0.14f
private const val CATEGORY_UNSELECTED_ALPHA = 0.08f

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategoryFilterChips(
    selectedCategory: SaveFieldUiCategory?,
    onCategorySelected: (SaveFieldUiCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = {
                Text(
                    text = "All",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                )
            },
            shape = CategoryChipShape,
            colors = categoryChipColors(color = Color.White),
        )
        SaveFieldUiCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                    )
                },
                shape = CategoryChipShape,
                colors = categoryChipColors(color = category.color()),
            )
        }
    }
}

@Composable
private fun categoryChipColors(color: Color): androidx.compose.material3.SelectableChipColors =
    FilterChipDefaults.filterChipColors(
        containerColor = color.copy(alpha = CATEGORY_UNSELECTED_ALPHA),
        selectedContainerColor = color.copy(alpha = CATEGORY_CHIP_ALPHA),
        labelColor = Color.White.copy(alpha = 0.55f),
        selectedLabelColor = Color.White,
    )
