package com.project.starter.modules.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.internal.configureKapt
import com.project.starter.modules.internal.configureRepositories
import com.project.starter.versioning.plugins.VersioningPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class ConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (rootConfig.quality.enabled) {
            pluginManager.apply("com.starter.quality")
        }
        if (rootConfig.versioning.enabled) {
            if (!rootProject.pluginManager.hasPlugin("com.starter.versioning")) {
                logger.debug("Apply com.starter.versioning to $rootProject")
                rootProject.pluginManager.apply(VersioningPlugin::class.java)
            }
        }
        configureKapt()
        configureRepositories()

        val javaVersion = rootConfig.javaVersion
        tasks.withType(KotlinCompile::class.java).configureEach {
            it.kotlinOptions.jvmTarget = javaVersion.toString()
        }
    }
}
