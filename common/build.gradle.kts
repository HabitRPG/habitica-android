import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import java.io.FileInputStream
import java.util.Properties


plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

val rootExtra = rootProject.extra

android {
    namespace = "com.habitrpg.common.habitica"
    compileSdk = rootExtra.get("target_sdk") as Int

    defaultConfig {
        minSdk = rootExtra.get("min_sdk") as Int
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

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra.get("compose_compiler") as String
    }

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

val core_ktx_version: String by rootExtra
val accompanist_version: String by rootExtra
val appcompat_version: String by rootExtra
val compose_version: String by rootExtra
val markwon_version: String by rootExtra
val coil_version: String by rootExtra
val mockk_version: String by rootExtra
val kotest_version: String by rootExtra
val kotlin_version: String by rootExtra
val navigation_version: String by rootExtra

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    implementation("androidx.core:core-ktx:$core_ktx_version")
    implementation("androidx.appcompat:appcompat:$appcompat_version")

    // Markdown
    implementation("io.noties.markwon:core:$markwon_version")
    implementation("io.noties.markwon:ext-strikethrough:$markwon_version")
    implementation("io.noties.markwon:image:$markwon_version")
    implementation("io.noties.markwon:recycler:$markwon_version")
    implementation("io.noties.markwon:linkify:$markwon_version")

    // Image Management Library
    implementation("io.coil-kt:coil:$coil_version")
    implementation("io.coil-kt:coil-gif:$coil_version")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.navigation:navigation-common-ktx:$navigation_version")
    implementation("androidx.navigation:navigation-runtime-ktx:$navigation_version")
    implementation("com.google.android.material:material:1.12.0")

    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("io.mockk:mockk-android:$mockk_version")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
    testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
    testImplementation("io.kotest:kotest-framework-datatest:$kotest_version")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.compose.runtime:runtime-livedata:$compose_version")
    implementation("androidx.compose.animation:animation:$compose_version")
    implementation("androidx.compose.ui:ui-text-google-fonts:$compose_version")
    implementation("androidx.compose.ui:ui-tooling:$compose_version")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("com.google.accompanist:accompanist-themeadapter-material3:$accompanist_version")

    implementation(project(":shared"))
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}