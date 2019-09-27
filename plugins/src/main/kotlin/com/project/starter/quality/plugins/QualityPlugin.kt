package com.project.starter.quality.plugins

import com.project.starter.quality.internal.configureDetekt
import com.project.starter.quality.internal.configureKtlint
import com.project.starter.quality.tasks.ProjectCodeStyleTask.Companion.addProjectCodeStyleTask
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class QualityPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        addProjectCodeStyleTask()
        configureKtlint()
        configureDetekt()
    }
}
