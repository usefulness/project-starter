package com.project.starter.modules.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.project.starter.config.getByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.AndroidApplicationConfigExtension
import com.project.starter.modules.internal.configureAndroidLint
import com.project.starter.modules.internal.configureAndroidPlugin
import com.project.starter.modules.internal.configureAndroidProject
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("kotlin-android")
        pluginManager.apply("com.starter.quality")
        pluginManager.apply(ConfigurationPlugin::class.java)

        extensions.create("projectConfig", AndroidApplicationConfigExtension::class.java)

        extensions.getByType<ApplicationExtension>().apply {
            configureAndroidPlugin(rootConfig)
            defaultConfig.targetSdk = rootConfig.android.targetSdkVersion ?: rootConfig.android.compileSdkVersion
            configureAndroidLint(lint)
        }

        configureAndroidProject<AndroidApplicationConfigExtension, ApplicationAndroidComponentsExtension>()
    }
}
