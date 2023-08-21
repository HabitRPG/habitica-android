import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val rootExtra = rootProject.extra

android {
    compileSdk = rootExtra.get("target_sdk") as Int

    defaultConfig {
        minSdk = rootExtra.get("min_sdk") as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        unitTests {
        }
        animationsDisabled = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
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

val core_ktx_version: String by rootExtra
val appcompat_version: String by rootExtra
val markwon_version: String by rootExtra
val coil_version: String by rootExtra
val mockk_version: String by rootExtra
val kotest_version: String by rootExtra
val kotlin_version: String by rootExtra

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
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.navigation:navigation-common-ktx:2.7.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.0")

    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("io.mockk:mockk-android:$mockk_version")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
    testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
    testImplementation("io.kotest:kotest-framework-datatest:$kotest_version")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    implementation(project(":shared"))
}

android.testOptions {
    unitTests.all {
        it.useJUnitPlatform()
    }
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

// Add Habitica Properties to buildConfigField
val HRPG_PROPS_FILE = File(projectDir.absolutePath + "/../habitica.properties")
if (HRPG_PROPS_FILE.canRead()) {
    val hrpgProps = Properties()
    hrpgProps.load(FileInputStream(HRPG_PROPS_FILE))

    android.buildTypes.configureEach {
        hrpgProps.forEach { property ->
            buildConfigField("String", property.key as String, "\"${property.value}\"")
        }
    }
} else {
    throw MissingResourceException("habitica.properties not found")
}
