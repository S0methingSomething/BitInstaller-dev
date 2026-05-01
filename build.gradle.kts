import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.versions)
    alias(libs.plugins.version.catalog.update)
}

allprojects {
    pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<KtlintExtension> {
            android.set(true)
            ignoreFailures.set(false)
            filter {
                exclude("**/build/**")
                include("**/*.kt")
                include("**/*.kts")
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
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
    description = "Runs the full local quality gate."
    dependsOn(
        "ktlintCheck",
        ":app:detekt",
        ":app:testDebugUnitTest",
        ":app:lintDebug",
        ":app:assembleDebug",
        "buildHealth",
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
