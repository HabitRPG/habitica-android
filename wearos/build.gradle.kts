plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.habitrpg.convention)
    alias(libs.plugins.habitrpg.application)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.service)
}

android {
    namespace = "com.habitrpg.android.habitica"
    compileSdk = libs.versions.targetSdk.get().toInt()

    defaultConfig {
        applicationId = "com.habitrpg.android.habitica"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.wearOsTargetSdk.get().toInt()
        compileSdk = libs.versions.targetSdk.get().toInt()

        buildConfigField("String", "TESTING_LEVEL", "\"production\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            ext["enableCrashlytics"] = false
            ext["alwaysUpdateBuildId"] = false
            resValue("string", "app_name", "Habitica Debug")
        }
        release {
            signingConfigs.asMap["release"]?.let { releaseSigning -> signingConfig = releaseSigning }
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "app_name", "Habitica")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.useJUnitPlatform() }
        }
        animationsDisabled = true
    }

    bundle.language.enableSplit = false
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

dependencies {
    implementation(projects.shared)
    implementation(projects.common)

    implementation(fileTree("../common/libs") { include("*.jar") })

    implementation(libs.core)
    implementation(libs.core.ktx)
    implementation(libs.google.play.wearable)
    implementation(libs.recyclerview)
    implementation(libs.wear)
    implementation(libs.wear.input)

    //Networking
    implementation(libs.bundles.okhttp)

    //REST API handling
    implementation(libs.retrofit) { exclude(module = libs.okhttp.asProvider().get().name) }
    implementation(libs.retrofit2.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.coordinatorlayout)
    implementation(libs.constraintlayout)
    ksp(libs.moshi.kotlin.codegen)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.google.services)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.common)
    implementation(libs.kotlinx.coroutine)
    implementation(libs.coroutine.android)
    implementation(libs.preference)
    implementation(libs.navigation.fragment)

    implementation(libs.google.play.auth)

    implementation(libs.appcompat)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlin.jdk7)
    implementation(libs.kotlin.reflect)

    implementation(libs.core.splashscreen) { exclude(module = libs.core.ktx.get().name) }

    testImplementation(libs.bundles.test.implementation)
    testImplementation(libs.mockk.android)
    testImplementation(libs.turbine)
}