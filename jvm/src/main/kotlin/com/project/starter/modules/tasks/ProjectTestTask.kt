package com.project.starter.modules.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "This is a lifecycle task, it does not define any inputs or outputs.")
open class ProjectTestTask : DefaultTask() {

    init {
        description = "Runs Unit tests against the whole project"
        group = "quality"
    }

    companion object {

        const val TASK_NAME = "projectTest"

        fun Project.registerProjectTestTask(action: (ProjectTestTask) -> Unit = {}): TaskProvider<ProjectTestTask> =
            tasks.register(TASK_NAME, ProjectTestTask::class.java, action)
    }
}
