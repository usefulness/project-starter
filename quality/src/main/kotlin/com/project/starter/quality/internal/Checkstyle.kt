@file:Suppress("DEPRECATION") // https://issuetracker.google.com/issues/170650362

package com.project.starter.quality.internal

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.config.withExtension
import com.project.starter.quality.extensions.JavaSourcesAware
import com.project.starter.quality.tasks.GenerateCheckstyleBaselineTask.Companion.addGenerateCheckstyleBaselineTask
import com.project.starter.quality.tasks.ProjectCodeStyleTask
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

private val pluginsWithConfgiuration = listOf(
    Config(
        plugin = "kotlin",
        starterPlugin = "com.starter.library.kotlin",
        configuration = Project::configureKotlinCheckstyle
    ),
    Config(
        plugin = "com.android.library",
        starterPlugin = "com.starter.library.android",
        configuration = Project::configureAndroidCheckstyle
    ),
    Config(
        plugin = "com.android.application",
        starterPlugin = "com.starter.application.android",
        configuration = Project::configureAndroidCheckstyle
    )
)

private data class Config(
    val plugin: String,
    val starterPlugin: String,
    val configuration: Project.() -> Unit
)

internal fun Project.configureCheckstyle(rootConfig: RootConfigExtension) {
    pluginsWithConfgiuration.forEach { (plugin, starterPlugin, configuration) ->
        pluginManager.withPlugin(plugin) {
            if (pluginManager.hasPlugin(starterPlugin)) {
                withExtension<JavaSourcesAware> {
                    if (it.javaFilesAllowed ?: rootConfig.javaFilesAllowed) {
                        configuration()
                    }
                }
            } else {
                afterEvaluate {
                    if (rootConfig.javaFilesAllowed) {
                        configuration()
                    }
                }
            }
        }
    }
}

private fun Project.configureKotlinCheckstyle() {
    applyCheckstyle()
    tasks.named("checkstyleMain", Checkstyle::class.java, ::configureTask)
    tasks.named("checkstyleTest", Checkstyle::class.java, ::configureTask)
    val checkstyle = tasks.register("checkstyle") {
        it.dependsOn("checkstyleMain", "checkstyleTest")
    }
    addGenerateCheckstyleBaselineTask()
    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        it.dependsOn(checkstyle)
    }
}

private fun Project.configureAndroidCheckstyle() {
    val android = extensions.getByName("android") as BaseExtension
    applyCheckstyle()
    val checkstyle = tasks.register("checkstyle")
    android.sourceSets.configureEach { sourceSet ->
        val id = sourceSet.name.split(" ").first()
        val files = getJavaFiles(sourceSet) + getResourceFiles(sourceSet)
        if (files.isNotEmpty()) {
            val variantCheck = tasks.register("checkstyle${id.capitalize()}", Checkstyle::class.java) { task ->
                configureTask(task)
                task.classpath = files(buildDir)

                task.source(files)
            }
            checkstyle.configure {
                it.dependsOn(variantCheck)
            }
        }
    }
    tasks.named(ProjectCodeStyleTask.TASK_NAME) {
        it.dependsOn(checkstyle)
    }

    addGenerateCheckstyleBaselineTask()
}

private fun Project.getJavaFiles(sourceSet: AndroidSourceSet) = sourceSet.java.srcDirs.map { dir ->
    fileTree(dir) {
        it.include("**/*.java")
    }
}.reduce { merged: FileTree, tree: FileTree ->
    merged + tree
}.files

private fun Project.getResourceFiles(sourceSet: AndroidSourceSet) = sourceSet.res.srcDirs
    .filterNot { fileTree(it).isEmpty }

private fun Project.applyCheckstyle() {
    pluginManager.apply("checkstyle")
    extensions.configure<CheckstyleExtension>("checkstyle") {
        it.toolVersion = "8.29"
    }
}

private fun Project.configureTask(task: Checkstyle) {
    val suppressions = loadFromResources("checkstyle-suppressions.xml").orNull
    val config = loadFromResources("checkstyle-config.xml").orNull
    logger.info("Checkstyle config: $config")

    task.configProperties = mapOf(
        "suppressions.global.file" to suppressions,
        "suppressions.local.file" to project.file("checkstyle-baseline.xml")
    )
    config?.let { task.configFile = it } ?: logger.warn("Missing Checkstyle configuration file")

    task.reports { report ->
        report.html.isEnabled = false
        report.xml.isEnabled = true
    }
}