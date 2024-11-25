import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.habitrpg.convention)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.useJUnitPlatform() }
        }
        animationsDisabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

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

    kotlin.jvmToolchain(11)
    composeOptions.kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    flavorDimensions.add("buildType")
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