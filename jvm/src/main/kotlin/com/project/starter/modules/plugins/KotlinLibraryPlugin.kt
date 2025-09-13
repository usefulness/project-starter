package com.project.starter.modules.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.KotlinLibraryConfigExtension
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class KotlinLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply("com.starter.quality")
        pluginManager.apply(ConfigurationPlugin::class.java)

        extensions.create("projectConfig", KotlinLibraryConfigExtension::class.java)

        tasks.withType(KotlinJvmCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(rootConfig.javaVersion.toString()))
        }
        tasks.withType(JavaCompile::class.java).configureEach {
            options.release.set(rootConfig.javaVersion.majorVersion.toInt())
        }
        registerProjectTestTask {
            it.dependsOn("test")
        }

        pluginManager.withPlugin("java-gradle-plugin") {
            tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java).configureEach {
                compilerOptions {
                    freeCompilerArgs.add("-Xlambdas=class")
                }
            }
        }
    }
}
