package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val ADVANCED_SEARCH_CONTAINER_COLOR_ARGB = 0x0DFFFFFF
private const val ADVANCED_SEARCH_ICON_ALPHA = 0.55f
private const val ADVANCED_SEARCH_HINT_ALPHA = 0.45f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SaveAdvancedSearch(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = value,
                onQueryChange = onValueChange,
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { updated -> expanded = updated },
                placeholder = { Text(text = "Search names, stats, money, flags...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = ADVANCED_SEARCH_ICON_ALPHA),
                        modifier = Modifier.size(18.dp),
                    )
                },
                trailingIcon = value.clearSearchIcon { onValueChange("") },
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
            )
        },
        expanded = expanded,
        onExpandedChange = { updated -> expanded = updated },
        colors = SearchBarDefaults.colors(containerColor = Color(ADVANCED_SEARCH_CONTAINER_COLOR_ARGB)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier,
    ) {
        Text(
            text = "Search narrows the editable values below.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = ADVANCED_SEARCH_HINT_ALPHA),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
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
