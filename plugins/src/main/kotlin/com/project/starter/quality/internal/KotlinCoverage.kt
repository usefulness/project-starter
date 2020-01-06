package com.project.starter.quality.internal

import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureKotlinCoverage() {
    pluginManager.apply("jacoco")

    tasks.named("jacocoTestReport", JacocoReport::class.java) {
        it.dependsOn(":$path:test")
        it.reports.apply {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }
}
