import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
    id("jacoco-report-aggregation")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose")
    id("realm-android")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.gms.google-services")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    compileSdk = rootProject.extra["target_sdk"].toString().toInt()
    namespace = "com.habitrpg.android.habitica"

    defaultConfig {
        applicationId = "com.habitrpg.android.habitica"
        minSdk = rootProject.extra["min_sdk"].toString().toInt()
        compileSdk = rootProject.extra["target_sdk"].toString().toInt()
        vectorDrawables.useSupportLibrary = true
        versionCode = rootProject.extra["app_version_code"].toString().toInt()
        versionName = rootProject.extra["app_version_name"].toString()
        targetSdk = rootProject.extra["target_sdk"].toString().toInt()
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
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 8
        }

        register("partners") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"partners\"")
            resValue("string", "app_name", "Habitica")
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 6
        }

        register("alpha") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"alpha\"")
            resValue("string", "app_name", "Habitica Alpha")
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 4
        }

        register("beta") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"beta\"")
            versionCode = rootProject.extra["app_version_code"].toString().toInt() + 2
        }

        register("prod") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"production\"")
            versionCode = rootProject.extra["app_version_code"].toString().toInt()
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
    implementation(fileTree("../common/libs") { include("*.jar") })

    //Networking
    implementation("com.squareup.okhttp3:okhttp:${rootProject.extra["okhttp_version"]}")
    implementation("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okhttp_version"]}")
    //REST API handling
    implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofit_version"]}") {
//        exclude module : "okhttp"
    }
    implementation("com.squareup.retrofit2:converter-gson:${rootProject.extra["retrofit_version"]}")

    //Dependency Injection
    implementation("com.google.dagger:hilt-android:${rootProject.extra["daggerhilt_version"]}")
    kapt("com.google.dagger:hilt-compiler:${rootProject.extra["daggerhilt_version"]}")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    //App Compatibility and Material Design
    implementation("androidx.appcompat:appcompat:${rootProject.extra["appcompat_version"]}")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:${rootProject.extra["recyclerview_version"]}")
    implementation("androidx.preference:preference-ktx:${rootProject.extra["preferences_version"]}")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("com.jaredrummler:android-device-names:2.1.1")

    // IAP Handling / Verification
    implementation("com.android.billingclient:billing-ktx:7.0.0")
    implementation("fr.avianey.com.viewpagerindicator:library:2.4.1@aar")

    implementation("io.coil-kt:coil-compose:${rootProject.extra["coil_version"]}")

    //Analytics
    implementation("com.amplitude:analytics-android:${rootProject.extra["amplitude_version"]}")

    //Tests
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("io.mockk:mockk:${rootProject.extra["mockk_version"]}")
    testImplementation("io.mockk:mockk-android:${rootProject.extra["mockk_version"]}")
    testImplementation("io.kotest:kotest-runner-junit5:${rootProject.extra["kotest_version"]}")
    testImplementation("io.kotest:kotest-assertions-core:${rootProject.extra["kotest_version"]}")
    testImplementation("io.kotest:kotest-framework-datatest:${rootProject.extra["kotest_version"]}")
    androidTestImplementation("com.kaspersky.android-components:kaspresso:1.5.1") {
//        exclude module : "protobuf-lite"
    }
    androidTestImplementation("com.kaspersky.android-components:kaspresso-compose-support:1.5.1")

    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    debugImplementation("androidx.fragment:fragment-testing:1.8.2")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    debugImplementation("androidx.test:monitor:1.7.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestImplementation("io.mockk:mockk-android:${rootProject.extra["mockk_version"]}")
    androidTestImplementation("io.mockk:mockk-agent:${rootProject.extra["mockk_version"]}")
    androidTestImplementation("io.kotest:kotest-assertions-core:${rootProject.extra["kotest_version"]}")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:${rootProject.extra["kotlin_version"]}")

    androidTestUtil("androidx.test:orchestrator:1.5.0")

    implementation("com.facebook.shimmer:shimmer:0.5.0")

    //Leak Detection
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")
    //Push Notifications
    implementation(platform("com.google.firebase:firebase-bom:${rootProject.extra["firebase_bom"]}"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.android.gms:play-services-auth:${rootProject.extra["play_auth_version"]}")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.android.gms:play-services-wearable:${rootProject.extra["play_wearables_version"]}")

    implementation("androidx.core:core-ktx:${rootProject.extra["core_ktx_version"]}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${rootProject.extra["lifecycle_version"]}")
    implementation("androidx.navigation:navigation-fragment-ktx:${rootProject.extra["navigation_version"]}")
    implementation("androidx.navigation:navigation-ui-ktx:${rootProject.extra["navigation_version"]}")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.paging:paging-runtime-ktx:${rootProject.extra["paging_version"]}")
    implementation("androidx.paging:paging-compose:${rootProject.extra["paging_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra["coroutines_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutines_version"]}")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${rootProject.extra["accompanist_version"]}")

    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.compose.runtime:runtime-livedata:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.animation:animation:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-text-google-fonts:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${rootProject.extra["lifecycle_version"]}")

    implementation(project(":common"))
    implementation(project(":shared"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${rootProject.extra["kotlin_version"]}")

    implementation("com.gu.android:toolargetool:0.3.0")
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