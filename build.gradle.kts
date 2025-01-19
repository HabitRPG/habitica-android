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
    alias(libs.plugins.habitrpg.application) apply false
    alias(libs.plugins.habitrpg.convention) apply false
}

tasks.register("allUnitTests", DefaultTask::class) {
    dependsOn(":Habitica:testProdDebugUnitTest", ":wearos:testProdDebugUnitTest", ":common:testProdDebugUnitTest")
}