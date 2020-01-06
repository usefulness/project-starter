package com.project.starter.modules.plugins

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.KotlinLibraryConfigExtension
import com.project.starter.modules.tasks.ForbidJavaFilesTask.Companion.addForbidJavaFilesTask
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.addProjectCoverageTask
import com.project.starter.modules.tasks.ProjectLintTask.Companion.addProjectLintTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.addProjectTestTask
import com.project.starter.quality.internal.configureKotlinCoverage
import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("kotlin")
        pluginManager.apply(ConfigurationPlugin::class.java)

        extensions.create("libraryConfig", KotlinLibraryConfigExtension::class.java)

        addProjectTestTask {
            it.dependsOn("test")
        }

        configureKotlinCoverage()
        addProjectCoverageTask {
            it.dependsOn("jacocoTestReport")
        }
        pluginManager.withPlugin("com.android.lint") {
            addProjectLintTask { projectLint ->
                projectLint.dependsOn("$path:lint")
            }
        }
        withExtension { config ->
            val javaFilesAllowed = config.javaFilesAllowed ?: rootConfig.javaFilesAllowed
            if (!javaFilesAllowed) {
                tasks.named("compileKotlin").dependsOn(addForbidJavaFilesTask())
            }
        }
    }

    private fun Project.withExtension(action: Project.(KotlinLibraryConfigExtension) -> Unit) =
        afterEvaluate {
            it.action(it.extensions.getByType(KotlinLibraryConfigExtension::class.java))
        }
}
