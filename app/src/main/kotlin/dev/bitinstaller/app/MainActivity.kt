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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import dev.bitinstaller.app.home.TargetIcon
import dev.bitinstaller.app.home.TargetPatchState
import dev.bitinstaller.app.home.previewHomeUiState
import dev.bitinstaller.app.shizuku.OperationLock
import dev.bitinstaller.app.shizuku.ShizukuAccessStatus
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.shizuku.ShizukuSnapshot
import dev.bitinstaller.app.targets.ALL_TARGETS
import dev.bitinstaller.app.targets.InstalledAppInfo
import dev.bitinstaller.app.targets.resolveAllAppInfo
import dev.bitinstaller.app.ui.theme.BitInstallerTheme
import kotlinx.coroutines.Dispatchers
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
    val repository = remember { ShizukuMonetizationRepository() }
    val manifestStore = remember(repository) { PatchManifestStore(repository) }
    val operationLock = remember { OperationLock() }
    val coroutineScope = rememberCoroutineScope()

    // Initial snapshot deferred — LaunchedEffect will populate on IO.
    val appState = remember {
        BitInstallerAppState(
            initialSnapshot = ShizukuSnapshot(
                status = ShizukuAccessStatus.UNAVAILABLE,
                uid = null,
            ),
        )
    }

    // Resolve app info asynchronously on Dispatchers.IO instead of blocking
    // the Main thread with 16+ Binder IPC calls during first composition.
    var appInfoMap by remember { mutableStateOf(emptyMap<String, InstalledAppInfo>()) }
    LaunchedEffect(Unit) {
        appInfoMap = resolveAllAppInfo(context)
        // Also resolve initial Shizuku status off Main.
        appState.snapshot = withContext(Dispatchers.IO) { repository.checkStatus() }

        if (appState.snapshot.status == ShizukuAccessStatus.READY) {
            val installed = ALL_TARGETS.filter {
                appInfoMap[it.packageName]?.isInstalled == true
            }
            appState.patchPresences = withContext(Dispatchers.IO) {
                manifestStore.recoverPresences(installed)
            }
        }
    }

    BindShizukuListeners(repository = repository, onSnapshotChanged = { appState.snapshot = it })

    HomeRoute(
        state = buildHomeUiState(
            HomeUiStateInput(
                snapshot = appState.snapshot,
                isLoading = appState.isLoading,
                loadingTargetId = appState.loadingTargetId,
                loadError = appState.loadError,
                patchPresences = appState.patchPresences,
                appInfoMap = appInfoMap,
            ),
        ),
        activeSession = appState.activeSession,
        liveDictionaryPrompt = appState.liveDictionaryPrompt,
        callbacks = buildHomeRouteCallbacks(
            context = context,
            deps = AppFlowDeps(
                repository = repository,
                manifestStore = manifestStore,
                operationLock = operationLock,
                coroutineScope = coroutineScope,
                appState = appState,
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
        // checkStatus() performs Binder IPC — these listeners run on Main,
        // but they fire infrequently (binder connect/disconnect/permission grant)
        // and Shizuku.checkSelfPermission() is fast enough for occasional callbacks.
        val refreshStatus = { onSnapshotChanged(repository.checkStatus()) }
        val binderReceivedListener = Shizuku.OnBinderReceivedListener(refreshStatus)
        val binderDeadListener = Shizuku.OnBinderDeadListener(refreshStatus)
        val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, _ ->
            if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
                refreshStatus()
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

private data class HomeUiStateInput(
    val snapshot: ShizukuSnapshot,
    val isLoading: Boolean,
    val loadingTargetId: String?,
    val loadError: String?,
    val patchPresences: Map<String, PatchManifestPresence>,
    val appInfoMap: Map<String, InstalledAppInfo>,
)

private data class TargetUiInput(
    val target: dev.bitinstaller.app.targets.PatchTarget,
    val info: InstalledAppInfo?,
    val isReady: Boolean,
    val isLoading: Boolean,
    val loadError: String?,
    val presence: PatchManifestPresence?,
)

private fun buildHomeUiState(input: HomeUiStateInput): HomeUiState {
    val backendStatus =
        when (input.snapshot.status) {
            ShizukuAccessStatus.UNAVAILABLE -> BackendStatus.ShizukuUnavailable
            ShizukuAccessStatus.PERMISSION_REQUIRED -> BackendStatus.PermissionRequired
            ShizukuAccessStatus.READY -> BackendStatus.Ready
        }
    val isReady = input.snapshot.status == ShizukuAccessStatus.READY

    return HomeUiState(
        title = "BitInstaller",
        summary = "MonetizationVars editor",
        backendStatus = backendStatus,
        patchTargets = ALL_TARGETS.map { target ->
            buildTargetUiState(
                TargetUiInput(
                    target = target,
                    info = input.appInfoMap[target.packageName],
                    isReady = isReady,
                    isLoading = input.isLoading && input.loadingTargetId == target.packageName,
                    loadError = if (input.loadingTargetId == target.packageName) input.loadError else null,
                    presence = input.patchPresences[target.packageName],
                ),
            )
        }.sortedWith(
            compareByDescending<PatchTargetUiState> { it.isInstalled }.thenBy { it.name },
        ),
    )
}

private fun buildTargetUiState(input: TargetUiInput): PatchTargetUiState {
    val installed = input.info?.isInstalled == true
    return PatchTargetUiState(
        name = input.info?.appName ?: input.target.displayName,
        packageName = input.target.packageName,
        icon = TargetIcon(
            monogram = input.target.monogram,
            drawable = input.info?.icon,
        ),
        versionLabel = input.info?.versionName.orEmpty(),
        isInstalled = installed,
        patchState = TargetPatchState(
            supportState = targetSupportState(isReady = input.isReady, isInstalled = installed),
            presenceState = targetPresenceState(input.isReady, input.presence),
            presenceLabel = targetPresenceLabel(input.isReady, installed, input.presence),
            statusLabel = targetStatusLabel(input.isReady, input.isLoading, input.loadError, installed),
            actionLabel = if (input.isLoading) "Loading" else "Patch",
            actionEnabled = input.isReady && installed && !input.isLoading,
        ),
    )
}

private fun targetSupportState(isReady: Boolean, isInstalled: Boolean): PatchSupportState =
    when {
        !isInstalled -> PatchSupportState.UNSUPPORTED
        isReady -> PatchSupportState.READY
        else -> PatchSupportState.BACKEND_REQUIRED
    }

private fun targetPresenceState(
    isReady: Boolean,
    presence: PatchManifestPresence?,
): PatchPresenceState =
    when {
        !isReady -> PatchPresenceState.UNKNOWN
        presence != null -> presence.state
        else -> PatchPresenceState.UNKNOWN
    }

private fun targetPresenceLabel(
    isReady: Boolean,
    isInstalled: Boolean,
    presence: PatchManifestPresence?,
): String =
    when {
        !isInstalled -> "Not installed"
        !isReady -> "Locked"
        presence != null -> presence.label
        else -> "Unknown"
    }

private fun targetStatusLabel(
    isReady: Boolean,
    isLoading: Boolean,
    loadError: String?,
    isInstalled: Boolean,
): String =
    when {
        !isInstalled -> "App not installed on device"
        isLoading -> "Reading MonetizationVars"
        loadError != null -> loadError
        isReady -> "Ready to load data"
        else -> "Grant Shizuku access first"
    }

@Preview(showBackground = true)
@Composable
fun BitInstallerPreview() {
    BitInstallerTheme {
        HomeRoute(state = previewHomeUiState())
    }
}
