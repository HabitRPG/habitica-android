import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val rootExtra = rootProject.extra

android {
    compileSdk = rootExtra.get("target_sdk") as Int

    defaultConfig {
        minSdk = rootExtra.get("min_sdk") as Int
        targetSdkVersion(rootExtra.get("target_sdk") as Int)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        register("debugIAP") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    namespace = "com.habitrpg.common.habitica"

    flavorDimensions.add("buildType")

    productFlavors {
        register("dev") {
            dimension = "buildType"
        }

        register("staff") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"staff\"")
        }

        register("partners") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"partners\"")
        }

        register("alpha") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"alpha\"")
        }

        register("beta") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"beta\"")
        }

        register("prod") {
            dimension = "buildType"
            buildConfigField("String", "TESTING_LEVEL", "\"production\"")
        }
    }
}

val core_ktx_version = rootExtra.get("core_ktx_version")
val appcompat_version = rootExtra.get("appcompat_version")
val markwon_version = rootExtra.get("markwon_version")
val coil_version = rootExtra.get("coil_version")
val mockk_version = rootExtra.get("mockk_version")
val kotest_version = rootExtra.get("kotest_version")

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    implementation("androidx.core:core-ktx:$core_ktx_version")
    implementation("androidx.appcompat:appcompat:$appcompat_version")

    // Markdown
    implementation("io.noties.markwon:core:$markwon_version")
    implementation("io.noties.markwon:ext-strikethrough:$markwon_version")
    implementation("io.noties.markwon:image:$markwon_version")
    implementation("io.noties.markwon:recycler:$markwon_version")
    implementation("io.noties.markwon:linkify:$markwon_version")

    // Image Management Library
    implementation("io.coil-kt:coil:$coil_version")
    implementation("io.coil-kt:coil-gif:$coil_version")

    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("io.mockk:mockk-android:$mockk_version")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
    testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
    testImplementation("io.kotest:kotest-framework-datatest:$kotest_version")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(project(":shared"))
}

android.testOptions {
    unitTests.all {
        it.useJUnitPlatform()
    }
}

// Add Habitica Properties to buildConfigField
val HRPG_PROPS_FILE = File(projectDir.absolutePath + "/../habitica.properties")
if (HRPG_PROPS_FILE.canRead()) {
    val HRPG_PROPS = Properties()
    HRPG_PROPS.load(FileInputStream(HRPG_PROPS_FILE))

    if (HRPG_PROPS != null) {
        android.buildTypes.configureEach {
            HRPG_PROPS.forEach { property ->
                buildConfigField("String", property.key as String, "\"${property.value}\"")
            }
        }
    } else {
        throw InvalidUserDataException("habitica.properties found but some entries are missing")
    }
} else {
    throw MissingResourceException("habitica.properties not found")
}
