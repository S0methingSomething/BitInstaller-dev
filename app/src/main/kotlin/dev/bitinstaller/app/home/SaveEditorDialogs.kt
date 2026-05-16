package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

@Composable
internal fun SaveFieldEditDialog(
    draft: SaveFieldEditDraft,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(draft.field.id) { mutableStateOf(draft.field.value) }
    val validationError = remember(value, draft.field.valueKind) { draft.field.valueKind.validateEditInput(value) }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxSize(),
        ) {
            SaveFieldEditContent(
                state =
                    SaveFieldEditContentState(
                        draft = draft,
                        value = value,
                        validationError = validationError,
                    ),
                actions =
                    SaveFieldEditContentActions(
                        onValueChange = { value = it },
                        onDismissRequest = onDismissRequest,
                        onConfirm = { onConfirm(value) },
                    ),
                modifier =
                    Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
            )
        }
    }
}

@Composable
internal fun SaveAdvancedFieldsDialog(
    targetName: String,
    save: BitLifeSaveSummary,
    recentFieldIds: List<String>,
    onDismissRequest: () -> Unit,
    onFieldClick: (SaveEditableField) -> Unit,
) {
    var query by remember(save.path) { mutableStateOf("") }
    var filter by remember(save.path) { mutableStateOf(AdvancedFieldFilter.ALL) }
    var sort by remember(save.path) { mutableStateOf(AdvancedFieldSort.RECENT_FIRST) }
    val filtered =
        remember(query, filter, sort, recentFieldIds, save.advancedFields) {
            save.advancedFields.filteredAndSorted(
                query = query,
                recentFieldIds = recentFieldIds,
                filter = filter,
                sort = sort,
            )
        }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxSize(),
        ) {
            SaveAdvancedFieldsContent(
                state =
                    SaveAdvancedFieldsContentState(
                        targetName = targetName,
                        save = save,
                        fields = filtered,
                        recentFieldIds = recentFieldIds,
                        query = query,
                        filter = filter,
                        sort = sort,
                    ),
                actions =
                    SaveAdvancedFieldsContentActions(
                        onQueryChange = { query = it },
                        onFilterChange = { filter = filter.next() },
                        onSortChange = { sort = sort.next() },
                        onFieldClick = onFieldClick,
                        onClose = onDismissRequest,
                    ),
                modifier =
                    Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
            )
        }
    }
}
