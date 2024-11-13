import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.kotest.multiplatform") version "5.6.2"
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

ktlint {
    filter {
        exclude { entry -> entry.file.toString().contains("generated") }
    }
}

tasks.withType<Detekt>().configureEach {
    source = fileTree("Habitica/src/main/java")
    config = files("detekt.yml")
    baseline = file("${rootProject.projectDir}/detekt_baseline.xml")
    reports {
        xml.required.set(false)
        html.required.set(true)
        html.outputLocation.set(file("build/reports/detekt.html"))
        txt.required.set(false)
        sarif.required.set(true)
        sarif.outputLocation.set(file("build/reports/detekt.sarif"))
    }
}

tasks.withType<Test>().configureEach {
    outputs.upToDateWhen { false }
    testLogging.events.addAll(listOf(PASSED, SKIPPED, FAILED, STANDARD_ERROR))
}