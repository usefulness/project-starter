package com.project.starter.quality.plugins

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.plugins.rootConfig
import com.project.starter.quality.internal.configureDetekt
import com.project.starter.quality.internal.configureKtlint
import com.project.starter.quality.tasks.ProjectCodeStyleTask.Companion.addProjectCodeStyleTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class QualityPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        repositories.jcenter()
        addProjectCodeStyleTask()
        configureKtlint()
        configureDetekt(rootConfig)

        val config = rootConfig.quality
        if (config.formatOnCompile) {
            applyFormatOnRecompile()
        }
    }

    private fun Project.applyFormatOnRecompile() {
        pluginManager.withPlugin("kotlin") {
            tasks.named("compileKotlin").dependsOn("$path:formatKotlin")
        }
        pluginManager.withPlugin("com.android.library") {
            tasks.named("preBuild").dependsOn("$path:formatKotlin")
        }
        pluginManager.withPlugin("com.android.application") {
            tasks.named("preBuild").dependsOn("$path:formatKotlin")
        }
    }
}
