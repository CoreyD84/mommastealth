pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.13.1" apply false
        id("com.android.library") version "8.13.1" apply false
        id("org.jetbrains.kotlin.android") version "2.2.0" apply false
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" apply false
        id("com.google.gms.google-services") version "4.4.1" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MommaNettie_New"
include(":app")
include(":mommastealth")
include(":safescope")
