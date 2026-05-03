package dev.bitinstaller.app.home

import android.util.Log
import dev.bitinstaller.app.shizuku.ShizukuMonetizationRepository
import dev.bitinstaller.app.targets.PatchTarget
import org.json.JSONObject
import java.security.MessageDigest

private const val TAG = "PatchManifestStore"
private const val MANIFEST_PACKAGE_NAME_KEY: String = "packageName"
private const val MANIFEST_PATH_KEY: String = "path"
private const val MANIFEST_SHA_KEY: String = "sha256"
private const val MANIFEST_UPDATED_AT_KEY: String = "updatedAt"

data class PatchManifestPresence(
    val state: PatchPresenceState,
    val label: String,
)

class PatchManifestStore(
    private val repository: ShizukuMonetizationRepository,
) {
    suspend fun presenceFor(
        target: PatchTarget,
        encryptedContent: String,
    ): PatchManifestPresence {
        val manifest = readRemoteManifest(target)
        val storedHash = manifest?.optString(MANIFEST_SHA_KEY).orEmpty()
        val storedPath = manifest?.optString(MANIFEST_PATH_KEY).orEmpty()
        val currentHash = encryptedContent.sha256()
        val isPatched = storedHash == currentHash && storedPath == target.monetizationVarsPath

        return if (isPatched) {
            PatchManifestPresence(state = PatchPresenceState.PATCHED, label = "Patched")
        } else {
            PatchManifestPresence(state = PatchPresenceState.NOT_PATCHED, label = "Not patched")
        }
    }

    suspend fun recordPatched(
        target: PatchTarget,
        encryptedContent: String,
    ) {
        val entry =
            JSONObject()
                .put(MANIFEST_PACKAGE_NAME_KEY, target.packageName)
                .put(MANIFEST_PATH_KEY, target.monetizationVarsPath)
                .put(MANIFEST_SHA_KEY, encryptedContent.sha256())
                .put(MANIFEST_UPDATED_AT_KEY, System.currentTimeMillis())
        repository.writeManifest(target = target, content = entry.toString(2))
    }

    /** Recover patch presence for a target by comparing the remote manifest
     *  hash against the current MonetizationVars file content. */
    suspend fun recoverPresence(target: PatchTarget): PatchManifestPresence {
        val file =
            runCatching { repository.readMonetizationVars(target) }.getOrNull()
                ?: return PatchManifestPresence(PatchPresenceState.NOT_PATCHED, "Not patched")
        return presenceFor(target, file.content)
    }

    /** Bulk-recover presences for a list of targets. Skips targets whose
     *  MonetizationVars file is unreadable (returns NOT_PATCHED for them). */
    suspend fun recoverPresences(targets: List<PatchTarget>): Map<String, PatchManifestPresence> =
        targets.associate { target ->
            target.packageName to recoverPresence(target)
        }

    private suspend fun readRemoteManifest(target: PatchTarget): JSONObject? {
        val raw = repository.readManifest(target) ?: return null
        return try {
            JSONObject(raw)
        } catch (e: org.json.JSONException) {
            Log.w(TAG, "Corrupt manifest for ${target.packageName}: ${e.message}")
            null
        }
    }
}

private fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray(Charsets.UTF_8))
    return digest.joinToString(separator = "") { byte ->
        "%02x".format(byte)
    }
}
