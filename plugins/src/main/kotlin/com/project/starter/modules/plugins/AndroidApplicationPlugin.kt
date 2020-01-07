package com.project.starter.modules.plugins

import com.android.build.gradle.AppExtension
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.AndroidApplicationConfigExtension
import com.project.starter.modules.internal.configureAndroidPlugin
import com.project.starter.modules.internal.configureAndroidProject
import com.project.starter.modules.internal.withExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("kotlin-android")
        pluginManager.apply(ConfigurationPlugin::class.java)

        extensions.create("applicationConfig", AndroidApplicationConfigExtension::class.java)

        val android = extensions.getByType<AppExtension>(AppExtension::class.java).apply {
            configureAndroidPlugin(rootConfig)
        }

        withExtension<AndroidApplicationConfigExtension> { projectConfig ->
            configureAndroidProject(android.applicationVariants, projectConfig)
        }
    }
}
