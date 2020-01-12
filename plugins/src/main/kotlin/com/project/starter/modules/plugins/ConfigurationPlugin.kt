package com.project.starter.modules.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.internal.configureCommonDependencies
import com.project.starter.modules.internal.configureKapt
import com.project.starter.modules.internal.configureRepositories
import com.project.starter.quality.plugins.QualityPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class ConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (rootConfig.quality.enabled) {
            pluginManager.apply(QualityPlugin::class.java)
        }
        configureKapt()
        configureRepositories()

        dependencies.configureCommonDependencies()

        val javaVersion = rootConfig.javaVersion
        tasks.withType(KotlinCompile::class.java).all {
            it.kotlinOptions.jvmTarget = javaVersion.toString()
        }
    }
}
