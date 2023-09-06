package com.project.starter.quality.internal

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.project.starter.config.getByType
import com.project.starter.modules.internal.daggerCoverageExclusions
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

internal fun Project.configureAndroidCoverage(
    androidComponents: AndroidComponentsExtension<*, *, *>,
    projectExclusions: () -> List<String>,
) {
    pluginManager.apply("jacoco")

    extensions.configure(JacocoPluginExtension::class.java) {
        it.toolVersion = "0.8.10"
    }
    tasks.withType(Test::class.java).configureEach {
        it.extensions.getByType(JacocoTaskExtension::class.java).apply {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    androidComponents.onVariants { variant ->
        val capitalizedVariant = variant.name.replaceFirstChar(Char::titlecase)
        tasks.register("jacoco${capitalizedVariant}TestReport", JacocoReport::class.java) { report ->
            val testTask = tasks.getByName("test${capitalizedVariant}UnitTest")
            val jacocoTestTaskExtension = testTask.extensions.getByType<JacocoTaskExtension>().apply {
                isIncludeNoLocationClasses = true
            }
            report.dependsOn(testTask)
            report.group = "verification"
            report.description = "Generates Jacoco coverage reports for the ${variant.name} variant."

            report.reports {
                it.html.required.set(true)
                it.xml.required.set(true)
            }

            val oldVariant = when (val android = project.extensions.getByName("android")) {
                is AppExtension -> android.applicationVariants.firstOrNull { it.name == variant.name }
                is LibraryExtension -> android.libraryVariants.firstOrNull { it.name == variant.name }
                else -> null
            } ?: return@register logger.warn("Couldn't find variant ${variant.name}")
            val sourceDirs = oldVariant.sourceSets.flatMap { it.javaDirectories + it.kotlinDirectories }
            val classesDir = oldVariant.javaCompileProvider.get().destinationDirectory.get().asFile
            val executionData = jacocoTestTaskExtension.destinationFile

            val coverageExcludes = excludes + projectExclusions()
            val kotlinClassesDir = "${layout.buildDirectory.get()}/tmp/kotlin-classes/${variant.name}"
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
    "**/IssuesRegistry.*",
)
private val framework = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
)

private val excludes = databinding + framework + daggerCoverageExclusions
