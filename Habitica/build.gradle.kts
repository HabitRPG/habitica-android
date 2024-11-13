import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import java.io.FileInputStream
import java.util.Properties

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
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.google.service)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    compileSdk = libs.versions.targetSdk.get().toInt()
    namespace = "com.habitrpg.android.habitica"

    defaultConfig {
        applicationId = "com.habitrpg.android.habitica"
        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.targetSdk.get().toInt()
        vectorDrawables.useSupportLibrary = true

        // change this
        versionCode = 1
        versionName = "1"

        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        resourceConfigurations.addAll(
            listOf("en", "bg", "de", "en-rGB", "es", "fr", "hr-rHR", "in", "it", "iw", "ja", "ko", "lt", "nl", "pl", "pt-rBR", "pt-rPT", "ru", "tr", "uk", "zh", "zh-rTW")
        )

        buildConfigField("String", "STORE", "\"google\"")
        buildConfigField("String", "TESTING_LEVEL", "\"production\"")
        val habiticaRes = Properties().apply { load(FileInputStream(File(projectDir.absolutePath + "/../habitica.resources"))) }
        habiticaRes.forEach { key, value -> resValue("string", key.toString(), "\"${value}\"") }

        val hrpgProps = Properties().apply { load(FileInputStream(File(projectDir.absolutePath + "/../habitica.properties"))) }
        hrpgProps.forEach { key, value -> buildConfigField("String", key as String, "\"${value}\"") }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        renderScript = true
        buildConfig = true
        aidl = true
    }

//    signingConfigs {
//        release
//    }
    flavorDimensions.add("buildType")

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
//            signingConfig signingConfigs . release
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
//            signingConfig signingConfigs . release
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            resValue("string", "content_provider", "com.habitrpg.android.habitica.fileprovider")
            resValue("string", "app_name", "Habitica")
        }
    }

    productFlavors {
        register("dev") {
            dimension = "buildType"
        }

        register("staff") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"staff\"")
            resValue("string", "app_name", "Habitica Staff")
            versionCode = 0 + 8
        }

        register("partners") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"partners\"")
            resValue("string", "app_name", "Habitica")
            versionCode = 0 + 6
        }

        register("alpha") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"alpha\"")
            resValue("string", "app_name", "Habitica Alpha")
            versionCode = 0 + 4
        }

        register("beta") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"beta\"")
            versionCode = 0 + 2
        }

        register("prod") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"production\"")
            versionCode = 0
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
        getByName("test") {
            java.srcDir("src/test/java")
        }
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

    bundle {
        language {
            // Specifies that the app bundle should not support
            // configuration APKs for language resources. These
            // resources are instead packaged with each base and
            // dynamic feature APK.
            enableSplit = false
        }
    }
    lint {
        abortOnError = false
        disable.addAll(listOf("MissingTranslation", "InvalidPackage"))
        enable.addAll(listOf("LogConditional", "IconExpectedSize", "MissingRegistered", "TypographyQuotes"))
    }

    packaging.resources.excludes.add("META-INF/*")
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
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
tasks.named("lint") { enabled = false }
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "500"))
}
tasks.withType<Test>().configureEach {
    outputs.upToDateWhen { false }
    testLogging.events.addAll(listOf(PASSED, SKIPPED, FAILED, STANDARD_ERROR))
}

dependencies {
    implementation(project(":common"))
    implementation(project(":shared"))

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

//Properties props = new Properties()
//def propFile = new File("signingrelease.properties")
//if (propFile.canRead()) {
//    props.load(new FileInputStream (propFile))
//
//    if (props != null && props.containsKey("STORE_FILE") && props.containsKey("STORE_PASSWORD") &&
//        props.containsKey("KEY_ALIAS") && props.containsKey("KEY_PASSWORD")
//    ) {
//        android.signingConfigs.release.storeFile = file(props["STORE_FILE"])
//        android.signingConfigs.release.storePassword = props["STORE_PASSWORD"]
//        android.signingConfigs.release.keyAlias = props["KEY_ALIAS"]
//        android.signingConfigs.release.keyPassword = props["KEY_PASSWORD"]
//    } else {
//        println "signing.properties found but some entries are missing"
//        android.buildTypes.release.signingConfig = null
//    }
//} else {
//    println "signing.properties not found"
//    android.buildTypes.release.signingConfig = null
//}