package com.project.starter.modules.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

open class ProjectCoverageTask : DefaultTask() {

    init {
        description = "Generates code coverage report for the project"
        group = "quality"
    }

    companion object {

        const val TASK_NAME = "projectCoverage"

        fun Project.registerProjectCoverageTask(action: (ProjectCoverageTask) -> Unit = {}): TaskProvider<ProjectCoverageTask> =
            tasks.register(TASK_NAME, ProjectCoverageTask::class.java, action)
    }
}
