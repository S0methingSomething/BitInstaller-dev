import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dependency.analysis)
}

val releaseStoreFile = providers.environmentVariable("BITINSTALLER_RELEASE_STORE_FILE")
val releaseStorePassword = providers.environmentVariable("BITINSTALLER_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = providers.environmentVariable("BITINSTALLER_RELEASE_KEY_ALIAS")
val releaseKeyPassword = providers.environmentVariable("BITINSTALLER_RELEASE_KEY_PASSWORD")
val debugStoreFile = providers.environmentVariable("BITINSTALLER_DEBUG_STORE_FILE")
val debugStorePassword = providers.environmentVariable("BITINSTALLER_DEBUG_STORE_PASSWORD")
val debugKeyAlias = providers.environmentVariable("BITINSTALLER_DEBUG_KEY_ALIAS")
val debugKeyPassword = providers.environmentVariable("BITINSTALLER_DEBUG_KEY_PASSWORD")

fun List<Provider<String>>.allPresentAndNotBlank(): Boolean =
    all { provider ->
        provider.orNull?.isNotBlank() == true
    }

val hasReleaseSigning =
    listOf(
        releaseStoreFile,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    ).allPresentAndNotBlank()
val hasDebugSigning =
    listOf(
        debugStoreFile,
        debugStorePassword,
        debugKeyAlias,
        debugKeyPassword,
    ).allPresentAndNotBlank()

android {
    namespace = "dev.bitinstaller.app"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "dev.bitinstaller.app"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 2
        versionName = "0.1.1-alpha"
    }

    signingConfigs {
        if (hasDebugSigning) {
            getByName("debug") {
                storeFile = file(debugStoreFile.get())
                storePassword = debugStorePassword.get()
                keyAlias = debugKeyAlias.get()
                keyPassword = debugKeyPassword.get()
            }
        }

        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        debug {
            isPseudoLocalesEnabled = true
            if (hasDebugSigning) {
                signingConfig = signingConfigs.getByName("debug")
            }
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    lint {
        abortOnError = true
        checkDependencies = false
        // Dependabot owns dependency drift; lint version checks are time-volatile under warningsAsErrors.
        disable +=
            setOf(
                "AndroidGradlePluginVersion",
                "GradleDependency",
                "NewerVersionAvailable",
            )
        htmlReport = true
        sarifReport = true
        textReport = true
        warningsAsErrors = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
    ignoreFailures = false
    parallel = false
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.annotation)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.nrbf4j)
    implementation(libs.compose.animation)
    implementation(libs.compose.animation.core)
    implementation(libs.compose.material.icons.core)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.common)
    implementation(libs.navigation.compose)
    implementation(libs.navigation.runtime)
    implementation(libs.compose.icons.extended)

    debugRuntimeOnly(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit4)
}
