package dev.bitinstaller.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.bitinstaller.app.home.HomeRoute
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
    val presenter = remember { BitInstallerAppPresenter() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { presenter.initialize(context) }

    // Recover patch presences when Shizuku transitions to READY (e.g. after
    // permission grant), which the one-shot LaunchedEffect(Unit) may miss.
    LaunchedEffect(presenter.appState.snapshot.status) {
        presenter.recoverPresencesIfReady()
    }

    BindShizukuListeners(
        repository = presenter.repository,
        onSnapshotChanged = { presenter.appState.snapshot = it },
    )

    HomeRoute(
        state = presenter.buildHomeUiState(),
        activeSession = presenter.appState.activeSession,
        liveDictionaryPrompt = presenter.appState.liveDictionaryPrompt,
        callbacks =
            buildHomeRouteCallbacks(
                context = context,
                deps =
                    AppFlowDeps(
                        repository = presenter.repository,
                        manifestStore = presenter.manifestStore,
                        operationLock = presenter.operationLock,
                        coroutineScope = coroutineScope,
                        appState = presenter.appState,
                    ),
            ),
    )
}

@Composable
private fun BindShizukuListeners(
    repository: ShizukuMonetizationRepository,
    onSnapshotChanged: (ShizukuSnapshot) -> Unit,
) {
    DisposableEffect(repository) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val refreshStatus = { onSnapshotChanged(repository.checkStatus()) }
        val binderDeadListener = Shizuku.OnBinderDeadListener(refreshStatus)
        val binderReceivedListener =
            Shizuku.OnBinderReceivedListener {
                handler.post { refreshStatus() }
            }
        val permissionListener =
            Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
                if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
                    when (grantResult) {
                        PackageManager.PERMISSION_GRANTED -> {
                            onSnapshotChanged(
                                ShizukuSnapshot(
                                    status = ShizukuAccessStatus.READY,
                                    uid = runCatching { Shizuku.getUid() }.getOrNull(),
                                ),
                            )
                        }

                        else -> {
                            refreshStatus()
                        }
                    }
                }
            }

        runCatching { Shizuku.addBinderReceivedListener(binderReceivedListener) }
        runCatching { Shizuku.addBinderDeadListener(binderDeadListener) }
        runCatching { Shizuku.addRequestPermissionResultListener(permissionListener) }
        refreshStatus()

        onDispose {
            runCatching { Shizuku.removeBinderReceivedListener(binderReceivedListener) }
            runCatching { Shizuku.removeBinderDeadListener(binderDeadListener) }
            runCatching { Shizuku.removeRequestPermissionResultListener(permissionListener) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BitInstallerPreview() {
    BitInstallerTheme {
        HomeRoute(state = previewHomeUiState())
    }
}
