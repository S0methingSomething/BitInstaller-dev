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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val AUTO_DISMISS_DELAY_MS = 2000L
private const val TOAST_BACKGROUND_ARGB = 0xF9000000
private const val TOAST_BORDER_ARGB = 0x2BFFFFFF
private const val TOAST_SHAPE_RADIUS_DP = 9999
private const val TOAST_ENTER_OFFSET_Y = 40
private const val TOAST_EXIT_OFFSET_Y = 30
private const val TOAST_ENTER_SCALE = 0.90f
private const val TOAST_EXIT_SCALE = 0.95f
private const val TOAST_LETTER_SPACING_SP = 2f

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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .background(Color(TOAST_BACKGROUND_ARGB), shape = RoundedCornerShape(TOAST_SHAPE_RADIUS_DP.dp))
                .border(
                    width = 1.dp,
                    color = Color(TOAST_BORDER_ARGB),
                    shape = RoundedCornerShape(TOAST_SHAPE_RADIUS_DP.dp),
                ).padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = message.uppercase(),
            color = Color.White,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = TOAST_LETTER_SPACING_SP.sp,
        )
    }
}

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
