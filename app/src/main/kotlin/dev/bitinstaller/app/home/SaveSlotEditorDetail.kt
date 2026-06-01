package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

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
