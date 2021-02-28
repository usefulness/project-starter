package com.project.starter.modules.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.config.withExtension
import com.project.starter.modules.extensions.KotlinLibraryConfigExtension
import com.project.starter.modules.internal.configureKapt
import com.project.starter.modules.internal.configureKotlinCoverage
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.registerForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.registerProjectCoverageTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention

class KotlinLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("kotlin")
        pluginManager.apply(ConfigurationPlugin::class.java)

        extensions.create("projectConfig", KotlinLibraryConfigExtension::class.java)

        registerProjectTestTask {
            it.dependsOn("test")
        }

        configureKotlinCoverage()
        registerProjectCoverageTask {
            it.dependsOn("jacocoTestReport")
        }
        withExtension<KotlinLibraryConfigExtension> { config ->
            val javaFilesAllowed = config.javaFilesAllowed ?: rootConfig.javaFilesAllowed
            if (!javaFilesAllowed) {
                val forbidJavaFiles = registerForbidJavaFilesTask { task ->
                    val plugin = project.convention.getPlugin(JavaPluginConvention::class.java)
                    plugin.sourceSets.configureEach { sourceSet ->
                        if (sourceSet.name == "main" || sourceSet.name == "test") {
                            task.source += sourceSet.java
                        }
                    }
                }
                tasks.named("compileKotlin") {
                    it.dependsOn(forbidJavaFiles)
                }
            }

            if (config.useKapt ?: rootConfig.useKapt) {
                configureKapt()
            }
        }
    }
}
