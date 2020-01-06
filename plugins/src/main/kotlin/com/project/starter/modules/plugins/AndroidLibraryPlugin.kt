package com.project.starter.modules.plugins

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.AndroidLibraryConfigExtension
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.addForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.addProjectCoverageTask
import com.project.starter.modules.tasks.ProjectLintTask.Companion.addProjectLintTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.addProjectTestTask
import com.project.starter.quality.internal.configureAndroidCoverage
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("kotlin-android")
        pluginManager.apply(ConfigurationPlugin::class.java)

        val rootConfig = this.rootConfig
        extensions.create("libraryConfig", AndroidLibraryConfigExtension::class.java)

        val android = extensions.getByType<LibraryExtension>(LibraryExtension::class.java).apply {
            defaultConfig.apply {
                compileSdkVersion(rootConfig.android.compileSdkVersion)
                minSdkVersion(rootConfig.android.minSdkVersion)
                targetSdkVersion(rootConfig.android.targetSdkVersion ?: rootConfig.android.compileSdkVersion)
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            dexOptions {
                it.preDexLibraries = true
            }
            addKotlinSourceSets()

            compileOptions.apply {
                sourceCompatibility = rootConfig.javaVersion
                targetCompatibility = rootConfig.javaVersion
            }
        }

        val libraryVariants = android.libraryVariants
        withExtension { projectConfig ->
            libraryVariants.all { variant ->
                variant.generateBuildConfigProvider.configure { it.enabled = projectConfig.generateBuildConfig }
            }
            configureAndroidCoverage(libraryVariants, projectConfig.coverageExclusions)
            val findBuildVariants = {
                if (projectConfig.defaultVariants.isEmpty()) {
                    val default = libraryVariants.firstOrNull { it.buildType.name == "debug" }
                        ?: libraryVariants.first()

                    listOf(default.name.capitalize())
                } else {
                    projectConfig.defaultVariants
                }
            }
            addProjectLintTask { projectLint ->
                val childTasks = findBuildVariants().map { "$path:lint${it.capitalize()}" }
                childTasks.forEach { projectLint.dependsOn(it) }
            }
            addProjectTestTask { projectTest ->
                val childTasks = findBuildVariants().map { "$path:test${it.capitalize()}UnitTest" }
                childTasks.forEach { projectTest.dependsOn(it) }
            }
            addProjectCoverageTask { projectCoverage ->
                val childTasks = findBuildVariants().map { "$path:jacoco${it.capitalize()}TestReport" }
                childTasks.forEach { projectCoverage.dependsOn(it) }
            }
            val javaFilesAllowed = projectConfig.javaFilesAllowed ?: rootConfig.javaFilesAllowed
            if (!javaFilesAllowed) {
                tasks.named("preBuild").dependsOn(addForbidJavaFilesTask())
            }
        }
    }

    private fun Project.withExtension(action: Project.(AndroidLibraryConfigExtension) -> Unit) =
        afterEvaluate {
            it.action(it.extensions.getByType(AndroidLibraryConfigExtension::class.java))
        }

    private fun BaseExtension.addKotlinSourceSets() {
        sourceSets.all { set ->
            val withKotlin = set.java.srcDirs.map { it.path.replace("java", "kotlin") }
            set.java.setSrcDirs(set.java.srcDirs + withKotlin)
        }
        // triggers info that source sets were changed
        val copy = sourceSets.toList()
        sourceSets.clear()
        sourceSets.addAll(copy)
    }
}
