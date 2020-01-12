package com.project.starter.quality.internal

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.modules.extensions.AndroidApplicationConfigExtension
import com.project.starter.modules.extensions.AndroidLibraryConfigExtension
import com.project.starter.modules.extensions.KotlinLibraryConfigExtension
import com.project.starter.modules.internal.withExtension
import com.project.starter.quality.tasks.GenerateCheckstyleBaselineTask.Companion.addGenerateCheckstyleBaselineTask
import com.project.starter.quality.tasks.ProjectCodeStyleTask
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

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
        val android = extensions.getByType(TestedExtension::class.java)
        if (pluginManager.hasPlugin("com.starter.library.android")) {
            withExtension<AndroidLibraryConfigExtension> { config ->
                if (config.javaFilesAllowed ?: rootConfig.javaFilesAllowed) {
                    configureAndroidCheckstyle(android)
                }
            }
        } else {
            configureAndroidCheckstyle(extensions.getByType(TestedExtension::class.java))
        }
    }
    pluginManager.withPlugin("com.android.application") {
        val android = extensions.getByType(TestedExtension::class.java)
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

private fun Project.configureAndroidCheckstyle(android: TestedExtension) {
    applyCheckstyle()
    tasks.register("checkstyleMain", Checkstyle::class.java) { task ->
        configureTask(task)
        task.classpath = files(buildDir)
        android.sourceSets.all { sourceSet ->
            if (sourceSet.name == "main") {
                task.source(sourceSet.java.sourceFiles, sourceSet.res.sourceFiles)
            }
        }
    }
    tasks.register("checkstyleTest", Checkstyle::class.java) { task ->
        configureTask(task)
        task.classpath = files(buildDir)
        android.sourceSets.all { sourceSet ->
            if (sourceSet.name == "test") {
                task.source(sourceSet.java.sourceFiles, sourceSet.res.sourceFiles)
            }
        }
    }
    tasks.register("checkstyle") {
        it.dependsOn("checkstyleMain", "checkstyleTest")
    }
    addGenerateCheckstyleBaselineTask()
}

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
    task.configFile = config

    task.reports { report ->
        report.html.isEnabled = false
        report.xml.isEnabled = true
    }
}
