package com.starter.quality.plugins

import com.starter.quality.internal.configureDetekt
import com.starter.quality.internal.configureKtlint
import com.starter.quality.tasks.ProjectCodeStyleTask.Companion.addProjectCodeStyleTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class QualityPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        addProjectCodeStyleTask()
        configureKtlint()
        configureDetekt()
    }
}
