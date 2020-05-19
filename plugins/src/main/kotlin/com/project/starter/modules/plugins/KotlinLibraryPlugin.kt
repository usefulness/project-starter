package com.project.starter.modules.plugins

import com.android.build.gradle.internal.dsl.LintOptions
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.KotlinLibraryConfigExtension
import com.project.starter.modules.internal.configureAndroidLint
import com.project.starter.modules.internal.withExtension
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.registerForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.registerProjectCoverageTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import com.project.starter.modules.tasks.ProjectLintTask.Companion.registerProjectLintTask
import com.project.starter.quality.internal.configureKotlinCoverage
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
        pluginManager.withPlugin("com.android.lint") {
            registerProjectLintTask { projectLint ->
                projectLint.dependsOn("$path:lint")
            }

            configureAndroidLint(extensions.getByType(LintOptions::class.java))
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
                tasks.named("compileKotlin").dependsOn(forbidJavaFiles)
            }
        }
    }
}
