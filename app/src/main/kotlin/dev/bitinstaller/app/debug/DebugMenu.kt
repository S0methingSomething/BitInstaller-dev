package dev.bitinstaller.app.debug

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.BuildConfig

@Composable
internal fun DebugMenu(
    debug: DebugState,
    scenarioRunner: DebugScenarioRunner,
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (debug.visible.value) {
            DebugToolOverlays(debug = debug)
        }

        if (BuildConfig.DEBUG) {
            DebugMenuTrigger(
                visible = debug.visible.value,
                onToggle = { debug.visible.value = !debug.visible.value },
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = TRIGGER_BOTTOM_OFFSET.dp, end = 16.dp),
            )
        }

        AnimatedVisibility(
            visible = debug.visible.value,
            enter = fadeIn() + slideInVertically { it / SLIDE_DIVISOR },
            exit = fadeOut() + slideOutVertically { it / SLIDE_DIVISOR },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            DebugMenuPanel(
                debug = debug,
                scenarioRunner = scenarioRunner,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DebugToolOverlays(debug: DebugState) {
    RecompositionCounter(debug = debug)
    FpsCounter(debug = debug)
    MemoryMonitor(debug = debug)
    DebugOverlays(debug = debug, modifier = Modifier.fillMaxSize())
}

@Composable
internal fun DebugMenuTrigger(
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = DEBUG_TRIGGER_ALPHA),
        modifier =
            modifier
                .size(TRIGGER_SIZE.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggle),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (visible) "✕" else DBG_LABEL,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = TRIGGER_FONT_SIZE.sp,
            )
        }
    }
}

internal const val TRIGGER_BOTTOM_OFFSET = 72
private const val TRIGGER_SIZE = 42
private const val TRIGGER_FONT_SIZE = 10
private const val SLIDE_DIVISOR = 3
private const val DEBUG_TRIGGER_ALPHA = 0.72f
private const val DBG_LABEL = "DBG"
