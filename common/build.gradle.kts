import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.habitrpg.common.habitica"
    compileSdk = libs.versions.targetSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val hrpgProps = Properties().apply { load(FileInputStream(File(projectDir.absolutePath + "/../habitica.properties"))) }
        hrpgProps.forEach { key, value -> buildConfigField("String", key as String, "\"${value}\"") }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.useJUnitPlatform() }
        }
        animationsDisabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        register("debugIAP") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions.kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()

    flavorDimensions.add("buildType")

    productFlavors {
        register("dev") {
            dimension = "buildType"
        }

        register("staff") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"staff\"")
        }

        register("partners") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"partners\"")
        }

        register("alpha") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"alpha\"")
        }

        register("beta") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"beta\"")
        }

        register("prod") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"production\"")
        }
    }
}

ktlint {
    filter {
        exclude { entry -> entry.file.toString().contains("generated") }
    }
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

dependencies {
    implementation(projects.shared)

    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    implementation(libs.core)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)

    // Markdown
    implementation(libs.bundles.markwon)

    // Image Management Library
    implementation(libs.coil)
    implementation(libs.coil.gif)

    implementation(libs.navigation.common)
    implementation(libs.navigation.runtime)
    implementation(libs.recyclerview)
    implementation(libs.material)

    testImplementation(libs.bundles.test.implementation)
    androidTestImplementation(libs.bundles.android.test.implementation)

    implementation(libs.activity.compose)
    implementation(libs.runtime.livedata)
    implementation(libs.compose.animation)
    implementation(libs.text.google.fonts)
    implementation(libs.ui.tooling)
    implementation(libs.material3)
    implementation(libs.accompanist.theme)
}