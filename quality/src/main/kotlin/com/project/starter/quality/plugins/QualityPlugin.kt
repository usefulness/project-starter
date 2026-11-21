package com.project.starter.quality.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.quality.internal.configureKtlint
import com.project.starter.quality.tasks.ProjectCodeStyleTask.Companion.addProjectCodeStyleTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class QualityPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        runCatching { repositories.mavenCentral() }
        addProjectCodeStyleTask()
        configureKtlint()
        configureFormatOnRecompile()
    }

    private fun Project.configureFormatOnRecompile() {
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            tasks.named("compileKotlin") {
                if (rootConfig.quality.formatOnCompile) {
                    dependsOn("formatKotlin")
                }
            }
        }
        pluginManager.withPlugin("com.android.library") {
            tasks.named("preBuild") {
                if (rootConfig.quality.formatOnCompile) {
                    dependsOn("formatKotlin")
                }
            }
        }
        pluginManager.withPlugin("com.android.application") {
            tasks.named("preBuild") {
                if (rootConfig.quality.formatOnCompile) {
                    dependsOn("formatKotlin")
                }
            }
        }
    }
}
