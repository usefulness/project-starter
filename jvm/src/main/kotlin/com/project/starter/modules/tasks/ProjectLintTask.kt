package com.project.starter.modules.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

open class ProjectLintTask : DefaultTask() {

    init {
        description = "Runs Android Lint checks against the whole project"
        group = "quality"
    }

    companion object {

        const val TASK_NAME = "projectLint"

        fun Project.registerProjectLintTask(action: (ProjectLintTask) -> Unit = {}): TaskProvider<ProjectLintTask> =
            tasks.register(TASK_NAME, ProjectLintTask::class.java, action)
    }
}
