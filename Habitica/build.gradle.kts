import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `jacoco-report-aggregation`
    id(
        libs.plugins.kotlin.android
            .get()
            .pluginId,
    )
    id(
        libs.plugins.android.application
            .get()
            .pluginId,
    )
    id(
        libs.plugins.hilt
            .get()
            .pluginId,
    )
    id(
        libs.plugins.navigation
            .get()
            .pluginId,
    )
    id(
        libs.plugins.ksp
            .get()
            .pluginId,
    )
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id(
        libs.plugins.kotlin.compose
            .get()
            .pluginId,
    )
    id(
        libs.plugins.realm
            .get()
            .pluginId,
    )
    id(
        libs.plugins.habitrpg.application
            .get()
            .pluginId,
    )
    id(
        libs.plugins.habitrpg.convention
            .get()
            .pluginId,
    )
    id(
        libs.plugins.crashlytics
            .get()
            .pluginId,
    )
    id(
        libs.plugins.firebase.perf
            .get()
            .pluginId,
    )
    id(
        libs.plugins.google.service
            .get()
            .pluginId,
    )
}

android {
    compileSdk =
        libs.versions.targetSdk
            .get()
            .toInt()
    namespace = "com.habitrpg.android.habitica"

    defaultConfig {
        applicationId = "com.habitrpg.android.habitica"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        vectorDrawables.useSupportLibrary = true

        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        @Suppress("UnstableApiUsage")
        androidResources.localeFilters.addAll(
            listOf(
                "en",
                "bg",
                "de",
                "en-rGB",
                "es",
                "fr",
                "hr-rHR",
                "hu",
                "in",
                "it",
                "iw",
                "ja",
                "ko",
                "lt",
                "nl",
                "pl",
                "pt-rBR",
                "pt-rPT",
                "ru",
                "tr",
                "uk",
                "zh",
                "zh-rTW",
            ),
        )

        buildConfigField("String", "STORE", "\"google\"")
        buildConfigField("String", "TESTING_LEVEL", "\"production\"")
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
        resValues = true
    }

    buildTypes {
        debug {
            // Keep it commented!
            // applicationIdSuffix ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            // Disable fabric build ID generation for debug builds
            ext["enableCrashlytics"] = false
            ext["alwaysUpdateBuildId"] = false
            enableUnitTestCoverage = false
            resValue("string", "content_provider", "com.habitrpg.android.habitica.debug.fileprovider")
            resValue("string", "app_name", "Habitica Debug")
        }
        create("debugIAP") {
            signingConfigs.asMap["release"]?.let { releaseSigning -> signingConfig = releaseSigning }
            isDebuggable = true
            isMinifyEnabled = false
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
            // Disable fabric build ID generation for debug builds
            ext["enableCrashlytics"] = false
            ext["alwaysUpdateBuildId"] = false
            resValue("string", "content_provider", "com.habitrpg.android.habitica.fileprovider")
            resValue("string", "app_name", "Habitica Debug")
        }
        release {
            signingConfigs.asMap["release"]?.let { releaseSigning -> signingConfig = releaseSigning }
            isDebuggable = false
            optimization {
                enable = true
            }
            resValue("string", "content_provider", "com.habitrpg.android.habitica.fileprovider")
            resValue("string", "app_name", "Habitica")
        }
    }

    productFlavors {
        named("prod") {
            buildConfigField("String", "STAGING_KEY", "\"\"")
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.directories.add("src/main/java")
            resources.directories.add("src/main/java")
            res.directories.add("res")
            assets.directories.add("assets")
        }
        getByName("test") { java.directories.add("src/test/java") }
        getByName("debugIAP") { java.directories.add("src/debug/java") }
        getByName("release") { java.directories.add("src/release/java") }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    hilt {
        enableAggregatingTask = true
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
        checkReleaseBuilds = false
        abortOnError = true
        disable.addAll(listOf("MissingTranslation"))
        enable.addAll(listOf("LogConditional", "IconExpectedSize", "MissingRegistered", "TypographyQuotes"))
    }

    bundle.language.enableSplit = false
    packaging.resources.excludes += "META-INF/*"
}

tasks.matching { it.name.startsWith("lint") }.configureEach { enabled = false }
tasks.withType<JavaCompile> { options.compilerArgs.addAll(listOf("-Xmaxerrs", "500")) }
tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(projects.common)
    implementation(projects.shared)

    implementation(fileTree("../common/libs") { include("*.jar") })

    // Networking
    implementation(libs.bundles.okhttp)

    // REST API handling
    implementation(libs.retrofit) {
        exclude(
            module =
                libs.okhttp
                    .asProvider()
                    .get()
                    .group,
        )
    }
    implementation(libs.retrofit.converter.gson)

    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler.androidx)
    compileOnly(libs.javax.annotation)

    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    implementation(libs.work.runtime)

    // App Compatibility and Material Design
    implementation(libs.bundles.design)

    // Desugaring
    coreLibraryDesugaring(libs.desugar)

    // IAP Handling / Verification
    implementation(libs.billing)

    implementation(libs.coil.compose)

    implementation(libs.shimmer)

    // Leak Detection
    debugImplementation(libs.leakcanary)

    // Google Services
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.google.services)
    implementation(libs.credentials)
    implementation(libs.credentials.playServicesAuth)
    implementation(libs.googleid)

    implementation(libs.flexbox)

    implementation(libs.core)
    implementation(libs.core.ktx)

    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.process)

    implementation(libs.navigation.fragment)
    implementation(libs.fragment.ktx)
    implementation(libs.paging)
    implementation(libs.paging.compose)
    implementation(libs.kotlinx.coroutine)
    implementation(libs.coroutine.android)
    implementation(libs.material3)

    implementation(libs.google.play.review)
    implementation(libs.google.play.review.ktx)

    implementation(libs.activity.compose)
    implementation(libs.runtime.livedata)
    implementation(libs.compose.animation)
    implementation(libs.text.google.fonts)
    implementation(libs.ui.tooling)
    implementation(libs.viewmodel.compose)

    implementation(libs.kotlin.stdlib)

    // Tests
    testImplementation(libs.bundles.test.implementation)
    androidTestImplementation(libs.bundles.android.test.implementation)
    androidTestImplementation(libs.kaspresso) { exclude(module = "protobuf-lite") }
    debugImplementation(libs.test.fragment)
    debugImplementation(libs.test.monitor)
    androidTestUtil(libs.test.orchestrator)
}
