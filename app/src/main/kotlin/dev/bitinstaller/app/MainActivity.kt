package dev.bitinstaller.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.bitinstaller.app.home.BackendStatus
import dev.bitinstaller.app.home.HomeRoute
import dev.bitinstaller.app.home.HomeUiState
import dev.bitinstaller.app.home.PatchManifestPresence
import dev.bitinstaller.app.home.PatchManifestStore
import dev.bitinstaller.app.home.PatchPresenceState
import dev.bitinstaller.app.home.PatchSupportState
import dev.bitinstaller.app.home.PatchTargetUiState
import dev.bitinstaller.app.home.previewHomeUiState
import dev.bitinstaller.app.shizuku.ShizukuAccessStatus
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.ShizukuSnapshot
import dev.bitinstaller.app.ui.theme.BitInstallerTheme
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BitInstallerTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    BitInstallerApp()
                }
            }
        }
    }
}

@Composable
private fun BitInstallerApp() {
    val context = LocalContext.current
    val repository = remember { ShizukuMonetizationRepository() }
    val manifestStore = remember(context) { PatchManifestStore(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()
    val appState = remember { BitInstallerAppState(initialSnapshot = repository.snapshot()) }

    BindShizukuListeners(repository = repository, onSnapshotChanged = { appState.snapshot = it })

    HomeRoute(
        state = buildHomeUiState(
            snapshot = appState.snapshot,
            isLoading = appState.isLoading,
            loadError = appState.loadError,
            patchPresence = appState.patchPresence,
        ),
        activeSession = appState.activeSession,
        liveDictionaryPrompt = appState.liveDictionaryPrompt,
        callbacks = buildHomeRouteCallbacks(
            context = context,
            repository = repository,
            manifestStore = manifestStore,
            coroutineScope = coroutineScope,
            appState = appState,
        ),
    )
}

@Composable
private fun BindShizukuListeners(
    repository: ShizukuMonetizationRepository,
    onSnapshotChanged: (ShizukuSnapshot) -> Unit,
) {
    DisposableEffect(repository) {
        val refreshSnapshot = { onSnapshotChanged(repository.snapshot()) }
        val binderReceivedListener = Shizuku.OnBinderReceivedListener(refreshSnapshot)
        val binderDeadListener = Shizuku.OnBinderDeadListener(refreshSnapshot)
        val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, _ ->
            if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
                refreshSnapshot()
            }
        }

        runCatching { Shizuku.addBinderReceivedListener(binderReceivedListener) }
        runCatching { Shizuku.addBinderDeadListener(binderDeadListener) }
        runCatching { Shizuku.addRequestPermissionResultListener(permissionListener) }
        refreshSnapshot()

        onDispose {
            runCatching { Shizuku.removeBinderReceivedListener(binderReceivedListener) }
            runCatching { Shizuku.removeBinderDeadListener(binderDeadListener) }
            runCatching { Shizuku.removeRequestPermissionResultListener(permissionListener) }
        }
    }
}

private fun buildHomeUiState(
    snapshot: ShizukuSnapshot,
    isLoading: Boolean,
    loadError: String?,
    patchPresence: PatchManifestPresence,
): HomeUiState {
    val backendStatus =
        when (snapshot.status) {
            ShizukuAccessStatus.UNAVAILABLE -> BackendStatus.ShizukuUnavailable
            ShizukuAccessStatus.PERMISSION_REQUIRED -> BackendStatus.PermissionRequired
            ShizukuAccessStatus.READY -> BackendStatus.Ready
        }

    return previewHomeUiState().copy(
        backendStatus = backendStatus,
        patchTargets = listOf(
            buildBitLifeTarget(
                snapshot = snapshot,
                isLoading = isLoading,
                loadError = loadError,
                patchPresence = patchPresence,
            ),
        ),
    )
}

private fun buildBitLifeTarget(
    snapshot: ShizukuSnapshot,
    isLoading: Boolean,
    loadError: String?,
    patchPresence: PatchManifestPresence,
): PatchTargetUiState {
    val isReady = snapshot.status == ShizukuAccessStatus.READY
    return PatchTargetUiState(
        name = "BitLife",
        packageName = "com.candywriter.bitlife",
        iconMonogram = "BL",
        versionLabel = "3.27.7",
        supportState = if (isReady) PatchSupportState.READY else PatchSupportState.BACKEND_REQUIRED,
        patchPresenceState = if (isReady) patchPresence.state else PatchPresenceState.UNKNOWN,
        patchPresenceLabel = if (isReady) patchPresence.label else "Locked",
        statusLabel = targetStatusLabel(
            isReady = isReady,
            isLoading = isLoading,
            loadError = loadError,
        ),
        patchLabel = if (isLoading) "Loading" else "Patch",
        patchEnabled = isReady && !isLoading,
    )
}

private fun targetStatusLabel(
    isReady: Boolean,
    isLoading: Boolean,
    loadError: String?,
): String =
    when {
        isLoading -> "Reading MonetizationVars"
        loadError != null -> loadError
        isReady -> "Ready to load BitLife data"
        else -> "Grant Shizuku access first"
    }

@Preview(showBackground = true)
@Composable
fun BitInstallerPreview() {
    BitInstallerTheme {
        HomeRoute(state = previewHomeUiState())
    }
}
