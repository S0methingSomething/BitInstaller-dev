package dev.bitinstaller.app.targets

import android.os.Environment

private const val MONETIZATION_VARS_FILE: String = "MonetizationVars"
private const val LIVE_DICTIONARY_DIR: String = "LiveDictionary"
private const val MANIFEST_FILE: String = ".bitinstaller-manifest.json"

/**
 * Identity and path-resolution for a patchable Candywriter app.
 *
 * Package names here must stay in sync with the `<queries>` block in
 * `AndroidManifest.xml` — Android requires literal package strings there,
 * so the duplication is unavoidable.
 */
data class PatchTarget(
    val packageName: String,
    val displayName: String,
    val monogram: String,
) {
    /**
     * External files directory for this package, used in Shizuku shell commands.
     * Resolves via [Environment.getExternalStorageDirectory] rather than
     * hardcoding `/storage/emulated/0`.
     */
    val filesDirectory: String
        get() {
            val root = Environment.getExternalStorageDirectory().absolutePath
            return "$root/Android/data/$packageName/files"
        }

    val monetizationVarsPath: String get() = "$filesDirectory/$MONETIZATION_VARS_FILE"

    val liveDictionaryPath: String get() = "$filesDirectory/$LIVE_DICTIONARY_DIR"

    val manifestPath: String get() = "$filesDirectory/$MANIFEST_FILE"
}

private val BITLIFE_TARGETS: List<PatchTarget> =
    listOf(
        PatchTarget("com.candywriter.bitlife", "BitLife", "BL"),
        PatchTarget("com.goodgamestudios.bitlife.go.life.simulation", "BitLife GO", "GO"),
        PatchTarget("com.goodgamestudios.bitlife.de.deutsch.life.simulation", "BitLife DE", "DE"),
        PatchTarget("com.goodgamestudios.bitlife.es.espanol.simulador.de.vida", "BitLife ES", "ES"),
        PatchTarget("com.goodgamestudios.bitlife.br.portugues.simulacao.de.vida", "BitLife BR", "BR"),
        PatchTarget("com.goodgamestudios.bitlife.fr.francais.simulation.de.vie", "BitLife FR", "FR"),
    )

private val PETGAME_TARGETS: List<PatchTarget> =
    listOf(
        PatchTarget("com.candywriter.doglife", "DogLife", "DL"),
        PatchTarget("com.candywriter.catlife", "CatLife", "CL"),
    )

/**
 * Targets eligible for MonetizationVars patching.
 *
 * CatLife and DogLife do not use the MonetizationVars system the same way
 * BitLife variants do, so they are excluded from the patch editor.
 */
val PATCH_TARGETS: List<PatchTarget> = BITLIFE_TARGETS

/**
 * All supported targets, including save-editor-only apps.
 *
 * Save editor works on every Candywriter life-sim app regardless of
 * MonetizationVars support.
 */
val ALL_TARGETS: List<PatchTarget> = BITLIFE_TARGETS + PETGAME_TARGETS

fun findTarget(packageName: String): PatchTarget? = ALL_TARGETS.find { it.packageName == packageName }
