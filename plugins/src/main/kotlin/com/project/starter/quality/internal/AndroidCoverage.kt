package com.project.starter.quality.internal

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureAndroidCoverage(variants: DomainObjectSet<out BaseVariant>, projectExclusions: List<String>) {
    pluginManager.apply("jacoco")
    variants.all { variant ->
        tasks.register("jacoco${variant.name.capitalize()}TestReport", JacocoReport::class.java) { report ->
            val capitalizedVariant = variant.name.capitalize()
            val testTask = tasks.getByName("test${capitalizedVariant}UnitTest")
            report.dependsOn(testTask)
            report.group = "verification"
            report.description = "Generates Jacoco coverage reports for the ${variant.name} variant."

            report.reports {
                it.html.isEnabled = true
                it.xml.isEnabled = true
            }

            val sourceDirs = variant.sourceSets.flatMap { it.javaDirectories }
            val classesDir = variant.javaCompileProvider.get().destinationDir
            val executionData = testTask.extensions.getByType(JacocoTaskExtension::class.java).destinationFile

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
    "**/android/databinding/*Binding.class",
    "**/BR.*",
    "**/IssuesRegistry.*"
)
private val framework = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*"
)
private val butterKnife = listOf(
    "**/*${"$"}ViewInjector*.*",
    "**/*${"$"}ViewBinder*.*"
)
private val dagger = listOf(
    "**/*_MembersInjector.class",
    "**/Dagger*Component.class",
    "**/Dagger*Component${"$"}Builder.class",
    "**/*Module_*Factory.class"
)

private val excludes = databinding + framework + butterKnife + dagger
