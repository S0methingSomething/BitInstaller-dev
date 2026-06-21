package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val ADVANCED_SEARCH_ICON_ALPHA = 0.55f
private const val ADVANCED_SEARCH_HINT_ALPHA = 0.45f
private const val ADVANCED_SEARCH_CONTAINER_ALPHA = 0.06f
private const val ADVANCED_SEARCH_MIN_HEIGHT = 48
private val SearchFieldShape = RoundedCornerShape(14.dp)

@Composable
internal fun SaveAdvancedSearch(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = "Search names, stats, money, flags...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ADVANCED_SEARCH_HINT_ALPHA),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = ADVANCED_SEARCH_ICON_ALPHA),
                modifier = Modifier.size(18.dp),
            )
        },
        trailingIcon = value.clearSearchIcon { onValueChange("") },
        singleLine = true,
        shape = SearchFieldShape,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = ADVANCED_SEARCH_CONTAINER_ALPHA),
                unfocusedContainerColor = Color.White.copy(alpha = ADVANCED_SEARCH_CONTAINER_ALPHA),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = ADVANCED_SEARCH_MIN_HEIGHT.dp),
    )
}

private fun String.clearSearchIcon(onClear: () -> Unit): (@Composable () -> Unit)? =
    if (isNotEmpty()) {
        {
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = ADVANCED_SEARCH_ICON_ALPHA),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    } else {
        null
    }
