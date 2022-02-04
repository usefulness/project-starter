package com.project.starter.modules.plugins

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.project.starter.config.getByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.config.withExtension
import com.project.starter.modules.extensions.AndroidLibraryConfigExtension
import com.project.starter.modules.internal.configureAndroidLint
import com.project.starter.modules.internal.configureAndroidPlugin
import com.project.starter.modules.internal.configureAndroidProject
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("kotlin-android")
        pluginManager.apply("com.starter.quality")
        pluginManager.apply(ConfigurationPlugin::class.java)

        val rootConfig = this.rootConfig
        extensions.create("projectConfig", AndroidLibraryConfigExtension::class.java)

        val android = extensions.getByType<LibraryExtension>().apply {
            configureAndroidPlugin(rootConfig)
            defaultConfig.targetSdk = rootConfig.android.targetSdkVersion ?: rootConfig.android.compileSdkVersion

            buildFeatures.buildConfig = false

            configureAndroidLint(lint)
        }

        withExtension<AndroidLibraryConfigExtension> { projectConfig ->
            val androidComponents = extensions.getByType<LibraryAndroidComponentsExtension>()
            configureAndroidProject(androidComponents, projectConfig)
        }
    }
}
