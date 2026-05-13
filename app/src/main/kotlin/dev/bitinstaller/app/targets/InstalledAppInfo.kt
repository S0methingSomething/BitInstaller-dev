package dev.bitinstaller.app.targets

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Snapshot of a target app's install state.
 *
 * [icon] is intentionally excluded from [equals]/[hashCode] because
 * [Drawable] has no value-equality — including it would break Compose
 * recomposition skipping and collection lookups.
 */
class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val internalFilesDirectory: String,
    val icon: Drawable?,
    val isInstalled: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InstalledAppInfo) return false
        return packageName == other.packageName &&
            appName == other.appName &&
            versionName == other.versionName &&
            internalFilesDirectory == other.internalFilesDirectory &&
            isInstalled == other.isInstalled
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + appName.hashCode()
        result = 31 * result + versionName.hashCode()
        result = 31 * result + internalFilesDirectory.hashCode()
        result = 31 * result + isInstalled.hashCode()
        return result
    }
}

/**
 * Resolve install state for [target] via [PackageManager].
 *
 * This performs Binder IPC and **must not** be called on the Main thread.
 * Internally dispatches to [Dispatchers.IO].
 */
suspend fun resolveAppInfo(
    context: Context,
    target: PatchTarget,
): InstalledAppInfo =
    withContext(Dispatchers.IO) {
        val pm = context.packageManager
        runCatching {
            val flags = PackageManager.ApplicationInfoFlags.of(0)
            val appInfo = pm.getApplicationInfo(target.packageName, flags)
            val pkgFlags = PackageManager.PackageInfoFlags.of(0)
            val packageInfo = pm.getPackageInfo(target.packageName, pkgFlags)
            InstalledAppInfo(
                packageName = target.packageName,
                appName = pm.getApplicationLabel(appInfo).toString(),
                versionName = packageInfo.versionName.orEmpty(),
                internalFilesDirectory = "${appInfo.dataDir}/files",
                icon = pm.getApplicationIcon(appInfo),
                isInstalled = true,
            )
        }.getOrElse {
            InstalledAppInfo(
                packageName = target.packageName,
                appName = target.displayName,
                versionName = "",
                internalFilesDirectory = "",
                icon = null,
                isInstalled = false,
            )
        }
    }

/**
 * Resolve install state for all [targets] off the Main thread.
 * Returns a map keyed by package name.
 */
suspend fun resolveAllAppInfo(
    context: Context,
    targets: List<PatchTarget> = ALL_TARGETS,
): Map<String, InstalledAppInfo> =
    withContext(Dispatchers.IO) {
        targets.associate { target ->
            target.packageName to resolveAppInfo(context, target)
        }
    }
