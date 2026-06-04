package dev.bitinstaller.app.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

private const val SAVE_DETAIL_SECTION_LETTER_SPACING_SP = 1f
private const val SAVE_DETAIL_TAB_SLIDE_DIVISOR = 3

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SaveSlotTabBody(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
    modifier: Modifier = Modifier,
) {
    val effectsFloatSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatialIntSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    AnimatedContent(
        targetState = state.selectedTab,
        transitionSpec = {
            val movingForward = targetState.saveTabIndex() >= initialState.saveTabIndex()
            val enter =
                slideInHorizontally(animationSpec = spatialIntSpec) { width ->
                    if (movingForward) width / SAVE_DETAIL_TAB_SLIDE_DIVISOR else -width / SAVE_DETAIL_TAB_SLIDE_DIVISOR
                } + fadeIn(animationSpec = effectsFloatSpec)
            val exit =
                slideOutHorizontally(animationSpec = spatialIntSpec) { width ->
                    if (movingForward) -width / SAVE_DETAIL_TAB_SLIDE_DIVISOR else width / SAVE_DETAIL_TAB_SLIDE_DIVISOR
                } + fadeOut(animationSpec = effectsFloatSpec)
            enter togetherWith exit
        },
        modifier = modifier.fillMaxWidth(),
        label = "save_slot_tab_transition",
    ) { selectedTab ->
        val tabState = state.copy(selectedTab = selectedTab)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            saveSlotStatusItem(state = tabState)
            saveSlotTabItems(state = tabState, actions = actions)
        }
    }
}

internal data class SaveSlotTabBodyState(
    val target: SaveTargetUiState,
    val save: BitLifeSaveSummary,
    val selectedTab: String,
)

internal data class SaveSlotTabBodyActions(
    val onFieldChange: (SaveEditableField, String) -> Unit,
    val onAttributeChange: (SaveEditableField, Float) -> Unit,
    val onAdvancedClick: () -> Unit,
)

private fun LazyListScope.saveSlotStatusItem(state: SaveSlotTabBodyState) {
    val statusText = state.target.editErrors[state.save.path] ?: state.save.errorMessage
    if (state.target.editingSavePath == state.save.path) {
        item(contentType = "status") {
            SaveSlotStatus(text = "Working on save...", isError = false, modifier = Modifier.animateItem())
        }
    } else if (statusText != null) {
        item(
            contentType = "status",
        ) { SaveSlotStatus(text = statusText, isError = true, modifier = Modifier.animateItem()) }
    }
}

private fun LazyListScope.saveSlotTabItems(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    if (state.save.errorMessage != null) return
    item(contentType = "tab-content") {
        Box(Modifier.animateItem()) {
            when (state.selectedTab) {
                SAVE_DETAIL_TAB_STATS -> SaveStatsTabContent(state = state, actions = actions)
                SAVE_DETAIL_TAB_PEOPLE -> SavePeopleTabContent(state = state, actions = actions)
                SAVE_DETAIL_TAB_ADVANCED -> SaveAdvancedTabContent(actions = actions)
            }
        }
    }
}

@Composable
private fun SaveStatsTabContent(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    SaveDetailPanel(title = "IDENTITY & BIO METRICS") {
        SaveFactRows(save = state.save, onFieldChange = actions.onFieldChange)
        SaveAttributeRows(attributes = state.save.attributes, onFieldChange = actions.onAttributeChange)
    }
}

@Composable
private fun SavePeopleTabContent(
    state: SaveSlotTabBodyState,
    actions: SaveSlotTabBodyActions,
) {
    SaveDetailPanel(title = "FAMILY & RELATIONSHIPS") {
        SaveCharacterRows(characters = state.save.characters, onFieldChange = actions.onFieldChange)
    }
}

@Composable
private fun SaveAdvancedTabContent(actions: SaveSlotTabBodyActions) {
    SaveDetailPanel(title = "ADVANCED VARIABLES") {
        Text(
            text = "Open the complete registry-style variable stream for this save.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = SAVE_DETAIL_TAB_INACTIVE_ALPHA),
        )
        Button(
            onClick = actions.onAdvancedClick,
            shape = SaveEditorControlShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        ) {
            Text(text = "Open Advanced Editor", fontWeight = FontWeight.Black)
        }
    }
}

private fun String.saveTabIndex(): Int =
    when (this) {
        SAVE_DETAIL_TAB_STATS -> 0
        SAVE_DETAIL_TAB_PEOPLE -> 1
        SAVE_DETAIL_TAB_ADVANCED -> 2
        else -> 0
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
