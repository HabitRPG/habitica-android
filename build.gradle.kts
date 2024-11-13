// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.google.service) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.navigation) apply false
    alias(libs.plugins.realm) apply false
}

tasks.register("allUnitTests", GradleBuild::class) {
    tasks = listOf(":Habitica:testProdDebugUnitTest", ":wearos:testProdDebugUnitTest", ":common:testProdDebugUnitTest")
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