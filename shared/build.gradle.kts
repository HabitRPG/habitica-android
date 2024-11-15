import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.android.library)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
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

ktlint {
    filter {
        exclude { entry -> entry.file.toString().contains("generated") }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

tasks.withType<Detekt> {
    source = fileTree("$projectDir/src/main/java")
    config = files("${rootProject.rootDir}/detekt.yml")
    baseline = file("${rootProject.projectDir}/detekt_baseline.xml")
    reports {
        xml.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.file("reports/detekt.html"))
        txt.required.set(false)
        sarif.required.set(true)
        sarif.outputLocation.set(layout.buildDirectory.file("reports/detekt.sarif"))
    }
}
tasks.withType<Test> {
    outputs.upToDateWhen { false }
    testLogging {
        showStandardStreams = true
        events.addAll(listOf(PASSED, SKIPPED, FAILED, STANDARD_ERROR))
    }
    afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
        if (desc.parent == null) { // will match the outermost suite
            val output = buildString {
                append("Results: ${result.resultType} ")
                append("(${result.testCount} tests, ")
                append("${result.successfulTestCount} passed, ")
                append("${result.failedTestCount} failed, ")
                append("${result.skippedTestCount} skipped)")
            }
            val startItem = "|  "
            val endItem = "  |"
            val repeatLength = startItem.length + output.length + endItem.length
            println(buildString {
                append("\n")
                repeat(repeatLength) { append("—") }
                append("\n")
                append(startItem + output + endItem)
                append("\n")
                repeat(repeatLength) { append("—") }
            })
        }
    }))
}