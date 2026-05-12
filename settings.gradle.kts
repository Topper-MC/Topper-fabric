pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.3"
    id("dev.kikugie.loom-back-compat") version "0.3"
}

stonecutter {
    create(rootProject) {
        versions("26.1.2", "1.21.11")
        vcsVersion = "26.1.2"
    }
}

rootProject.name = "Topper-fabric"