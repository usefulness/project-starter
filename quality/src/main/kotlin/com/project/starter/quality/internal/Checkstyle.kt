package com.project.starter.quality.internal

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import com.project.starter.config.getByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.config.withExtension
import com.project.starter.quality.extensions.JavaSourcesAware
import com.project.starter.quality.tasks.GenerateCheckstyleBaselineTask.Companion.addGenerateCheckstyleBaselineTask
import com.project.starter.quality.tasks.ProjectCodeStyleTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

private val pluginsWithConfiguration = listOf(
    Config(
        plugin = "kotlin",
        starterPlugin = "com.starter.library.kotlin",
        configuration = { configureKotlinCheckstyle() },
    ),
    Config(
        plugin = "com.android.library",
        starterPlugin = "com.starter.library.android",
        configuration = { configureAndroidCheckstyle(extensions.getByType<LibraryExtension>().libraryVariants) },
    ),
    Config(
        plugin = "com.android.application",
        starterPlugin = "com.starter.application.android",
        configuration = { configureAndroidCheckstyle(extensions.getByType<AppExtension>().applicationVariants) },
    ),
)

private data class Config(
    val plugin: String,
    val starterPlugin: String,
    val configuration: Project.() -> Unit,
)

internal fun Project.configureCheckstyle() {
    pluginsWithConfiguration.forEach { (plugin, starterPlugin, configuration) ->
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
    tasks.named(ProjectCodeStyleTask.TASK_NAME) { it.dependsOn(checkstyle) }
}

private fun Project.configureAndroidCheckstyle(variants: DomainObjectSet<out BaseVariant>) {
    applyCheckstyle()
    val checkstyle = tasks.register("checkstyle")
    val android = project.extensions.getByName("android") as TestedExtension
    val config = { variant: BaseVariant ->
        val variantCheck = tasks.register("checkstyle${variant.name.capitalize()}", Checkstyle::class.java) { task ->
            configureTask(task)
            task.classpath = files(buildDir)
            task.source(variant.sourceSets.flatMap { it.javaDirectories + it.resDirectories })
        }
        checkstyle.configure { it.dependsOn(variantCheck) }
    }
    variants.configureEach(config)
    android.unitTestVariants.configureEach(config)
    android.testVariants.configureEach(config)

    addGenerateCheckstyleBaselineTask()
    tasks.named(ProjectCodeStyleTask.TASK_NAME) { it.dependsOn(checkstyle) }
}

private fun Project.applyCheckstyle() {
    pluginManager.apply("checkstyle")
}

private fun Project.configureTask(task: Checkstyle) {
    val suppressions = loadFromResources("checkstyle-suppressions.xml").orNull
    val config = loadFromResources("checkstyle-config.xml").orNull
    logger.info("Checkstyle config: $config")

    task.configProperties = mapOf(
        "suppressions.global.file" to suppressions,
        "suppressions.local.file" to file("checkstyle-baseline.xml"),
    )
    config?.let { task.configFile = it } ?: logger.warn("Missing Checkstyle configuration file")

    task.reports { report ->
        val reportsDir = extensions.getByType(CheckstyleExtension::class.java).reportsDir
        report.html.required.set(false)
        report.html.outputLocation.set(reportsDir.resolve("${task.name}.html"))
        report.xml.required.set(false)
        report.xml.outputLocation.set(reportsDir.resolve("${task.name}.xml"))
    }
}
