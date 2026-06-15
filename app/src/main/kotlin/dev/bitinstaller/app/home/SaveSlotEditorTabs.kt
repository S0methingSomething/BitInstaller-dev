package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

internal fun SnapshotStateMap<String, String>.valueFor(field: SaveEditableField): String = this[field.id] ?: field.value

private const val SAVE_DETAIL_SECTION_LETTER_SPACING_SP = 1f

@Composable
internal fun SaveSlotTabBody(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    modifier: Modifier = Modifier,
) {
    val statsActive = state.selectedTab == SAVE_DETAIL_TAB_STATS
    val peopleActive = state.selectedTab == SAVE_DETAIL_TAB_PEOPLE
    val advancedActive = state.selectedTab == SAVE_DETAIL_TAB_ADVANCED

    Box(modifier = modifier.fillMaxSize()) {
        if (statsActive) {
            SlotDetailLazyColumn {
                saveSlotStatusItem(state = state)
                statsTabItem(state = state, actions = actions)
            }
        }

        if (peopleActive) {
            SlotDetailLazyColumn {
                saveSlotStatusItem(state = state)
                peopleTabItem(state = state, actions = actions)
            }
        }

        if (advancedActive && state.save.errorMessage == null) {
            SaveAdvancedInlineTab(
                save = state.save,
                draftValues = state.draftValues,
                recentFieldIds = state.recentFieldIds,
                onDraftChange = actions.onDraftChange,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SlotDetailLazyColumn(content: LazyListScope.() -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
        content = content,
    )
}

internal data class SaveSlotTabBodyState(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val selectedTab: String,
    val draftValues: SnapshotStateMap<String, String>,
    val recentFieldIds: List<String>,
)

@Immutable
internal data class SaveSlotTabBodyActions(
    val onDraftChange: (SaveEditableField, String) -> Unit,
)

private fun LazyListScope.saveSlotStatusItem(state: SaveSlotTabBodyState) {
    if (state.target.editingSavePath == state.save.path) return
    val statusText = state.target.editErrors[state.save.path] ?: state.save.errorMessage
    if (statusText != null) {
        item(
            contentType = "status",
        ) { SaveSlotStatus(text = statusText, isError = true) }
    }
}

private fun LazyListScope.statsTabItem(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    if (state.save.errorMessage != null) return
    item(contentType = "stats-panel") {
        SaveStatsTabContent(state = state, actions = actions)
    }
}

private fun LazyListScope.peopleTabItem(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    if (state.save.errorMessage != null) return
    item(contentType = "people-characters") {
        SavePeopleTabContent(state = state, actions = actions)
    }
}

@Composable
private fun SaveStatsTabContent(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    SaveDetailPanel(title = "IDENTITY & BIO METRICS") {
        SaveFactRows(save = state.save, draftValues = state.draftValues, onFieldChange = actions.onDraftChange)
        SaveAttributeRows(
            attributes = state.save.attributes,
            draftValues = state.draftValues,
            onFieldChange = actions.onDraftChange,
        )
    }
}

@Composable
private fun SavePeopleTabContent(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    SaveDetailPanel(title = "FAMILY & RELATIONSHIPS") {
        SaveCharacterRows(
            characters = state.save.characters,
            draftValues = state.draftValues,
            onFieldChange = actions.onDraftChange,
        )
    }
}

@Composable
private fun SaveDetailPanel(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    SaveEditorPanel(containerAlpha = 0.04f, shape = SaveEditorControlShape, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = SAVE_DETAIL_TAB_INACTIVE_ALPHA),
                fontWeight = FontWeight.Bold,
                letterSpacing = SAVE_DETAIL_SECTION_LETTER_SPACING_SP.sp,
            )
            content()
        }
    }
}
