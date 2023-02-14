plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("io.kotest.multiplatform") version "5.5.5"
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

kotlin {
    android()
    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test")) // This brings all the platform dependencies automatically
            }
        }
        val androidMain by getting
        val androidUnitTest by getting
        val iosMain by getting
        val iosTest by getting
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    buildTypes {
        release {
        }
    }

    namespace = "com.habitrpg.shared.habitica"
}
