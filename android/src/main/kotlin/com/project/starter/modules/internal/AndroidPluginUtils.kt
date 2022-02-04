package com.project.starter.modules.internal

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.TestedExtension
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.config.getByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.AndroidExtension
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.registerForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.registerProjectCoverageTask
import com.project.starter.modules.tasks.ProjectLintTask.Companion.registerProjectLintTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import com.project.starter.quality.internal.configureAndroidCoverage
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun CommonExtension<*, *, *, *>.configureAndroidPlugin(rootConfig: RootConfigExtension) {
    defaultConfig.apply {
        compileSdk = rootConfig.android.compileSdkVersion
        minSdk = rootConfig.android.minSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions.apply {
        sourceCompatibility = rootConfig.javaVersion
        targetCompatibility = rootConfig.javaVersion
    }
}

internal fun Project.configureAndroidProject(androidComponents: AndroidComponentsExtension<*, *, *>, projectConfig: AndroidExtension) {
    configureAndroidCoverage(androidComponents, projectConfig.coverageExclusions)
    val projectLint = registerProjectLintTask()
    val projectTest = registerProjectTestTask()
    val projectCoverage = registerProjectCoverageTask()
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

    val selectors = if (projectConfig.defaultVariants.isEmpty()) {
        listOf(androidComponents.selector().all())
    } else {
        projectConfig.defaultVariants.map { androidComponents.selector().withName(it) }
    }
    selectors.forEach { selector ->
        androidComponents.onVariants(selector) { variant ->
            projectLint.dependsOn("$path:lint${variant.name.capitalize()}")
            projectTest.dependsOn("$path:test${variant.name.capitalize()}UnitTest")
            projectCoverage.dependsOn("$path:jacoco${variant.name.capitalize()}TestReport")
        }
    }
}

private fun <T : Task> TaskProvider<out T>.dependsOn(name: String) {
    configure { it.dependsOn(name) }
}
