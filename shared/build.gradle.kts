import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.android.library)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.habitrpg.convention)
    alias(libs.plugins.kotest)
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser()
        nodejs()
    }

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

android {
    compileSdk = libs.versions.targetSdk.get().toInt()
    namespace = "com.habitrpg.shared.habitica"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig.minSdk = 21

    buildTypes {
        create("debugIAP") {
            initWith(buildTypes["debug"])
            isMinifyEnabled = false
            isJniDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}