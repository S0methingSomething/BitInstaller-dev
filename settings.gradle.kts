pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BitInstaller"

val useLocalNrbf4j = providers.gradleProperty("bitinstaller.useLocalNrbf4j").orNull == "true"
val localNrbf4j = file("../Nrbf4j")
if (useLocalNrbf4j && localNrbf4j.isDirectory) {
    includeBuild(localNrbf4j) {
        dependencySubstitution {
            substitute(module("io.github.s0methingsomething:nrbf4j")).using(project(":lib"))
        }
    }
}

include(":app")
