package com.project.starter.modules.plugins

import com.project.starter.config.findByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.config.withExtension
import com.project.starter.modules.extensions.KotlinLibraryConfigExtension
import com.project.starter.modules.internal.configureKotlinCoverage
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.registerForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.registerProjectCoverageTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("kotlin")
        pluginManager.apply("com.starter.quality")
        pluginManager.apply(ConfigurationPlugin::class.java)

        extensions.create("projectConfig", KotlinLibraryConfigExtension::class.java)

        tasks.withType(KotlinCompile::class.java).configureEach {
            it.kotlinOptions.jvmTarget = rootConfig.javaVersion.toString()
        }
        tasks.withType(JavaCompile::class.java).configureEach {
            it.options.release.set(rootConfig.javaVersion.majorVersion.toInt())
        }
        registerProjectTestTask {
            it.dependsOn("test")
        }

        configureKotlinCoverage()
        registerProjectCoverageTask { projectCoverage ->
            projectCoverage.dependsOn("jacocoTestReport")
        }
        withExtension<KotlinLibraryConfigExtension> { config ->
            val javaFilesAllowed = config.javaFilesAllowed ?: rootConfig.javaFilesAllowed
            if (!javaFilesAllowed) {
                val forbidJavaFiles = registerForbidJavaFilesTask { task ->
                    project.extensions.findByType<SourceSetContainer>()?.configureEach { sourceSet ->
                        if (sourceSet.name == "main" || sourceSet.name == "test") {
                            task.source += sourceSet.java
                        }
                    }
                }
                tasks.named("compileKotlin") {
                    it.dependsOn(forbidJavaFiles)
                }
            }
        }
    }
}
