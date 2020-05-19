package com.project.starter.modules.tasks

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

@CacheableTask
open class ForbidJavaFilesTask : SourceTask() {

    @TaskAction
    fun run() {
        source.visit { file ->
            if (file.name.endsWith(".java")) {
                logger.error("Error at $file")
                throw GradleException("Java files are not allowed within ${project.path}")
            }
        }
    }

    companion object {

        private const val TASK_NAME = "forbidJavaFiles"

        fun Project.registerForbidJavaFilesTask(action: (ForbidJavaFilesTask) -> Unit = {}) =
            tasks.register(TASK_NAME, ForbidJavaFilesTask::class.java, action)
    }
}
