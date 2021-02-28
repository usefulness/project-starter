package com.project.starter.modules.plugins

import com.android.build.gradle.AppExtension
import com.project.starter.config.getByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.config.withExtension
import com.project.starter.modules.extensions.AndroidApplicationConfigExtension
import com.project.starter.modules.internal.configureAndroidLint
import com.project.starter.modules.internal.configureAndroidPlugin
import com.project.starter.modules.internal.configureAndroidProject
import com.project.starter.modules.internal.configureKapt
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("kotlin-android")
        pluginManager.apply(ConfigurationPlugin::class.java)

        extensions.create("projectConfig", AndroidApplicationConfigExtension::class.java)

        val android = extensions.getByType<AppExtension>().apply {
            configureAndroidPlugin(rootConfig)
            configureAndroidLint(lintOptions)
        }

        withExtension<AndroidApplicationConfigExtension> { projectConfig ->
            configureAndroidProject(android.applicationVariants, projectConfig)

            if (projectConfig.useKapt ?: rootConfig.useKapt) {
                configureKapt()
            }
        }
    }
}
