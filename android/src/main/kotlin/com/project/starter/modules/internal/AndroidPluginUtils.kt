package com.project.starter.modules.internal

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.config.getByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.AndroidExtension
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.registerForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.registerProjectCoverageTask
import com.project.starter.modules.tasks.ProjectLintTask.Companion.registerProjectLintTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import com.project.starter.quality.internal.configureAndroidCoverage
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun BaseExtension.configureAndroidPlugin(rootConfig: RootConfigExtension) {
    defaultConfig.apply {
        compileSdkVersion(rootConfig.android.compileSdkVersion)
        minSdk = rootConfig.android.minSdkVersion
        targetSdk = rootConfig.android.targetSdkVersion ?: rootConfig.android.compileSdkVersion
        setTestInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }

    compileOptions.apply {
        sourceCompatibility = rootConfig.javaVersion
        targetCompatibility = rootConfig.javaVersion
    }
}

internal fun Project.configureAndroidProject(variants: DomainObjectSet<out BaseVariant>, projectConfig: AndroidExtension) {
    configureAndroidCoverage(variants, projectConfig.coverageExclusions)
    val findBuildVariants = {
        projectConfig.defaultVariants.ifEmpty {
            val default = variants.firstOrNull { it.buildType.name == "debug" } ?: variants.first()

            listOf(default.name.capitalize())
        }
    }
    registerProjectLintTask { projectLint ->
        val childTasks = findBuildVariants().map { "$path:lint${it.capitalize()}" }
        childTasks.forEach { projectLint.dependsOn(it) }
    }
    registerProjectTestTask { projectTest ->
        val childTasks = findBuildVariants().map { "$path:test${it.capitalize()}UnitTest" }
        childTasks.forEach { projectTest.dependsOn(it) }
    }
    registerProjectCoverageTask { projectCoverage ->
        val childTasks = findBuildVariants().map { "$path:jacoco${it.capitalize()}TestReport" }
        childTasks.forEach { projectCoverage.dependsOn(it) }
    }
    tasks.withType(KotlinCompile::class.java).configureEach {
        it.kotlinOptions.jvmTarget = rootConfig.javaVersion.toString()
    }
    val javaFilesAllowed = projectConfig.javaFilesAllowed ?: rootConfig.javaFilesAllowed
    if (!javaFilesAllowed) {
        val forbidJavaFiles = registerForbidJavaFilesTask { task ->
            val extension = project.extensions.getByType<TestedExtension>()
            extension.sourceSets.configureEach { sourceSet ->
                task.source += sourceSet.java.getSourceFiles()
            }
        }

        tasks.named("preBuild") {
            it.dependsOn(forbidJavaFiles)
        }
    }
}
