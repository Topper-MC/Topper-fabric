pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.1"
}

stonecutter {
    kotlinController = true
    shared {
        fun mc(vararg versions: String) {
            for (version in versions) {
                val buildscript = if (sc.eval(version, ">=26.1")) {
                    "build.gradle.kts"
                } else {
                    "build-legacy.gradle.kts"
                }
                version(version, version).buildscript(buildscript)
            }
        }
        mc("26.1.2", "1.21.11", "1.21.10", "1.21.8")
    }
    create(rootProject)
}

rootProject.name = "Topper-fabric"