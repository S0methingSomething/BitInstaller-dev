package dev.bitinstaller.app.home

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

private const val PATCH_MANIFEST_FILE_NAME: String = "patch-manifest.json"
private const val MANIFEST_PACKAGE_NAME_KEY: String = "packageName"
private const val MANIFEST_PATH_KEY: String = "path"
private const val MANIFEST_SHA_KEY: String = "sha256"
private const val MANIFEST_UPDATED_AT_KEY: String = "updatedAt"

data class PatchManifestPresence(
    val state: PatchPresenceState,
    val label: String,
)

class PatchManifestStore(context: Context) {
    private val manifestFile = File(context.filesDir, PATCH_MANIFEST_FILE_NAME)

    fun presenceFor(
        packageName: String,
        path: String,
        encryptedContent: String,
    ): PatchManifestPresence {
        val entry = readManifest().optJSONObject(packageName)
        val storedHash = entry?.optString(MANIFEST_SHA_KEY).orEmpty()
        val storedPath = entry?.optString(MANIFEST_PATH_KEY).orEmpty()
        val currentHash = encryptedContent.sha256()
        val isPatched = storedHash == currentHash && storedPath == path

        return if (isPatched) {
            PatchManifestPresence(state = PatchPresenceState.PATCHED, label = "Patched")
        } else {
            PatchManifestPresence(state = PatchPresenceState.NOT_PATCHED, label = "Not patched")
        }
    }

    fun recordPatched(
        packageName: String,
        path: String,
        encryptedContent: String,
    ) {
        val manifest = readManifest()
        manifest.put(
            packageName,
            JSONObject()
                .put(MANIFEST_PACKAGE_NAME_KEY, packageName)
                .put(MANIFEST_PATH_KEY, path)
                .put(MANIFEST_SHA_KEY, encryptedContent.sha256())
                .put(MANIFEST_UPDATED_AT_KEY, System.currentTimeMillis()),
        )
        manifestFile.writeText(manifest.toString(2))
    }

    private fun readManifest(): JSONObject =
        runCatching {
            if (manifestFile.exists()) JSONObject(manifestFile.readText()) else JSONObject()
        }.getOrDefault(JSONObject())
}

private fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return digest.joinToString(separator = "") { byte ->
        "%02x".format(byte)
    }
}
