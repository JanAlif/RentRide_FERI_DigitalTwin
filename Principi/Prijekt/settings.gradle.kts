pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
    }
}


rootProject.name = "untitled"

