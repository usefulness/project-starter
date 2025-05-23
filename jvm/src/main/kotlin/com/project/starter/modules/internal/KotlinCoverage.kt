package com.project.starter.modules.internal

import com.project.starter.config.getByType
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureKotlinCoverage() {
    pluginManager.apply("jacoco")

    tasks.withType(Test::class.java).configureEach {
        extensions.getByType<JacocoTaskExtension>().apply {
            isIncludeNoLocationClasses = true
            excludes = daggerCoverageExclusions + "jdk.internal.*"
        }
    }
    extensions.configure(JacocoPluginExtension::class.java) {
        toolVersion = "0.8.13"
    }
    tasks.named("jacocoTestReport", JacocoReport::class.java) {
        dependsOn("test")
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

val daggerCoverageExclusions = listOf(
    "**/*_MembersInjector.class",
    "**/Dagger*Component.class",
    "**/Dagger*Component${"$"}Builder.class",
    "**/*_*Factory.class",
    "**/*_Factory.class",
)
