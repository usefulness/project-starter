package com.project.starter.modules.tasks

import com.android.build.gradle.TestedExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

@CacheableTask
open class ForbidJavaFilesTask : SourceTask() {

    init {
        if (project.hasProperty("android")) {
            val extension = project.extensions.getByType(TestedExtension::class.java)
            extension.sourceSets.all {
                source += it.java.sourceFiles
            }
        } else {
            val plugin = project.convention.getPlugin(JavaPluginConvention::class.java)
            plugin.sourceSets.all {
                if (it.name == "main" || it.name == "test") {
                    source += it.java
                }
            }
        }
    }

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

        fun Project.addForbidJavaFilesTask(action: (ForbidJavaFilesTask) -> Unit = {}) =
            tasks.register(TASK_NAME, ForbidJavaFilesTask::class.java, action)
    }
}
