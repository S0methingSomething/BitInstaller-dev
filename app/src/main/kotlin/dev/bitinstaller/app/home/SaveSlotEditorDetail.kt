package dev.bitinstaller.app.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveFieldEdit

internal const val SAVE_DETAIL_TAB_STATS = "stats"
internal const val SAVE_DETAIL_TAB_PEOPLE = "people"
internal const val SAVE_DETAIL_TAB_ADVANCED = "advanced"
internal const val SAVE_DETAIL_TAB_ACTIVE_ALPHA = 0.08f
internal const val SAVE_DETAIL_TAB_INACTIVE_ALPHA = 0.4f

@Composable
internal fun SaveSlotEditorDetail(
    target: SaveTargetUiState,
    save: BitLifeSaveSummary,
    actions: SaveSlotEditorDetailActions,
    modifier: Modifier = Modifier,
    transitionState: SaveSlotSharedTransitionState = SaveSlotSharedTransitionState(),
) {
    val initial =
        SaveSlotEditorState(
            target = target,
            save = save,
            selectedTab = SAVE_DETAIL_TAB_STATS,
            draft = SaveSlotEditDraft(),
            showDiscardPrompt = false,
            navigateBack = false,
            editsToSave = null,
        )
    var state by remember(save.path) { mutableStateOf(initial) }
    SaveSlotEditorSideEffects(state = state, actions = actions, onState = { state = it }, saveKey = save.path)
    SaveSlotEditorContent(
        content =
            SaveSlotEditorContent(
                state = state,
                save = save,
                actions = actions,
                onReduce = { state = saveSlotEditorReduce(state, it) },
                transitionState = transitionState,
                modifier = modifier,
            ),
    )
}

private data class SaveSlotEditorContent(
    val state: SaveSlotEditorState,
    val save: BitLifeSaveSummary,
    val actions: SaveSlotEditorDetailActions,
    val onReduce: (SaveSlotEditorEvent) -> Unit,
    val transitionState: SaveSlotSharedTransitionState,
    val modifier: Modifier,
)

@Composable
private fun SaveSlotEditorContent(content: SaveSlotEditorContent) {
    val state = content.state
    val save = content.save
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = content.modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp),
    ) {
        SaveSlotEditorHeader(
            save = save,
            dirtyCount = state.draft.dirtyCount,
            transitionState = content.transitionState,
        )
        SaveSlotCategoryTabs(
            selectedTab = state.selectedTab,
            onTabSelected = { content.onReduce(SaveSlotEditorEvent.TabSelected(it)) },
        )
        SaveSlotTabBody(
            state =
                SaveSlotTabBodyState(
                    target = state.target,
                    save = state.save,
                    selectedTab = state.selectedTab,
                    draft = state.draft,
                ),
            actions =
                SaveSlotTabBodyActions(
                    onDraftChange = { field, value -> content.onReduce(SaveSlotEditorEvent.FieldEdited(field, value)) },
                ),
            modifier = Modifier.weight(1f),
        )
        if (save.errorMessage == null) {
            SaveDetailActions(
                enabled = state.target.editingSavePath != save.path,
                dirtyCount = state.draft.dirtyCount,
                onSaveRequested = { content.onReduce(SaveSlotEditorEvent.SaveRequested) },
                onDiscardRequested = { content.onReduce(SaveSlotEditorEvent.DiscardRequested) },
                onBackRequested = { content.onReduce(SaveSlotEditorEvent.BackRequested) },
            )
        } else {
            TextButton(
                onClick = content.actions.onBackClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            ) {
                Text(text = "Back to save slots")
            }
        }
    }
}

@Composable
private fun DiscardPrompt(
    state: SaveSlotEditorState,
    onEvent: (SaveSlotEditorEvent) -> Unit,
) {
    if (!state.showDiscardPrompt) return
    AlertDialog(
        onDismissRequest = { onEvent(SaveSlotEditorEvent.DismissDiscardPrompt) },
        title = { Text(text = "Discard changes?") },
        text = { Text(text = "This save has unsaved edits. Discard them and return to the save slots?") },
        confirmButton = {
            TextButton(onClick = { onEvent(SaveSlotEditorEvent.ConfirmDiscard) }) {
                Text(text = "Discard")
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(SaveSlotEditorEvent.DismissDiscardPrompt) }) {
                Text(text = "Keep editing")
            }
        },
    )
}

internal data class SaveSlotEditorDetailActions(
    val onBackClick: () -> Unit,
    val onSaveChanges: (List<SaveFieldEdit>) -> Unit,
    val onSaveRevert: () -> Unit,
)

@Composable
private fun SaveSlotEditorHeader(
    save: BitLifeSaveSummary,
    dirtyCount: Int,
    transitionState: SaveSlotSharedTransitionState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        SaveEditorPanel(
            containerAlpha = 0.055f,
            shape = SaveEditorControlShape,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .saveSlotSharedBounds(
                        save = save,
                        transitionState = transitionState,
                    ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SaveSlotBubble(slotName = save.slotName)
                    Column {
                        Text(
                            text = save.heroName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        SaveFileMetaLine(save = save)
                        if (dirtyCount > 0) {
                            Text(
                                text = if (dirtyCount == 1) "1 UNSAVED CHANGE" else "$dirtyCount UNSAVED CHANGES",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveSlotCategoryTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        SaveSlotTabItem(
            label = "Stats",
            tab = SAVE_DETAIL_TAB_STATS,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.weight(1f),
        )
        SaveSlotTabItem(
            label = "Family",
            tab = SAVE_DETAIL_TAB_PEOPLE,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.weight(1f),
        )
        SaveSlotTabItem(
            label = "Advanced",
            tab = SAVE_DETAIL_TAB_ADVANCED,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SaveSlotTabItem(
    label: String,
    tab: String,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = selectedTab == tab
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .background(
                    color = if (active) Color.White.copy(alpha = SAVE_DETAIL_TAB_ACTIVE_ALPHA) else Color.Transparent,
                    shape = SaveEditorControlShape,
                ).padding(vertical = 10.dp),
    ) {
        TextButton(onClick = { onTabSelected(tab) }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                color = Color.White.copy(alpha = if (active) 1f else SAVE_DETAIL_TAB_INACTIVE_ALPHA),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SaveDetailActions(
    enabled: Boolean,
    dirtyCount: Int,
    onSaveRequested: () -> Unit,
    onDiscardRequested: () -> Unit,
    onBackRequested: () -> Unit,
) {
    val isDirty = dirtyCount > 0
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            enabled = enabled && isDirty,
            onClick = onSaveRequested,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = SaveEditorControlShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        ) {
            Text(
                text = if (dirtyCount == 1) "Save 1 Change" else "Save $dirtyCount Changes",
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
        }
        FilledTonalButton(
            enabled = enabled && isDirty,
            onClick = onDiscardRequested,
            shape = SaveEditorControlShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        ) {
            Text(text = "Discard Changes", textAlign = TextAlign.Center)
        }
        TextButton(
            onClick = onBackRequested,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        ) {
            Text(text = "Back to save slots", textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SaveSlotEditorSideEffects(
    state: SaveSlotEditorState,
    actions: SaveSlotEditorDetailActions,
    onState: (SaveSlotEditorState) -> Unit,
    saveKey: String,
) {
    LaunchedEffect(state.target.editMessageTokens[saveKey]) {
        onState(state.copy(draft = SaveSlotEditDraft()))
    }
    LaunchedEffect(state.editsToSave) {
        state.editsToSave?.let { edits -> actions.onSaveChanges(edits) }
    }
    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) {
            onState(saveSlotEditorReduce(state, SaveSlotEditorEvent.NavigateBackHandled))
            actions.onBackClick()
        }
    }
    BackHandler(enabled = state.draft.isDirty && !state.showDiscardPrompt) {
        onState(saveSlotEditorReduce(state, SaveSlotEditorEvent.DiscardRequested))
    }
    BackHandler(enabled = !state.draft.isDirty && !state.showDiscardPrompt) { actions.onBackClick() }
    DiscardPrompt(state = state, onEvent = { onState(saveSlotEditorReduce(state, it)) })
}
