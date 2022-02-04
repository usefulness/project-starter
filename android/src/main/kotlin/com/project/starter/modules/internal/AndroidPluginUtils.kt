package com.project.starter.modules.internal

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.TestedExtension
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.config.getByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.config.withExtension
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

internal inline fun <reified TStarter, reified TAgp> Project.configureAndroidProject()
    where TStarter : AndroidExtension, TAgp : AndroidComponentsExtension<*, *, *> {
    val androidComponents = extensions.getByType(TAgp::class.java)

    configureAndroidCoverage(androidComponents) { extensions.getByType(TStarter::class.java).coverageExclusions }
    val projectLint = registerProjectLintTask()
    val projectTest = registerProjectTestTask()
    val projectCoverage = registerProjectCoverageTask()
    tasks.withType(KotlinCompile::class.java).configureEach {
        it.kotlinOptions.jvmTarget = rootConfig.javaVersion.toString()
    }

    withExtension<TStarter> { projectConfig ->
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

    androidComponents.beforeVariants { variant ->
        val capitalizedName = variant.name.capitalize()
        projectLint.dependsOn("$path:lint$capitalizedName")
        projectTest.dependsOn("$path:test${capitalizedName}UnitTest")
        projectCoverage.dependsOn("$path:jacoco${capitalizedName}TestReport")
    }
}

private fun <T : Task> TaskProvider<out T>.dependsOn(name: String) {
    configure { it.dependsOn(name) }
}
