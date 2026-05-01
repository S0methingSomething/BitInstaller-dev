package dev.bitinstaller.app

import android.content.Context
import android.content.Intent

private const val SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api"

internal fun openShizukuApp(context: Context, onError: (String?) -> Unit) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE_NAME)
    if (launchIntent == null) {
        onError("Shizuku app is not installed")
        return
    }
    context.startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
