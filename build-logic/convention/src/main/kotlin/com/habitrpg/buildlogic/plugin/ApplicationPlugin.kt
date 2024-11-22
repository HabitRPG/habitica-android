package com.habitrpg.buildlogic.plugin

import com.android.build.gradle.AppExtension
import com.google.gson.Gson
import com.habitrpg.buildlogic.model.HabiticaFlavor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.util.Properties

class ApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.withPlugin("com.android.application") {
            val signingProps = try {
                Properties().apply { load(FileInputStream(File("signingrelease.properties"))) }
            } catch (t: Throwable) {
                Properties()
            }
            val signingPropsAvailable = signingProps.containsKey("STORE_FILE") && signingProps.containsKey("STORE_PASSWORD") &&
                    signingProps.containsKey("KEY_ALIAS") && signingProps.containsKey("KEY_PASSWORD")

            val versionProps = try {
                Properties().apply { load(FileInputStream(File("version.properties"))) }
            } catch (t: Throwable) {
                Properties()
            }
            val versionPropsAvailable = versionProps.containsKey("NAME") && versionProps.containsKey("CODE")
            val currentVersionCode = (versionProps["CODE"] ?: "0").toString().toInt()

            val habiticaFlavor = Gson().fromJson(FileReader(file("habitica-flavor.json")), HabiticaFlavor::class.java)

            extensions.getByType<AppExtension>().apply {
                defaultConfig {
                    versionName = versionProps["NAME"].toString()
                    versionCode = currentVersionCode

                    val habiticaRes = Properties().apply { load(FileInputStream(File(projectDir.absolutePath + "/../habitica.resources"))) }
                    habiticaRes.forEach { key, value -> resValue("string", key.toString(), "\"${value}\"") }
                    val hrpgProps = Properties().apply { load(FileInputStream(File(projectDir.absolutePath + "/../habitica.properties"))) }
                    hrpgProps.forEach { key, value -> buildConfigField("String", key as String, "\"${value}\"") }
                }

                if (signingPropsAvailable && versionPropsAvailable) signingConfigs.register("release") {
                    storeFile = file(signingProps["STORE_FILE"].toString())
                    storePassword = signingProps["STORE_PASSWORD"].toString()
                    keyAlias = signingProps["KEY_ALIAS"].toString()
                    keyPassword = signingProps["KEY_PASSWORD"].toString()
                }

                flavorDimensions(habiticaFlavor.dimension)

                productFlavors {
                    habiticaFlavor.flavors.forEach { flavor ->
                        register(flavor.name) {
                            dimension = habiticaFlavor.dimension
                            versionCode = currentVersionCode + (flavor.versionCodeIncrement ?: 0)
                            if (flavor.testingLevel != null) buildConfigField("String", "TESTING_LEVEL", flavor.testingLevel)
                            if (flavor.appName != null) resValue("string", "app_name", flavor.appName)
                        }
                    }
                }
            }
        }
    }
}