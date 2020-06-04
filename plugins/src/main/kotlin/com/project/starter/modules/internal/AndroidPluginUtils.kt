package com.project.starter.modules.internal

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.AndroidExtension
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.registerForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.registerProjectCoverageTask
import com.project.starter.modules.tasks.ProjectLintTask.Companion.registerProjectLintTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import com.project.starter.quality.internal.configureAndroidCoverage
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

internal fun BaseExtension.configureAndroidPlugin(rootConfig: RootConfigExtension) {
    defaultConfig.apply {
        compileSdkVersion(rootConfig.android.compileSdkVersion)
        minSdkVersion(rootConfig.android.minSdkVersion)
        targetSdkVersion(rootConfig.android.targetSdkVersion ?: rootConfig.android.compileSdkVersion)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    dexOptions.preDexLibraries = true
    addKotlinSourceSets()

    compileOptions.apply {
        sourceCompatibility = rootConfig.javaVersion
        targetCompatibility = rootConfig.javaVersion
    }
}

internal fun Project.configureAndroidProject(variants: DomainObjectSet<out BaseVariant>, projectConfig: AndroidExtension) {
    configureAndroidCoverage(variants, projectConfig.coverageExclusions)
    val findBuildVariants = {
        if (projectConfig.defaultVariants.isEmpty()) {
            val default = variants.firstOrNull { it.buildType.name == "debug" }
                ?: variants.first()

            listOf(default.name.capitalize())
        } else {
            projectConfig.defaultVariants
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
    val javaFilesAllowed = projectConfig.javaFilesAllowed ?: rootConfig.javaFilesAllowed
    if (!javaFilesAllowed) {
        val forbidJavaFiles = registerForbidJavaFilesTask { task ->
            val extension = project.extensions.getByType(TestedExtension::class.java)
            extension.sourceSets.configureEach { sourceSet ->
                task.source += sourceSet.java.sourceFiles
            }
        }

        tasks.named("preBuild").dependsOn(forbidJavaFiles)
    }
}

internal inline fun <reified T> Project.withExtension(crossinline action: Project.(T) -> Unit) =
    afterEvaluate {
        it.action(it.extensions.getByType(T::class.java))
    }

private fun BaseExtension.addKotlinSourceSets() {
    sourceSets.configureEach { set ->
        val withKotlin = set.java.srcDirs.map { it.path.replace("java", "kotlin") }
        set.java.setSrcDirs(set.java.srcDirs + withKotlin)
    }
}
