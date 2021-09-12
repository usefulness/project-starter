package com.project.starter.modules.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.internal.configureRepositories
import com.project.starter.versioning.plugins.VersioningPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        afterEvaluate {
            if (rootConfig.versioning.enabled) {
                if (!rootProject.pluginManager.hasPlugin("com.starter.versioning")) {
                    logger.info("Apply com.starter.versioning to $rootProject")
                    rootProject.pluginManager.apply(VersioningPlugin::class.java)
                }
            }
        }
        configureRepositories()
    }
}
