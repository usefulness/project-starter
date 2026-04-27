package com.project.starter.modules.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "This is a lifecycle task, it does not define any inputs or outputs.")
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
