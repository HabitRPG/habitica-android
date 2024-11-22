import com.android.build.gradle.internal.lint.AndroidLintTask

plugins {
    `jacoco-report-aggregation`
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.realm)
    alias(libs.plugins.habitrpg.application)
    alias(libs.plugins.habitrpg.convention)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.google.service)
}

android {
    compileSdk = libs.versions.targetSdk.get().toInt()
    namespace = "com.habitrpg.android.habitica"

    defaultConfig {
        applicationId = "com.habitrpg.android.habitica"
        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.targetSdk.get().toInt()
        vectorDrawables.useSupportLibrary = true

        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        resourceConfigurations.addAll(
            listOf("en", "bg", "de", "en-rGB", "es", "fr", "hr-rHR", "in", "it", "iw", "ja", "ko", "lt", "nl", "pl", "pt-rBR", "pt-rPT", "ru", "tr", "uk", "zh", "zh-rTW")
        )

        buildConfigField("String", "STORE", "\"google\"")
        buildConfigField("String", "TESTING_LEVEL", "\"production\"")
    }

    buildFeatures {
        viewBinding = true
        compose = true
        renderScript = true
        buildConfig = true
        aidl = true
    }

    buildTypes {
        debug {
            // Keep it commented!
            //applicationIdSuffix ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            // Disable fabric build ID generation for debug builds
            ext["enableCrashlytics"] = false
            ext["alwaysUpdateBuildId"] = false
            enableUnitTestCoverage = false
            resValue("string", "content_provider", "com.habitrpg.android.habitica.debug.fileprovider")
            resValue("string", "content_provider", "com.habitrpg.android.habitica.fileprovider")
            resValue("string", "app_name", "Habitica Debug")
        }
        create("debugIAP") {
            signingConfigs.asMap["release"]?.let { releaseSigning -> signingConfig = releaseSigning }
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            // Disable fabric build ID generation for debug builds
            ext["enableCrashlytics"] = false
            ext["alwaysUpdateBuildId"] = false
            resValue("string", "content_provider", "com.habitrpg.android.habitica.fileprovider")
            resValue("string", "app_name", "Habitica Debug")
        }
        release {
            signingConfigs.asMap["release"]?.let { releaseSigning -> signingConfig = releaseSigning }
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            resValue("string", "content_provider", "com.habitrpg.android.habitica.fileprovider")
            resValue("string", "app_name", "Habitica")
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src/main/java")
            resources.srcDirs("src/main/java")
            aidl.srcDirs("src/main/java")
            renderscript.srcDirs("src/main/java")
            res.srcDirs("res")
            assets.srcDirs("assets")
        }
        getByName("test") { java.srcDir("src/test/java") }
        getByName("debugIAP") { java.srcDirs("src/debug/java") }
        getByName("release") { java.srcDirs("src/release/java") }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { it.useJUnitPlatform() }
        }
        animationsDisabled = true
    }

    lint {
        abortOnError = false
        disable.addAll(listOf("MissingTranslation", "InvalidPackage"))
        enable.addAll(listOf("LogConditional", "IconExpectedSize", "MissingRegistered", "TypographyQuotes"))
    }

    bundle.language.enableSplit = false
    packaging.resources.excludes.add("META-INF/*")
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

tasks.withType<AndroidLintTask> { enabled = false }
tasks.withType<JavaCompile> { options.compilerArgs.addAll(listOf("-Xmaxerrs", "500")) }

dependencies {
    implementation(projects.common)
    implementation(projects.shared)

    implementation(fileTree("../common/libs") { include("*.jar") })

    //Networking
    implementation(libs.bundles.okhttp)

    //REST API handling
    implementation(libs.retrofit) { exclude(module = libs.okhttp.asProvider().get().group) }
    implementation(libs.retrofit.converter.gson)

    //Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    compileOnly(libs.javax.annotation)

    //App Compatibility and Material Design
    implementation(libs.bundles.design)

    //Desugaring
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.device.names)

    // IAP Handling / Verification
    implementation(libs.billing)
    implementation(libs.viewPagerIndicator) { exclude(group = "com.google.android") }

    implementation(libs.coil.compose)

    //Analytics
    implementation(libs.amplitude.analytic)

    //Tests
    testImplementation(libs.bundles.test.implementation)

    androidTestImplementation(libs.bundles.android.test.implementation)
    androidTestImplementation(libs.kaspresso) { exclude(module = "protobuf-lite") }

    debugImplementation(libs.test.fragment)
    debugImplementation(libs.test.monitor)

    androidTestUtil(libs.test.orchestrator)

    implementation(libs.shimmer)

    //Leak Detection
    debugImplementation(libs.leakcanary)

    // Google Services
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.google.services)

    implementation(libs.flexbox)

    implementation(libs.core)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.common)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.fragment.ktx)
    implementation(libs.paging)
    implementation(libs.paging.compose)
    implementation(libs.kotlinx.coroutine)
    implementation(libs.coroutine.android)
    implementation(libs.material3)
    implementation(libs.accompanist.sysmtemUi)

    implementation(libs.google.play.review)
    implementation(libs.google.play.review.ktx)

    implementation(libs.activity.compose)
    implementation(libs.runtime.livedata)
    implementation(libs.compose.animation)
    implementation(libs.text.google.fonts)
    implementation(libs.ui.tooling)
    implementation(libs.viewmodel.compose)

    implementation(libs.kotlin.jdk7)

    implementation(libs.toolargetool)
}