package com.project.starter.modules.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.internal.configureRepositories
import com.project.starter.versioning.plugins.VersioningPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        afterEvaluate {
            if (rootConfig.versioning.enabled) {
                if (!rootProject.pluginManager.hasPlugin("com.starter.versioning")) {
                    logger.info("Apply com.starter.versioning to $rootProject")
                    rootProject.pluginManager.apply(VersioningPlugin::class.java)
                }
            }
            tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions.jvmTarget = rootConfig.javaVersion.toString()
            }
            tasks.withType(JavaCompile::class.java).configureEach {
                it.options.release.set(rootConfig.javaVersion.majorVersion.toInt())
            }
        }
        configureRepositories()
    }
}
