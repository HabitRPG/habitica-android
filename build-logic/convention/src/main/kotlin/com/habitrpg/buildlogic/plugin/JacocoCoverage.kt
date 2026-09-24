package com.habitrpg.buildlogic.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.testing.jacoco.tasks.JacocoReport

private const val COVERAGE_PACKAGE_INCLUDE = "com/habitrpg/**"

private val COVERAGE_GENERATED_CODE_EXCLUDES =
    listOf(
        "**/databinding/**",
        "**/Dagger*.class",
        "**/Dagger*\$*.class",
        "**/Hilt_*.class",
        "**/Hilt_*\$*.class",
        "**/*_HiltComponents*.class",
        "**/*_HiltModules*.class",
        "**/*_Factory.class",
        "**/*_Factory\$*.class",
        "**/*_MembersInjector.class",
        "**/*_MembersInjector\$*.class",
        "**/*_GeneratedInjector.class",
        "**/*_ComponentTreeDeps.class",
        "**/*Directions.class",
        "**/*Directions\$*.class",
        "**/*Args.class",
        "**/*Args\$*.class",
    )

abstract class HabiticaJacocoReport : JacocoReport() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classesJars: ListProperty<RegularFile>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classesDirectories: ListProperty<Directory>
}

internal fun Project.registerJacocoUnitTestCoverageReports() {
    pluginManager.apply("jacoco")

    val androidComponents = extensions.findByType(AndroidComponentsExtension::class.java) ?: return
    androidComponents.onVariants(androidComponents.selector().withBuildType("debug")) { variant ->
        val variantName = variant.name
        val capitalizedVariantName = variantName.replaceFirstChar { it.uppercase() }

        val reportTask =
            tasks.register("jacoco${capitalizedVariantName}UnitTestReport", HabiticaJacocoReport::class.java) {
                group = "Reporting"
                description = "Generates a JaCoCo unit test coverage report for the $variantName variant, scoped to com.habitrpg code."
                dependsOn("test${capitalizedVariantName}UnitTest")

                sourceDirectories.setFrom(files("src/main/java"))
                executionData.setFrom(
                    fileTree(layout.buildDirectory.dir("outputs/unit_test_code_coverage/${variantName}UnitTest")) {
                        include("*.exec")
                    },
                )

                classDirectories.setFrom(
                    classesJars.map { jars ->
                        jars.map {
                            zipTree(it).matching {
                                include(COVERAGE_PACKAGE_INCLUDE)
                                exclude(COVERAGE_GENERATED_CODE_EXCLUDES)
                            }
                        }
                    },
                    classesDirectories.map { dirs ->
                        dirs.map {
                            fileTree(it) {
                                include(COVERAGE_PACKAGE_INCLUDE)
                                exclude(COVERAGE_GENERATED_CODE_EXCLUDES)
                            }
                        }
                    },
                )

                reports {
                    html.required.set(true)
                    xml.required.set(true)
                }
            }

        variant.artifacts
            .forScope(ScopedArtifacts.Scope.PROJECT)
            .use(reportTask)
            .toGet(ScopedArtifact.CLASSES, HabiticaJacocoReport::classesJars, HabiticaJacocoReport::classesDirectories)
    }
}
