package com.project.starter.quality.internal

import com.android.build.gradle.api.BaseVariant
import com.project.starter.config.getByType
import com.project.starter.modules.internal.daggerCoverageExclusions
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureAndroidCoverage(variants: DomainObjectSet<out BaseVariant>, projectExclusions: List<String>) {
    pluginManager.apply("jacoco")

    variants.configureEach { variant ->
        tasks.register("jacoco${variant.name.capitalize()}TestReport", JacocoReport::class.java) { report ->
            val capitalizedVariant = variant.name.capitalize()
            val testTask = tasks.getByName("test${capitalizedVariant}UnitTest")
            val jacocoTestTaskExtension = testTask.extensions.getByType<JacocoTaskExtension>().apply {
                isIncludeNoLocationClasses = true
            }
            report.dependsOn(testTask)
            report.group = "verification"
            report.description = "Generates Jacoco coverage reports for the ${variant.name} variant."

            report.reports {
                it.html.isEnabled = true
                it.xml.isEnabled = true
            }

            val sourceDirs = variant.sourceSets.flatMap { it.javaDirectories }
            val classesDir = variant.javaCompileProvider.get().destinationDir
            val executionData = jacocoTestTaskExtension.destinationFile

            val coverageExcludes = excludes + projectExclusions
            val kotlinClassesDir = "$buildDir/tmp/kotlin-classes/${variant.name}"
            val kotlinTree = fileTree(mapOf("dir" to kotlinClassesDir, "excludes" to coverageExcludes))
            val javaTree = fileTree(mapOf("dir" to classesDir, "excludes" to coverageExcludes))

            report.classDirectories.setFrom(javaTree + kotlinTree)
            report.executionData.setFrom(files(executionData))
            report.sourceDirectories.setFrom(files(sourceDirs))
        }
    }
}

private val databinding = listOf(
    "android/databinding/**/*.class",
    "androidx/databinding/**/*.class",
    "**/databinding/*Binding.class",
    "**/databinding/*BindingImpl.class",
    "**/BR.*",
    "**/IssuesRegistry.*"
)
private val framework = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*"
)

private val excludes = databinding + framework + daggerCoverageExclusions
