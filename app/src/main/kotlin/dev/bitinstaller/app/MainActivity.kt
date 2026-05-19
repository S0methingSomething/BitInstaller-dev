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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.bitinstaller.app.home.HomeRoute
import dev.bitinstaller.app.home.previewHomeUiState
import dev.bitinstaller.app.save.SaveScanCache
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.ShizukuSnapshot
import dev.bitinstaller.app.ui.theme.BitInstallerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val presenter: BitInstallerAppPresenter = viewModel()
    val saveCache = remember(context) { SaveScanCache(context) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { presenter.initialize() }

    BindShizukuListeners(
        repository = presenter.repository,
        onSnapshotChanged = { presenter.appState.snapshot = it },
    )

    HomeRoute(
        state = presenter.homeUiState.value,
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
                        saveCache = saveCache,
                    ),
            ),
    )
}

@Composable
private fun BindShizukuListeners(
    repository: ShizukuMonetizationRepository,
    onSnapshotChanged: (ShizukuSnapshot) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(repository) {
        fun refreshStatus() {
            coroutineScope.launch {
                val snapshot = withContext(Dispatchers.IO) { repository.checkStatus() }
                onSnapshotChanged(snapshot)
            }
        }
        val binderDeadListener =
            Shizuku.OnBinderDeadListener {
                refreshStatus()
            }
        val binderReceivedListener =
            Shizuku.OnBinderReceivedListener {
                refreshStatus()
            }
        val permissionListener =
            Shizuku.OnRequestPermissionResultListener { requestCode, _ ->
                if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
                    refreshStatus()
                }
            }

        runCatching { Shizuku.addBinderReceivedListenerSticky(binderReceivedListener) }
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
