import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.habitrpg.buildlogic"
version = "0.1.0"

java {
    sourceCompatibility = VERSION_11
    targetCompatibility = VERSION_11
}

kotlin.compilerOptions.jvmTarget = JvmTarget.JVM_11

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.gson)
    implementation(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("conventionPlugin") {
            id = "com.habitrpg.buildlogic.convention"
            version = project.version
            implementationClass = "com.habitrpg.buildlogic.plugin.ConventionPlugin"
        }
        register("applicationPlugin") {
            id = "com.habitrpg.buildlogic.application"
            version = project.version
            implementationClass = "com.habitrpg.buildlogic.plugin.ApplicationPlugin"
        }
    }
}