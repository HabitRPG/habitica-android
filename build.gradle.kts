// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("min_sdk", 21)
        set("target_sdk", 34)
        set("wearos_target_sdk", 33)
        set("app_version_name", "")
        set("app_version_code", 0)
        set("accompanist_version", "0.30.0")
        set("amplitude_version", "1.6.1")
        set("appcompat_version", "1.7.0")
        set("coil_version", "2.4.0")
        set("compose_version", "1.6.8")
        set("compose_compiler","1.5.14" )
        set("core_ktx_version", "1.13.1")
        set("coroutines_version", "1.8.0")
        set("daggerhilt_version", "2.51.1")
        set("firebase_bom", "31.3.0")
        set("kotest_version","5.6.2")
        set("kotlin_version", "2.0.20")
        set("ktlint_version", "1.2.1")
        set("lifecycle_version", "2.8.4")
        set("markwon_version", "4.6.2")
        set("mockk_version", "1.13.4")
        set("moshi_version", "1.15.0")
        set("navigation_version", "2.7.7")
        set("okhttp_version", "4.12.0")
        set("paging_version", "3.3.0")
        set("play_wearables_version", "18.2.0")
        set("play_auth_version", "21.2.0")
        set("preferences_version", "1.2.1")
        set("realm_version", "1.0.2")
        set("retrofit_version", "2.9.0")
        set("recyclerview_version", "1.3.2")
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
        mavenLocal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("com.neenbedankt.gradle.plugins:android-apt:1.8")
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.2")
        classpath("io.realm:realm-gradle-plugin:10.13.2-transformer-api")
        classpath("io.realm.kotlin:gradle-plugin:${rootProject.extra["realm_version"]}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlin_version"]}")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.19.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${rootProject.extra["navigation_version"]}")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${rootProject.extra["daggerhilt_version"]}")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:11.3.1")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${rootProject.extra["kotlin_version"]}")
    }
}

//Properties props = new Properties()
//def propFile = new File("version.properties")
//if (propFile.canRead()) {
//    props.load(new FileInputStream(propFile))
//
//    if (props != null && props.containsKey("NAME") && props.containsKey("CODE")) {
//        ext.app_version_name = props["NAME"]
//        ext.app_version_code = props["CODE"] as Integer
//    } else {
//        println "signing.properties found but some entries are missing"
//        android.buildTypes.release.signingConfig = null
//    }
//} else {
//    println "signing.properties not found"
//}

tasks.register("allUnitTests", GradleBuild::class) {
    tasks = listOf(":Habitica:testProdDebugUnitTest", ":wearos:testProdDebugUnitTest", ":common:testProdDebugUnitTest")
}
