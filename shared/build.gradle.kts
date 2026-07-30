import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id(libs.plugins.android.kotlin.multiplatform.library.get().pluginId)
    id("kotlin-parcelize")
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.habitrpg.convention.get().pluginId)
    id(libs.plugins.kotest.get().pluginId)
}

kotlin {
    android {
        namespace = "com.habitrpg.shared.habitica"
        compileSdk = libs.versions.targetSdk.get().toInt()
        minSdk = 21
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutine)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test")) // This brings all the platform dependencies automatically
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}
