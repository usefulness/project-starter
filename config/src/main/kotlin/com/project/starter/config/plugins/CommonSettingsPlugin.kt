package com.project.starter.config.plugins

import com.project.starter.config.extensions.RootConfigExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class CommonSettingsPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (this != rootProject) {
            throw GradleException("Common configuration can be applied to the root project only")
        }
        extensions.create("commonConfig", RootConfigExtension::class.java)
    }
}

val Project.rootConfig: RootConfigExtension
    get() = rootProject.extensions.findByType(RootConfigExtension::class.java)
        ?: RootConfigExtension()
