package com.project.starter.modules.plugins

import com.android.build.gradle.LibraryExtension
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
            configureAndroidLint(lintOptions)
        }

        withExtension<AndroidLibraryConfigExtension> { projectConfig ->
            val variants = android.libraryVariants

            configureAndroidProject(variants, projectConfig)

            // Use `buildFeatures.buildConfig` after https://issuetracker.google.com/issues/159540417 is fixed
            variants.configureEach { variant ->
                variant.generateBuildConfigProvider.configure { it.enabled = projectConfig.generateBuildConfig }
            }
        }
    }
}
