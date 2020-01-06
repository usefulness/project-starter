package com.project.starter.modules.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project

open class ProjectTestTask : DefaultTask() {

    init {
        description = "Runs Unit tests against the whole project"
        group = "quality"
    }

    companion object {

        const val TASK_NAME = "projectTest"

        fun Project.addProjectTestTask(action: (ProjectTestTask) -> Unit = {}) =
            tasks.register(TASK_NAME, ProjectTestTask::class.java, action)
    }
}
