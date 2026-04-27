package com.project.starter.quality.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "This is a lifecycle task, it does not define any inputs or outputs.")
open class ProjectCodeStyleTask : DefaultTask() {

    init {
        description = "Runs code style checks against the whole project"
        group = "quality"
    }

    companion object {

        const val TASK_NAME = "projectCodeStyle"

        fun Project.addProjectCodeStyleTask(action: (ProjectCodeStyleTask) -> Unit = {}) {
            tasks.register(TASK_NAME, ProjectCodeStyleTask::class.java, action)
        }
    }
}
