package com.project.starter.modules.internal

import com.project.starter.config.getByType
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureMultiplatformCoverage() {
    pluginManager.apply("jacoco")

    tasks.withType(Test::class.java).configureEach {
        it.extensions.getByType<JacocoTaskExtension>().apply {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    extensions.configure(JacocoPluginExtension::class.java) {
        it.toolVersion = "0.8.10"
    }
    tasks.register("jacocoTestReport", JacocoReport::class.java) {
        it.dependsOn(":$path:jvmTest")
        it.classDirectories.setFrom(layout.buildDirectory.map { buildDir -> buildDir.file("classes/kotlin/jvm/main") })
        it.sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/jvmMain/kotlin"))
        it.executionData.setFrom(layout.buildDirectory.map { buildDir -> buildDir.file("jacoco/jvmTest.exec") })
        it.reports.apply {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
