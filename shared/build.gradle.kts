plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("io.kotest.multiplatform") version "5.6.2"
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

kotlin {
    android()
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra.get("coroutines_version")}")
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
    compileSdk = rootProject.extra.get("target_sdk") as Int
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
        }
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

    namespace = "com.habitrpg.shared.habitica"
}
