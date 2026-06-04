package dev.bitinstaller.app.debug

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun DebugMenuPanel(
    debug: DebugState,
    scenarioRunner: DebugScenarioRunner,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(MENU_TAB_TOOLS) }

    Surface(
        color = Color(DEBUG_PANEL_BACKGROUND_ARGB),
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
        ) {
            DebugMenuHeader(
                debug = debug,
                activeLabel = scenarioRunner.activeLabel(),
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onClose = { debug.visible.value = false },
            )
            when (selectedTab) {
                MENU_TAB_TOOLS -> DebugToolsTab(debug = debug, scenarioRunner = scenarioRunner)
                MENU_TAB_LOG -> DebugLogTab(debug = debug)
                MENU_TAB_OVERRIDES -> DebugOverridesTab(debug = debug)
            }
        }
    }
}

@Composable
internal fun DebugMenuHeader(
    debug: DebugState,
    activeLabel: String?,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onClose: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DebugMenuHeaderRow(debug = debug, activeLabel = activeLabel, onClose = onClose)
        DebugMenuTabRow(selectedTab = selectedTab, onTabSelected = onTabSelected)
    }
}

@Composable
private fun DebugMenuHeaderRow(
    debug: DebugState,
    activeLabel: String?,
    onClose: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column {
            Text(
                text = "Debug Menu",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
            activeLabel?.let { label ->
                Text(
                    text = "Running: $label",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (debug.isRecording.value) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape,
                    modifier = Modifier.size(DEBUG_RECORDING_DOT_SIZE.dp),
                ) {}
                Spacer(modifier = Modifier.width(4.dp))
            }
            TextButton(onClick = onClose) {
                Text(text = "Close", color = Color.White.copy(alpha = DEBUG_CLOSE_ALPHA))
            }
        }
    }
}

@Composable
private fun DebugMenuTabRow(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
    ) {
        listOf(MENU_TAB_TOOLS, MENU_TAB_LOG, MENU_TAB_OVERRIDES).forEach { tab ->
            FilterChip(
                label = tab,
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

@Composable
internal fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color =
            if (selected) {
                Color.White.copy(alpha = DEBUG_CHIP_SELECTED_ALPHA)
            } else {
                Color.White.copy(alpha = DEBUG_CHIP_ALPHA)
            },
        shape = RoundedCornerShape(8.dp),
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else Color.White.copy(alpha = DEBUG_CHIP_TEXT_ALPHA),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

private const val DEBUG_PANEL_BACKGROUND_ARGB = 0xE6131313
private const val DEBUG_RECORDING_DOT_SIZE = 8
private const val DEBUG_CLOSE_ALPHA = 0.6f
private const val DEBUG_CHIP_SELECTED_ALPHA = 0.12f
private const val DEBUG_CHIP_ALPHA = 0.06f
private const val DEBUG_CHIP_TEXT_ALPHA = 0.5f
internal const val MENU_TAB_TOOLS = "Tools"
internal const val MENU_TAB_LOG = "Log"
internal const val MENU_TAB_OVERRIDES = "Overrides"
