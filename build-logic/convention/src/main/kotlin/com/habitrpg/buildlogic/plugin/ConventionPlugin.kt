package com.habitrpg.buildlogic.plugin

import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.gradle.kotlin.dsl.KotlinClosure2
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.KtlintExtension

class ConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {

        with(pluginManager) {
            apply("io.gitlab.arturbosch.detekt")
            apply("org.jlleitschuh.gradle.ktlint")
        }

        configure<KtlintExtension> {
            filter {
                exclude { entry -> entry.file.toString().contains("generated") }
            }
        }

        tasks.withType<Detekt> {
            source = fileTree("$projectDir/src/main/java")
            config = files("${rootProject.rootDir}/detekt.yml")
            baseline = file("${rootProject.projectDir}/detekt_baseline.xml")
            reports {
                xml.required.set(false)
                html.required.set(true)
                html.outputLocation.set(layout.buildDirectory.file("reports/detekt.html"))
                txt.required.set(false)
                sarif.required.set(true)
                sarif.outputLocation.set(layout.buildDirectory.file("reports/detekt.sarif"))
            }
        }

        tasks.withType<Test> {
            outputs.upToDateWhen { false }
            testLogging {
                showStandardStreams = true
                events.addAll(listOf(PASSED, SKIPPED, FAILED, STANDARD_ERROR))
            }
            afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (desc.parent == null) { // will match the outermost suite
                    val output = buildString {
                        append("Results: ${result.resultType} ")
                        append("(${result.testCount} tests, ")
                        append("${result.successfulTestCount} passed, ")
                        append("${result.failedTestCount} failed, ")
                        append("${result.skippedTestCount} skipped)")
                    }
                    val startItem = "|  "
                    val endItem = "  |"
                    val repeatLength = startItem.length + output.length + endItem.length
                    println(buildString {
                        append("\n")
                        repeat(repeatLength) { append("—") }
                        append("\n")
                        append(startItem + output + endItem)
                        append("\n")
                        repeat(repeatLength) { append("—") }
                    })
                }
            }))
        }
    }
}