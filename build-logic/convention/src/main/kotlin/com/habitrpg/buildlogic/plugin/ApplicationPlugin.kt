package com.habitrpg.buildlogic.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.google.gson.Gson
import com.habitrpg.buildlogic.model.HabiticaFlavor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.io.FileReader
import java.util.Properties

class ApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            pluginManager.withPlugin("com.android.application") {
                val signingProps = loadProperties(file("signingrelease.properties"))
                val signingPropsAvailable =
                    signingProps.containsKey("STORE_FILE") && signingProps.containsKey("STORE_PASSWORD") &&
                        signingProps.containsKey("KEY_ALIAS") && signingProps.containsKey("KEY_PASSWORD")

                val versionProps = loadProperties(rootProject.file("version.properties"))
                val versionPropsAvailable = versionProps.containsKey("NAME") && versionProps.containsKey("CODE")
                val currentVersionCode = versionProps.getProperty("CODE")?.toIntOrNull() ?: 0

                val habiticaFlavor = Gson().fromJson(FileReader(file("habitica-flavor.json")), HabiticaFlavor::class.java)

                extensions.getByType<ApplicationExtension>().apply {
                    defaultConfig {
                        if (versionPropsAvailable) {
                            versionName = versionProps.getProperty("NAME")
                            versionCode = currentVersionCode
                        }

                        val habiticaRes = loadProperties(rootProject.file("habitica.resources"))
                        habiticaRes.forEach { key, value -> resValue("string", key.toString(), "\"${value}\"") }
                        val hrpgProps = loadProperties(rootProject.file("habitica.properties"))
                        hrpgProps.forEach { key, value -> buildConfigField("String", key as String, "\"${value}\"") }
                    }

                    if (signingPropsAvailable && versionPropsAvailable) {
                        signingConfigs.register("release") {
                            storeFile = file(signingProps["STORE_FILE"].toString())
                            storePassword = signingProps["STORE_PASSWORD"].toString()
                            keyAlias = signingProps["KEY_ALIAS"].toString()
                            keyPassword = signingProps["KEY_PASSWORD"].toString()
                        }
                    }

                    if (habiticaFlavor.dimension !in flavorDimensions) {
                        flavorDimensions += habiticaFlavor.dimension
                    }

                    productFlavors {
                        habiticaFlavor.flavors.forEach { flavor ->
                            register(flavor.name) {
                                dimension = habiticaFlavor.dimension
                                versionCode = currentVersionCode + (flavor.versionCodeIncrement ?: 0)
                                if (flavor.applicationId != null) applicationId = flavor.applicationId
                                if (flavor.testingLevel != null) buildConfigField("String", "TESTING_LEVEL", flavor.testingLevel)
                                if (flavor.appName != null) resValue("string", "app_name", flavor.appName)
                            }
                        }
                    }
                }
            }
        }
}

private fun loadProperties(file: File): Properties =
    Properties().apply {
        if (file.exists()) {
            file.inputStream().use(::load)
        }
    }
