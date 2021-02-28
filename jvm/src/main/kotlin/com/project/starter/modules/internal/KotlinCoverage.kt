package com.project.starter.modules.internal

import com.project.starter.config.getByType
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureKotlinCoverage() {
    pluginManager.apply("jacoco")

    extensions.configure(JacocoPluginExtension::class.java) {
        it.toolVersion = "0.8.6"
    }
    tasks.named("jacocoTestReport", JacocoReport::class.java) {
        it.dependsOn(":$path:test")
        it.reports.apply {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }
    tasks.named("test") { testTask ->
        testTask.extensions.getByType<JacocoTaskExtension>().apply {
            excludes = daggerCoverageExclusions
        }
    }
}

val daggerCoverageExclusions = listOf(
    "**/*_MembersInjector.class",
    "**/Dagger*Component.class",
    "**/Dagger*Component${"$"}Builder.class",
    "**/*_*Factory.class",
    "**/*_Factory.class"
)
