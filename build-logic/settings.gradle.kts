dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven("https://jitpack.io")
        mavenLocal()
    }
    versionCatalogs.create("libs").from(files("../gradle/libs.versions.toml"))
}

rootProject.name = "build-logic"
include(":convention")