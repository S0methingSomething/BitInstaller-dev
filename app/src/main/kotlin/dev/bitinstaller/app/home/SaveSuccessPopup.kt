package dev.bitinstaller.app.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val AUTO_DISMISS_DELAY_MS = 2400L
private const val TOAST_BG_ALPHA = 0.92f
private const val TOAST_BORDER_ALPHA = 0.18f
private const val TOAST_ACCENT_ALPHA = 0.85f
private const val TOAST_TEXT_ALPHA = 0.95f
private const val TOAST_ACCENT_START = 0xFF34D399
private const val TOAST_ACCENT_END = 0xFF10B981
private const val TOAST_ACCENT_RADIUS_DP = 50
private const val TOAST_ENTER_OFFSET_Y = 24
private const val TOAST_EXIT_OFFSET_Y = 16
private const val TOAST_ENTER_SCALE = 0.96f
private const val TOAST_EXIT_SCALE = 0.98f

internal data class SaveSuccessPopup(
    val path: String,
    val token: Int,
    val message: String,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SaveEditorSuccessPopup(
    popup: SaveSuccessPopup?,
    onDismiss: (SaveSuccessPopup) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(popup) {
        if (popup != null) {
            delay(AUTO_DISMISS_DELAY_MS)
            onDismiss(popup)
        }
    }
    AnimatedVisibility(
        visible = popup != null,
        enter =
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                slideInVertically(
                    initialOffsetY = { TOAST_ENTER_OFFSET_Y },
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                ) +
                scaleIn(
                    initialScale = TOAST_ENTER_SCALE,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                ),
        exit =
            fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                slideOutVertically(
                    targetOffsetY = { TOAST_EXIT_OFFSET_Y },
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                ) +
                scaleOut(
                    targetScale = TOAST_EXIT_SCALE,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                ),
        modifier = modifier,
    ) {
        SaveEditorToast(message = popup?.message.orEmpty())
    }
}

@Composable
private fun SaveEditorToast(message: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .sizeIn(maxWidth = 360.dp)
                .background(
                    color = Color.Black.copy(alpha = TOAST_BG_ALPHA),
                    shape = ToastShape,
                ).border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = TOAST_BORDER_ALPHA),
                    shape = ToastShape,
                ).padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .size(22.dp)
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color(TOAST_ACCENT_START).copy(alpha = TOAST_ACCENT_ALPHA),
                                        Color(TOAST_ACCENT_END).copy(alpha = TOAST_ACCENT_ALPHA),
                                    ),
                            ),
                        shape = RoundedCornerShape(TOAST_ACCENT_RADIUS_DP),
                    ),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = message,
            color = Color.White.copy(alpha = TOAST_TEXT_ALPHA),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp,
            maxLines = 2,
        )
    }
}

private val ToastShape = RoundedCornerShape(12.dp)

internal fun SaveTargetUiState.saveSuccessPopup(dismissedTokens: Map<String, Int>): SaveSuccessPopup? =
    editMessages
        .asSequence()
        .mapNotNull { (path, message) ->
            val token = editMessageTokens[path] ?: return@mapNotNull null
            SaveSuccessPopup(path = path, token = token, message = message)
                .takeUnless { popup -> dismissedTokens[popup.path] == popup.token }
        }.firstOrNull()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeNoticePopup(
    notice: HomeNoticeUiState?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(notice?.token) {
        if (notice != null) {
            delay(AUTO_DISMISS_DELAY_MS)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = notice != null,
        enter =
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                slideInVertically(
                    initialOffsetY = { TOAST_ENTER_OFFSET_Y },
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                ) +
                scaleIn(
                    initialScale = TOAST_ENTER_SCALE,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                ),
        exit =
            fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                slideOutVertically(
                    targetOffsetY = { TOAST_EXIT_OFFSET_Y },
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                ) +
                scaleOut(
                    targetScale = TOAST_EXIT_SCALE,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                ),
        modifier = modifier,
    ) {
        SaveEditorToast(message = notice?.message.orEmpty())
    }
}
