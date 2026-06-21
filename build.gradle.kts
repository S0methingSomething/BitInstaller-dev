import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.versions)
    alias(libs.plugins.version.catalog.update)
}

allprojects {
    pluginManager.withPlugin("com.diffplug.spotless") {
        extensions.configure<SpotlessExtension> {
            kotlin {
                target("**/*.kt")
                ktlint()
            }
            kotlinGradle {
                target("**/*.kts")
                ktlint()
            }
        }
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
}

fun isNonStable(version: String): Boolean {
    val stableKeyword =
        listOf("RELEASE", "FINAL", "GA").any { keyword ->
            version.uppercase().contains(keyword)
        }
    val stableVersionRegex = "^[0-9,.v-]+(-r)?$".toRegex()

    return !stableKeyword && !stableVersionRegex.matches(version)
}

tasks.withType<DependencyUpdatesTask>().configureEach {
    checkConstraints = true
    outputFormatter = "plain"
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

versionCatalogUpdate {
    sortByKey.set(true)
    keep {
        keepUnusedVersions.set(true)
    }
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
            onUnusedDependencies {
                exclude("dev.rikka.shizuku:provider")
            }
        }
    }
}

tasks.register("qualityCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs the local quality gate without packaging an APK."
    dependsOn(
        ":app:spotlessCheck",
        ":app:detekt",
        ":app:testDebugUnitTest",
        ":app:lintDebug",
        "buildHealth",
    )
}

tasks.register("fullBuildCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs quality checks and builds the debug APK. Intended for CI."
    dependsOn(
        "qualityCheck",
        ":app:assembleDebug",
    )
}

tasks.register("fullReleaseCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs quality checks and builds the R8-optimized release APK. Intended for release CI."
    dependsOn(
        "qualityCheck",
        ":app:assembleRelease",
    )
}

tasks.register("updateCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Reports available dependency and version catalog updates."
    dependsOn(
        "dependencyUpdates",
    )
}

tasks.named("check") {
    dependsOn("qualityCheck")
}
