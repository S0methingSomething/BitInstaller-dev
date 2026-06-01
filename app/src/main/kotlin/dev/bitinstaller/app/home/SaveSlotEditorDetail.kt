package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

private const val SAVE_DETAIL_TAB_STATS = "stats"
private const val SAVE_DETAIL_TAB_PEOPLE = "people"
private const val SAVE_DETAIL_TAB_ADVANCED = "advanced"
private const val SAVE_DETAIL_TAB_ACTIVE_ALPHA = 0.08f
private const val SAVE_DETAIL_TAB_INACTIVE_ALPHA = 0.4f
private const val SAVE_DETAIL_SECTION_LETTER_SPACING_SP = 1f

@Composable
internal fun SaveSlotEditorDetail(
    target: SaveTargetUiState,
    save: BitLifeSaveSummary,
    actions: SaveSlotEditorDetailActions,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable(save.path) { mutableStateOf(SAVE_DETAIL_TAB_STATS) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp),
    ) {
        SaveSlotEditorHeader(save = save, onBackClick = actions.onBackClick)
        SaveSlotCategoryTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        SaveSlotTabBody(
            state = SaveSlotTabBodyState(target = target, save = save, selectedTab = selectedTab),
            actions =
                SaveSlotTabBodyActions(
                    onFieldClick = actions.onFieldClick,
                    onAdvancedClick = actions.onAdvancedClick,
                ),
            modifier = Modifier.weight(1f),
        )
        if (save.errorMessage == null) {
            SaveDetailActions(
                fieldCount = save.advancedFields.size,
                enabled = target.editingSavePath != save.path,
                onAdvancedClick = actions.onAdvancedClick,
                onSaveRevert = actions.onSaveRevert,
            )
        }
    }
}

internal data class SaveSlotEditorDetailActions(
    val onBackClick: () -> Unit,
    val onFieldClick: (SaveEditableField) -> Unit,
    val onAdvancedClick: () -> Unit,
    val onSaveRevert: () -> Unit,
)

@Composable
private fun SaveSlotEditorHeader(
    save: BitLifeSaveSummary,
    onBackClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Back to save slots")
        }
        SaveEditorPanel(containerAlpha = 0.055f, shape = SaveEditorControlShape, modifier = Modifier.fillMaxWidth()) {
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
private fun SaveSlotTabBody(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    modifier: Modifier = Modifier,
) {
    val target = state.target
    val save = state.save
    val statusText = target.editErrors[save.path] ?: save.errorMessage
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (target.editingSavePath == save.path) {
            item(contentType = "status") { SaveSlotStatus(text = "Working on save...", isError = false) }
        } else if (statusText != null) {
            item(contentType = "status") { SaveSlotStatus(text = statusText, isError = true) }
        }

        if (save.errorMessage == null) {
            when (state.selectedTab) {
                SAVE_DETAIL_TAB_STATS -> {
                    item(contentType = "stats") {
                        SaveDetailPanel(title = "IDENTITY & BIO METRICS") {
                            SaveFactRows(save = save, onFieldClick = actions.onFieldClick)
                            SaveAttributeRows(attributes = save.attributes, onFieldClick = actions.onFieldClick)
                        }
                    }
                }

                SAVE_DETAIL_TAB_PEOPLE -> {
                    item(contentType = "people") {
                        SaveDetailPanel(title = "FAMILY & RELATIONSHIPS") {
                            SaveCharacterRows(characters = save.characters, onFieldClick = actions.onFieldClick)
                        }
                    }
                }

                SAVE_DETAIL_TAB_ADVANCED -> {
                    item(contentType = "advanced") {
                        SaveDetailPanel(title = "ADVANCED VARIABLES") {
                            Text(
                                text = "Open the complete registry-style variable stream for this save.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = SAVE_DETAIL_TAB_INACTIVE_ALPHA),
                            )
                            Button(
                                onClick = actions.onAdvancedClick,
                                shape = SaveEditorControlShape,
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black,
                                    ),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                            ) {
                                Text(text = "Open Advanced Editor", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class SaveSlotTabBodyState(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val selectedTab: String,
)

private data class SaveSlotTabBodyActions(
    val onFieldClick: (SaveEditableField) -> Unit,
    val onAdvancedClick: () -> Unit,
)

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

@Composable
private fun SaveDetailActions(
    fieldCount: Int,
    enabled: Boolean,
    onAdvancedClick: () -> Unit,
    onSaveRevert: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            enabled = enabled,
            onClick = onAdvancedClick,
            shape = SaveEditorControlShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        ) {
            Text(
                text = "Open Advanced Editor · $fieldCount",
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
        }
        FilledTonalButton(
            enabled = enabled,
            onClick = onSaveRevert,
            shape = SaveEditorControlShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        ) {
            Text(text = "Revert from backup", textAlign = TextAlign.Center)
        }
    }
}
