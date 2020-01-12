package com.project.starter.quality.internal

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.modules.extensions.AndroidApplicationConfigExtension
import com.project.starter.modules.extensions.AndroidLibraryConfigExtension
import com.project.starter.modules.extensions.KotlinLibraryConfigExtension
import com.project.starter.modules.internal.withExtension
import com.project.starter.quality.tasks.GenerateCheckstyleBaselineTask.Companion.addGenerateCheckstyleBaselineTask
import com.project.starter.quality.tasks.ProjectCodeStyleTask
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.jmailen.gradle.kotlinter.id

internal fun Project.configureCheckstyle(rootConfig: RootConfigExtension) {
    pluginManager.withPlugin("kotlin") {
        if (pluginManager.hasPlugin("com.starter.library.kotlin")) {
            withExtension<KotlinLibraryConfigExtension> { config ->
                if (config.javaFilesAllowed ?: rootConfig.javaFilesAllowed) {
                    configureKotlinCheckstyle()
                }
            }
        } else {
            configureKotlinCheckstyle()
        }
    }
    pluginManager.withPlugin("com.android.library") {
        val android = extensions.getByType(LibraryExtension::class.java)
        if (pluginManager.hasPlugin("com.starter.library.android")) {
            withExtension<AndroidLibraryConfigExtension> { config ->
                if (config.javaFilesAllowed ?: rootConfig.javaFilesAllowed) {
                    configureAndroidCheckstyle(android)
                }
            }
        } else {
            configureAndroidCheckstyle(android)
        }
    }
    pluginManager.withPlugin("com.android.application") {
        val android = extensions.getByType(AppExtension::class.java)
        if (pluginManager.hasPlugin("com.starter.application.android")) {
            withExtension<AndroidApplicationConfigExtension> { config ->
                if (config.javaFilesAllowed ?: rootConfig.javaFilesAllowed) {
                    configureAndroidCheckstyle(android)
                }
            }
        } else {
            configureAndroidCheckstyle(android)
        }
    }
    tasks.named(ProjectCodeStyleTask.TASK_NAME).dependsOn("$path:checkstyle")
}

private fun Project.configureKotlinCheckstyle() {
    applyCheckstyle()
    tasks.named("checkstyleMain", Checkstyle::class.java, ::configureTask)
    tasks.named("checkstyleTest", Checkstyle::class.java, ::configureTask)
    tasks.register("checkstyle") {
        it.dependsOn("checkstyleMain", "checkstyleTest")
    }
    addGenerateCheckstyleBaselineTask()
}

private fun Project.configureAndroidCheckstyle(android: BaseExtension) {
    applyCheckstyle()
    val checkstyle = tasks.register("checkstyle")
    android.sourceSets.all { sourceSet ->
        val id = sourceSet.name.id
        val files = getJavaFiles(sourceSet) + getResourceFiles(sourceSet)
        if (files.isNotEmpty()) {
            val variantCheck = tasks.register("checkstyle${id.capitalize()}", Checkstyle::class.java) { task ->
                configureTask(task)
                task.classpath = files(buildDir)

                task.source(files)
            }
            checkstyle.dependsOn(variantCheck)
        }
    }
    addGenerateCheckstyleBaselineTask()
}

private fun Project.getJavaFiles(sourceSet: AndroidSourceSet) = sourceSet.java.srcDirs.map { dir ->
    fileTree(dir) {
        it.include("**/*.java")
    }
}.reduce { merged: FileTree, tree ->
    merged.plus(tree)
}.files

private fun Project.getResourceFiles(sourceSet: AndroidSourceSet) = sourceSet.res.srcDirs
    .filterNot { fileTree(it).isEmpty }

private fun Project.applyCheckstyle() {
    pluginManager.apply("checkstyle")
    extensions.configure<CheckstyleExtension>("checkstyle") {
        it.toolVersion = "8.28"
    }
}

private fun Project.configureTask(task: Checkstyle) {
    val suppressions = loadFromResources("checkstyle-suppressions.xml")
    val config = loadFromResources("checkstyle-config.xml")
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
