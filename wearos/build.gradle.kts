import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.service)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.habitrpg.android.habitica"
    compileSdk = libs.versions.targetSdk.get().toInt()

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.useJUnitPlatform() }
        }
        animationsDisabled = true
    }

    defaultConfig {
        applicationId = "com.habitrpg.android.habitica"
        minSdk = 26
        targetSdk = libs.versions.wearOsTargetSdk.get().toInt()
        compileSdk = libs.versions.targetSdk.get().toInt()

        // change this
        versionCode = 1
        versionName = "1"

        val hrpgProps = Properties().apply { load(FileInputStream(File(projectDir.absolutePath + "/../habitica.properties"))) }
        hrpgProps.forEach { key, value -> buildConfigField("String", key as String, "\"${value}\"") }
        buildConfigField("String", "TESTING_LEVEL", "\"production\"")
    }

//    signingConfigs {
//        release
//    }

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
//            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            resValue("string", "app_name", "Habitica")
        }
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

    flavorDimensions.add("buildType")

    productFlavors {
        register("dev") {
            dimension = "buildType"
        }

        register("staff") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"staff\"")
            resValue("string", "app_name", "Habitica Staff")
            versionCode = 0 + 9
        }

        register("partners") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"partners\"")
            resValue("string", "app_name", "Habitica")
            versionCode = 0 + 7
        }

        register("alpha") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"alpha\"")
            resValue("string", "app_name", "Habitica Alpha")
            versionCode = 0 + 5
        }

        register("beta") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"beta\"")
            versionCode = 0 + 3
        }

        register("prod") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"production\"")
            versionCode = 0 + 1
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

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

tasks.withType<Test> {
    outputs.upToDateWhen { false }
    testLogging.events.addAll(listOf(PASSED, SKIPPED, FAILED, STANDARD_ERROR))
}

dependencies {
    implementation(project(":common"))
    implementation(project(":shared"))

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
    implementation(libs.retrofit) { exclude(module = libs.okhttp.asProvider().get().group) }
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
//val props = Properties()
//val propFile = File("signingrelease.properties")
//if (propFile.canRead()) {
//    props.load(FileInputStream(propFile))
//
//    if (props != null && props.containsKey("STORE_FILE") && props.containsKey("STORE_PASSWORD") &&
//        props.containsKey("KEY_ALIAS") && props.containsKey("KEY_PASSWORD")
//    ) {
//        android.signingConfigs.release.storeFile = file(props["STORE_FILE"])
//        android.signingConfigs.release.storePassword = props["STORE_PASSWORD"]
//        android.signingConfigs.release.keyAlias = props["KEY_ALIAS"]
//        android.signingConfigs.release.keyPassword = props["KEY_PASSWORD"]
//    } else {
//        println("signing.properties found but some entries are missing")
//        android.buildTypes.release.signingConfig = null
//    }
//} else {
//    println("signing.properties not found")
//    android.buildTypes.release.signingConfig = null
//}
