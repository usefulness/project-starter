package com.project.starter.config.plugins

import com.project.starter.config.extensions.RootConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class CommonSettingsPlugin : Plugin<Settings> {

    override fun apply(target: Settings) {
        val extension = target.extensions.create(EXTENSION_NAME, RootConfigExtension::class.java)
        target.gradle.settingsEvaluated {
            gradle.lifecycle.beforeProject {
                extensions.add(RootConfigExtension::class.java, PROJECT_EXTENSION_NAME, extension)
            }
        }
    }

    private companion object {
        const val EXTENSION_NAME = "commonConfig"
        const val PROJECT_EXTENSION_NAME = "starterCommonConfig"
    }
}

val Project.rootConfig: RootConfigExtension
    get() = extensions.findByType(RootConfigExtension::class.java)
        ?: RootConfigExtension()
