package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeRoute(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    activeSession: PatchEditorSession? = null,
    liveDictionaryPrompt: LiveDictionaryPromptUiState? = null,
    callbacks: HomeRouteCallbacks = HomeRouteCallbacks(),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HomeContent(
            state = state,
            onDashboardActionClick = callbacks.onDashboardActionClick,
            onPatchClick = callbacks.onPatchClick,
        )

        activeSession?.let { session ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.52f)),
            ) {
                PatchEditorScene(
                    target = session.target,
                    contentAlpha = 1f,
                    onDismissRequest = callbacks.onDismissSession,
                    config = PatchEditorSceneConfig(
                        initialData = session.initialData,
                        saveData = { data -> callbacks.onSaveSession(session, data) },
                    ),
                )
            }
        }

        liveDictionaryPrompt?.let { prompt ->
            LiveDictionaryPrompt(
                prompt = prompt,
                onDismissRequest = callbacks.onDismissLiveDictionaryPrompt,
                onConfirm = callbacks.onConfirmLiveDictionaryFix,
            )
        }
    }
}

@Composable
private fun LiveDictionaryPrompt(
    prompt: LiveDictionaryPromptUiState,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = prompt.title) },
        text = { Text(text = prompt.message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = prompt.confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        },
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onDashboardActionClick: () -> Unit,
    onPatchClick: (PatchTargetUiState) -> Unit,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        item { HeroSection(state = state) }
        item { DashboardSection(status = state.backendStatus, onActionClick = onDashboardActionClick) }
        item {
            PatchTargetsSection(
                targets = state.patchTargets,
                onPatchClick = onPatchClick,
            )
        }
    }
}

@Composable
private fun HeroSection(state: HomeUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
    ) {
        Text(
            text = state.title,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = state.summary,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
