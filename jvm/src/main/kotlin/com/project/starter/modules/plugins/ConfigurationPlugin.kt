package com.project.starter.modules.plugins

import com.project.starter.modules.internal.configureRepositories
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        configureRepositories()
    }
}
