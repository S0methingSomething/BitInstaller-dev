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
    var selectedTab by remember(save.path) { mutableStateOf(SAVE_DETAIL_TAB_STATS) }
    var draft by remember(save.path) { mutableStateOf(SaveSlotEditDraft()) }
    var showDiscardPrompt by remember(save.path) { mutableStateOf(false) }
    LaunchedEffect(target.editMessageTokens[save.path]) { draft = SaveSlotEditDraft() }
    BackHandler(enabled = draft.isDirty) { showDiscardPrompt = true }

    SaveDiscardPrompt(
        visible = showDiscardPrompt,
        onDismiss = { showDiscardPrompt = false },
        onDiscard = {
            draft = SaveSlotEditDraft()
            showDiscardPrompt = false
            actions.onBackClick()
        },
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp),
    ) {
        SaveSlotEditorHeader(
            save = save,
            dirtyCount = draft.dirtyCount,
            transitionState = transitionState,
        )
        SaveSlotCategoryTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        SaveSlotTabBody(
            state = SaveSlotTabBodyState(target = target, save = save, selectedTab = selectedTab, draft = draft),
            actions =
                SaveSlotTabBodyActions(
                    onDraftChange = { field, value -> draft = draft.update(field, value) },
                ),
            modifier = Modifier.weight(1f),
        )
        if (save.errorMessage == null) {
            SaveDetailActions(
                enabled = target.editingSavePath != save.path,
                dirtyCount = draft.dirtyCount,
                onSaveChanges = { actions.onSaveChanges(draft.toEdits(save)) },
                onDiscardChanges = { draft = SaveSlotEditDraft() },
                onBackClick = {
                    if (draft.isDirty) {
                        showDiscardPrompt = true
                    } else {
                        actions.onBackClick()
                    }
                },
            )
        } else {
            TextButton(
                onClick = actions.onBackClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            ) {
                Text(text = "Back to save slots")
            }
        }
    }
}

@Composable
private fun SaveDiscardPrompt(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
) {
    if (visible) {
        DiscardChangesDialog(onDismiss = onDismiss, onDiscard = onDiscard)
    }
}

@Composable
private fun DiscardChangesDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Discard changes?") },
        text = { Text(text = "This save has unsaved edits. Discard them and return to the save slots?") },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(text = "Discard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
    onSaveChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
    onBackClick: () -> Unit,
) {
    val isDirty = dirtyCount > 0
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            enabled = enabled && isDirty,
            onClick = onSaveChanges,
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
            onClick = onDiscardChanges,
            shape = SaveEditorControlShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        ) {
            Text(text = "Discard Changes", textAlign = TextAlign.Center)
        }
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        ) {
            Text(text = "Back to save slots", textAlign = TextAlign.Center)
        }
    }
}
