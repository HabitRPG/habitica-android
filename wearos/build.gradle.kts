import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.habitrpg.android.habitica"
    compileSdk = rootProject.extra["target_sdk"].toString().toInt()

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
        targetSdk = rootProject.extra["wearos_target_sdk"].toString().toInt()
        compileSdk = rootProject.extra["target_sdk"].toString().toInt()
        versionCode = rootProject.extra["app_version_code"].toString().toInt() + 1
        versionName = "${rootProject.extra["app_version_name"]}w"

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
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 9
        }

        register("partners") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"partners\"")
            resValue("string", "app_name", "Habitica")
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 7
        }

        register("alpha") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"alpha\"")
            resValue("string", "app_name", "Habitica Alpha")
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 5
        }

        register("beta") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"beta\"")
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 3
        }

        register("prod") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"production\"")
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 1
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
    implementation(fileTree("../common/libs") { include("*.jar") })

    implementation("androidx.core:core-ktx:${rootProject.extra["core_ktx_version"]}")
    implementation("com.google.android.gms:play-services-wearable:${rootProject.extra["play_wearables_version"]}")
    implementation("androidx.recyclerview:recyclerview:${rootProject.extra["recyclerview_version"]}")
    implementation("androidx.wear:wear:1.3.0")
    implementation("androidx.wear:wear-input:1.1.0")

    //Networking
    implementation("com.squareup.okhttp3:okhttp:${rootProject.extra["okhttp_version"]}")
    implementation("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okhttp_version"]}")

    //REST API handling
    implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofit_version"]}") {
//        exclude module : "okhttp"
    }
    implementation("com.squareup.retrofit2:converter-moshi:${rootProject.extra["retrofit_version"]}")
    implementation("com.squareup.moshi:moshi-kotlin:${rootProject.extra["moshi_version"]}")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:${rootProject.extra["moshi_version"]}")

    implementation(platform("com.google.firebase:firebase-bom:${rootProject.extra["firebase_bom"]}"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${rootProject.extra["lifecycle_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra["coroutines_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutines_version"]}")
    implementation("androidx.preference:preference-ktx:${rootProject.extra["preferences_version"]}")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")

    implementation("com.google.android.gms:play-services-auth:${rootProject.extra["play_auth_version"]}")

    implementation(project(":common"))
    implementation(project(":shared"))
    implementation("androidx.appcompat:appcompat:${rootProject.extra["appcompat_version"]}")

    implementation("com.google.dagger:hilt-android:${rootProject.extra["daggerhilt_version"]}")
    kapt("com.google.dagger:hilt-compiler:${rootProject.extra["daggerhilt_version"]}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${rootProject.extra["kotlin_version"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${rootProject.extra["kotlin_version"]}")

    implementation("androidx.core:core-splashscreen:1.1.0-rc01")

    testImplementation("io.mockk:mockk:${rootProject.extra["mockk_version"]}")
    testImplementation("io.mockk:mockk-android:${rootProject.extra["mockk_version"]}")
    testImplementation("io.kotest:kotest-runner-junit5:${rootProject.extra["kotest_version"]}")
    testImplementation("io.kotest:kotest-assertions-core:${rootProject.extra["kotest_version"]}")
    testImplementation("io.kotest:kotest-framework-datatest:${rootProject.extra["kotest_version"]}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutines_version"]}")
    testImplementation("app.cash.turbine:turbine:0.12.1")
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
