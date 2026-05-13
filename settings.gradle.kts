pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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

val localNrbf4j = file("../Nrbf4j")
if (localNrbf4j.isDirectory) {
    includeBuild(localNrbf4j) {
        dependencySubstitution {
            substitute(module("io.github.s0methingsomething:nrbf4j")).using(project(":lib"))
        }
    }
}

include(":app")
